package cn.hsb.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegistryData implements Serializable {
    // 服务名称
    private String applicationName;
    // 支持的因子列表
    private List<String> supportElements;
    // 接口名称
    private String interfaceName;
}
