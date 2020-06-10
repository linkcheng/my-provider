package cn.hsb.sidecar.service;

import cn.hsb.common.dto.CommonResult;
import cn.hsb.common.dto.RegistryData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "${router.name}", fallback = DataServiceFallbackFactory.class)
public interface DataService {
    @PostMapping("/data_service/register")
    CommonResult register(@RequestBody RegistryData data);
}
