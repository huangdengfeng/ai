package com.seezoon.domain.dao.types;

import java.util.Arrays;
import java.util.Objects;


public enum DbRecordStatus {

    VALID((byte) 1, "有效"),
    INVALID((byte) 2, "无效"),
    ;

    private byte code;
    private String name;

    DbRecordStatus(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return code;
    }

    public static boolean isValid(Byte code) {
        return Objects.equals(VALID.code, code);
    }

    public static boolean isInvalid(Byte code) {
        return Objects.equals(INVALID.code, code);
    }

    public static void check(Byte code) {
        boolean match = Arrays.stream(DbRecordStatus.values()).anyMatch(item -> Objects.equals(item.code, code));
        if (!match) {
            throw new IllegalArgumentException("db record status code invalid:" + code);
        }
    }
}
