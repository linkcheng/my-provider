package cn.hsb.sidecar.service;

import cn.hsb.common.dto.CommonResult;
import cn.hsb.common.dto.RegistryData;
import cn.hsb.common.dto.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DataServiceFallbackFactory implements DataService{
    @Override
    public CommonResult register(RegistryData data) {
        log.error("Registry applicationName={}, interfaceName={}, elements size={}",
                data.getApplicationName(), data.getInterfaceName(), data.getSupportElements().size());
        return new CommonResult(StatusCode.FAILURE, "注册失败");
    }
}
