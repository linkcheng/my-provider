package cn.hsb.common.dto;

import java.io.Serializable;

public enum StatusCode implements Serializable {
    SUCCESS(200),
    FAILURE(500);

    private int code;

    StatusCode(int code) {
        this.code = code;
    }
}
