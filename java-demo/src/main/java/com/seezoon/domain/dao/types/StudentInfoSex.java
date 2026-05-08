package com.seezoon.domain.dao.types;

import java.util.Arrays;
import java.util.Objects;
import lombok.Getter;

@Getter
public enum StudentInfoSex {
    MALE((byte) 1, "男"),
    FEMALE((byte) 2, "女"),
    ;

    private byte code;
    private String name;

    StudentInfoSex(byte code, String name) {
        this.code = code;
        this.name = name;
    }

    public byte code() {
        return code;
    }
    public static boolean isMale(Byte code) {
        return Objects.equals(MALE.code, code);
    }

    public static boolean isFemale(Byte code) {
        return Objects.equals(FEMALE.code, code);
    }

    public static void check(Byte code) {
        boolean match = Arrays.stream(StudentInfoSex.values()).anyMatch(item -> Objects.equals(item.code, code));
        if (!match) {
            throw new IllegalArgumentException("student info sex code invalid:" + code);
        }
    }
}
