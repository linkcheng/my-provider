package cn.hsb.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> implements Serializable {
    private cn.hsb.common.dto.StatusCode code;
    private String message;
    private T data;

    public CommonResult(cn.hsb.common.dto.StatusCode code, String message) {
        this.code = code;
        this.message = message;
    }
}