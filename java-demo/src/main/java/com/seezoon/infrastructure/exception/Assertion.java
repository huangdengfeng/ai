package com.seezoon.infrastructure.exception;


import com.seezoon.infrastructure.error.ErrorCode;
import org.apache.commons.lang3.StringUtils;

/**
 * Assertion utility class that assists in validating arguments.
 *
 * @author huangdengfeng
 */
public abstract class Assertion {

    private static final int DEFAULT_CODE = ErrorCode.ASSERTION_ERROR.code();

    public static void isTrue(boolean expression, String msg) {
        if (!expression) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }

    public static void affectedOne(int affectedRows) {
        isTrue(affectedRows == 1, "expect affected one,actual " + affectedRows);
    }

    public static void leOneRow(int actual) {
        Assertion.isTrue(actual <= 1, "expect: <= 1 ,actual:" + actual);
    }

    public static void affectedRow(int expect, int actual) {
        Assertion.isTrue(expect == actual, "expect: " + expect + " ,actual:" + actual);
    }

    public static void notNull(Object object, String msg) {
        if (object == null) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }

    public static void notEmpty(String object, String msg) {
        if (StringUtils.isEmpty(object)) {
            throw new BizException(DEFAULT_CODE, msg);
        }
    }
}
