package com.example.redis;

/**
 * 用于测试：非白名单类型
 */
public class ForbiddenPayload {
    private String value;

    public ForbiddenPayload() {
    }

    public ForbiddenPayload(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
