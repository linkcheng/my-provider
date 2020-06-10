package cn.hsb.router.controller;

import cn.hsb.common.dto.CommonResult;
import cn.hsb.common.dto.RegistryData;
import cn.hsb.common.dto.StatusCode;
import cn.hsb.router.dto.ElementData;
import cn.hsb.router.dto.RequestData;
import cn.hsb.router.entity.ElementConfig;
import cn.hsb.router.service.ElementConfigService;
import cn.hsb.router.service.RedisService;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.common.collect.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/data_service")
@RefreshScope
public class DataServiceController {
    private ConcurrentMap<String, String> conMap = new ConcurrentHashMap<>(10240);
    private ThreadPoolTaskExecutor executor;
    private CompletionService<Map<String, Object>> completionService;

    @Autowired
    private ElementConfigService elementConfigServiceImpl;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RestTemplate shortRestTemplate;

    @Value("${element.defaultPath}")
    private String defaultPath;

    @Autowired
    @Qualifier("taskConcurrentExecutor")
    public void setCompletionService(Executor executor) {
        this.executor = (ThreadPoolTaskExecutor)executor;
        this.completionService = new ExecutorCompletionService<>(executor);
    }

    /**
     * 初始化数据
     */
    @PostConstruct
    public void init() {
        log.info("Start to init");
        List<ElementConfig> data = elementConfigServiceImpl.getAll();
        ArrayListMultimap<String, String> path2Names = ArrayListMultimap.create();
        // 初始化本地 map
        data.forEach(e -> {
            conMap.put(e.getName(), e.getPath());
            path2Names.put(e.getPath(), e.getName());
        });
        log.info("Get element size={}, conMap cache key size={}, path2Names cache key size={}, value size={}",
                data.size(), conMap.keySet().size(), path2Names.keySet().size(), path2Names.size());

        // 初始化 redis 缓存
        int size = conMap.keySet().size();
        int threshold = 500;
        if (size > threshold) {
            Map<String, String> map = Maps.newHashMap();
            int i = 0;
            for (Map.Entry<String, String> entry: conMap.entrySet()) {
                map.put(entry.getKey(), entry.getValue());
                ++i;
                if (i % threshold == 0) {
                    redisService.multiSet(map);
                    map.clear();
                }
            }
            redisService.multiSet(map);
        } else {
            redisService.multiSet(conMap);
        }

        for (String path : path2Names.keySet()) {
            List<String> names = path2Names.get(path);
            redisService.sAdd(path, names.toArray(new String[0]));
        }
        log.info("Init done");
    }

    @GetMapping("/elements")
    public Map getElements() {
        return conMap;
    }

    @GetMapping("/default")
    public String getDefaultPath() {
        return defaultPath;
    }

    /**
     * 服务注册
     */
    @PostMapping("/register")
    @SentinelResource(value = "register", fallback = "registerFallback")
    public CommonResult register(@RequestBody RegistryData data) {
        log.info("RegistryData={}", data.toString());
        final List<String> newElements = data.getSupportElements();
        if (newElements.size() < 1) {
            log.info("没有新增因子不需要注册");
            return new CommonResult(StatusCode.SUCCESS, "没有新增因子不需要注册");
        }

        final String newPath = concatUrl(data.getApplicationName(), data.getInterfaceName());
        Set<String> oldElements = redisService.members(newPath);
        if (oldElements.size() == newElements.size() && oldElements.containsAll(newElements)) {
            log.info("新增因子已存在不需要再次注册");
            return new CommonResult(StatusCode.SUCCESS, "新增因子已存在不需要再次注册");
        }

        // 1. 删除新 path 对应的老 elements，以及新 elements 对应的老 path
        // 2. 删除老 path 对应的 elements，因为它们移动到新 path 中了
        // 3. 添加新 path 对应新的 elements，以及新 elements 对应新 path

        List<String> oldPaths = redisService.multiGet(newElements);
        HashMultimap<String, String> oldPath2NewElements = HashMultimap.create();

        for (int i = 0; i < oldPaths.size(); i++) {
            if (Strings.isNotEmpty(oldPaths.get(i)) && !newPath.equals(oldPaths.get(i))) {
                oldPath2NewElements.put(oldPaths.get(i), newElements.get(i));
            }
        }
        if (oldElements.size() > 0) {
            log.info("断开 {} 与 {} 的映射", newPath, oldElements.toString());
            delElements(oldElements, newPath);
        }
        if (oldPath2NewElements.size() > 0) {
            log.info("更新 {}", oldPath2NewElements.toString());
            updElements(oldPath2NewElements);
        }

        log.info("新增 {} 与 {} 的映射", newPath, newElements.toString());
        addElements(Sets.newHashSet(newElements), newPath);

        return new CommonResult(StatusCode.SUCCESS, "注册成功");
    }

    public CommonResult registerFallback(RegistryData data, Throwable e) {
        log.error("Registry data={}, error={}", data.toString(), e.toString());
        return new CommonResult<>(StatusCode.FAILURE, "注册失败", e.toString());
    }

    /**
     * 更新因子对应的 path
     */
    private void updElements(HashMultimap<String, String> oldPath2NewElements) {
        try {
            for (String oldPath : oldPath2NewElements.keySet()) {
                redisService.sRem(oldPath, oldPath2NewElements.get(oldPath).toArray());
            }
        } catch (Exception e) {
            log.info(e.toString());
        }
    }

    /**
     * 删除因子
     * @param elements 需要删除的
     */
    private void delElements(Set<String> elements, String path) {
        // 更新数据库
        elementConfigServiceImpl.deleteMany(elements);
        // 跟新 redis 缓存
        redisService.deleteKey(elements);
        redisService.sRem(path, elements.toArray());
        // 更新本地缓存
        elements.forEach(e -> conMap.remove(e));
    }

    /**
     * 新增因子
     * @param elements 需要新增的
     */
    private void addElements(Set<String> elements, String path) {
        HashMap<String, String> tmp = Maps.newHashMap();
        List<ElementConfig> ecs = elements.stream()
                .map(e -> {
                    tmp.put(e, path);
                    return ElementConfig.create(e, path);
                })
                .collect(Collectors.toList());
        // 更新数据库
        elementConfigServiceImpl.updateMany(ecs);
        // 跟新 redis 缓存
        redisService.multiSet(tmp);
        redisService.sAdd(path, (String[])elements.toArray(new String[0]));
        // 更新本地缓存
        conMap.putAll(tmp);
    }

    /**
     * 计算因子值
     * @param data ElementData
     */
    @PostMapping("/element")
    @SentinelResource(value = "element", blockHandler = "elementBlockHandler", fallback = "elementFallBack")
    public JSONObject element(@RequestBody ElementData data) {
        log.info(data.toString());
        List<String> elements = data.getElement_id_list();
        List<String> finalElements = data.getFinal_element_id_list();
        Map<String, Object> context = data.getContext();

        // 任务分桶
        ArrayListMultimap<String, String> path2Names = bucket(elements);
        log.info("dispatch bucket={}", path2Names.toString());

        // 分发任务
        for (String path : path2Names.keySet()) {
            List<String> names = path2Names.get(path);
            RequestData requestData = RequestData.create(path, ElementData.create(names, finalElements, context));
            completionService.submit(() -> requestProvider(requestData));
            log.info("Add task to pool data={}", requestData.toString());
        }

        // 用于监控线程池使用情况, nqa
        executorState();

        // 取出结果
        JSONObject result = new JSONObject();
        for (String path : path2Names.keySet()) {
            try {
                Future<Map<String, Object>> future = completionService.take();
                Map<String, Object> res = future.get();
                result.putAll(res);
                log.info("Get result={}", res);
            } catch (Exception e) {
                log.error(e.toString());
            }
        }

        log.info(JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue));
        return result;
    }

    private JSONObject requestProvider(RequestData requestData) {
        String url = requestData.getPath();
        ElementData data = requestData.getData();
        try {
            return shortRestTemplate.postForObject(url, data, JSONObject.class);
        } catch (RestClientException e) {
            log.error("Get elements exception {}", e.toString());
            return new JSONObject();
        }
    }

    public JSONObject elementBlockHandler(ElementData elementData, Throwable e) {
        log.error("Element data={}, error={}", elementData.toString(), e.toString());
        return new JSONObject();
    }

    public JSONObject elementFallBack(ElementData elementData, Throwable e) {
        log.error("Element data={}, error={}", elementData.toString(), e.toString());
        return new JSONObject();
    }

    /**
     * 用于监控线程池使用情况
     */
    private void executorState() {
        String threadNamePrefix = executor.getThreadNamePrefix();
        int activeCount = executor.getActiveCount() ;
        int queueSize = executor.getThreadPoolExecutor().getQueue().size();
        int poolSize = executor.getPoolSize();

        log.info("线程池 " + threadNamePrefix + "========>"
                + " active thread count:" + activeCount
                + ", queue size:" + queueSize
                + ", pool size:" + poolSize
        );
        if (queueSize > 500) {
            log.warn("线程池 " + threadNamePrefix + "========>"
                    + " active thread count:" + activeCount
                    + ", queue size:" + queueSize
                    + ", pool size:" + poolSize
            );
        }
    }

    /**
     * 把传入的 elements 对应的分到不同的桶中，每个桶的名字为 path
     * @param elements List
     * @return ArrayListMultimap
     */
    private ArrayListMultimap<String, String> bucket(List<String> elements) {
        ArrayListMultimap<String, String> path2Names = ArrayListMultimap.create();
        elements.forEach(name -> {
            String path = conMap.getOrDefault(name, defaultPath);

            // 本地 map 中没有找到对应 path 时，尝试从 redis 缓存中获取，
            if (path.equals(defaultPath)) {
                path = redisService.get(name, defaultPath);

                // 如果取到非默认值，则刷新本地 map
                if (!path.equals(defaultPath)) {
                    conMap.put(name, path);
                }
            }

            path2Names.put(path, name);
        });

        return path2Names;
    }

    private String concatUrl(String app, String name) {
        return name.startsWith("/") ? "http://" + app + name : "http://" + app + "/" + name;
    }
}
