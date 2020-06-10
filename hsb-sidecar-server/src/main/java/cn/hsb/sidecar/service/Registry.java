package cn.hsb.sidecar.service;

import cn.hsb.common.dto.CommonResult;
import cn.hsb.common.dto.RegistryData;
import cn.hsb.common.dto.StatusCode;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@RefreshScope
@Component
@SuppressWarnings("all")
public class Registry {
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ConfigurableEnvironment environment;

    @Autowired
    @Qualifier("sidecarHealthIndicator")
    private HealthIndicator healthIndicator;

    @Autowired
    private DataService dataService;

    // sidecar 代理的服务地址 localhost:port
    @Value("${server.elementUrl}")
    private String url;

    // 上一次 schedule 调度时的上报的因子数
    private Set<String> oldElements = Sets.newHashSet();

    // 上一次 schedule 调度时的监控状态
    private Status oldServerStatus = Status.DOWN;

    private boolean isOK = false;

    @PostConstruct
    private void init() {
        schedule();
    }

    /**
     * 周期地调度
     */
    private void schedule() {
        Schedulers.single().schedulePeriodically(() -> {
            Status status = healthIndicator.health().getStatus();

            if (status.equals(Status.DOWN)) {
                log.warn("Server is down, wait to up");
                oldServerStatus = Status.DOWN;
            } else if (!oldServerStatus.equals(status) || !isOK) {
                log.info("Server is up, get elements and try to register them");
                isOK = getAndRegister();
                oldServerStatus = Status.UP;
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    /**
     * 拉取与注册
     */
    private boolean getAndRegister() {
        // 拉取
        List<String> elements = getElements();
        log.info("Get elements successfully, size={}", elements.size());
        if(elements.size() < 1) {
            log.error("Get elements size = 0");
            return false;
        }

        // 如果两次请求的因子没有变化不需要重新注册
        // 否则需要注册
        if(elements.size() == oldElements.size() && oldElements.containsAll(elements)) {
            log.info("两次因子没有差别，不需要更新");
            return true;
        }

        String applicationName = environment.getProperty("spring.application.name");
        String interfaceName = environment.getProperty("interface.name");
        RegistryData data = RegistryData.builder()
                .applicationName(applicationName)
                .interfaceName(interfaceName)
                .supportElements(elements)
                .build();

        // 注册
        CommonResult ret = dataService.register(data);

        if(ret.getCode() == StatusCode.SUCCESS) {
            log.info("Get elements, and register them successfully");
            oldElements.clear();
            oldElements.addAll(elements);
            return true;
        } else {
            log.error("Get elements successfully, but register them unsuccessfully");
            return false;
        }
    }

    /**
     * 请求 sidecar 代理的服务，获取本地支持的所有因子
     */
    private List<String> getElements() {
        try {
            ResponseEntity<List<String>> exchange = this.restTemplate.exchange(
                url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<String>>(){}
            );
            return exchange.getBody();
        } catch (RestClientException e) {
            log.error("Get elements exception {}", e.toString());
            return Lists.newArrayList();
        }
     }
}