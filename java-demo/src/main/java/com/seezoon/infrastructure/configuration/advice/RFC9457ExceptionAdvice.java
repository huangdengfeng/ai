package com.seezoon.infrastructure.configuration.advice;

import com.seezoon.infrastructure.error.ErrorCode;
import com.seezoon.infrastructure.exception.BizException;
import com.seezoon.infrastructure.exception.SysException;
import com.seezoon.infrastructure.utils.OtelUtils;
import io.opentelemetry.api.common.AttributeKey;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * 统一异常处理，将异常转化为错误码错误信息
 *
 * @author huangdengfeng
 * @see <a href="https://datatracker.ietf.org/doc/html/rfc9457">RFC 9457</a>
 */
@RestControllerAdvice
@Slf4j
public class RFC9457ExceptionAdvice {


    private static final String ERROR_CODE = "errorCode";
    private static final AttributeKey ATTR_ERROR_CODE = AttributeKey.stringKey(ERROR_CODE);

    /**
     * for Receiving parameters and verification
     *
     * @param e
     * @return
     */
    @ExceptionHandler({MissingServletRequestParameterException.class, ValidationException.class,
            MethodArgumentTypeMismatchException.class, HttpMessageNotReadableException.class, BindException.class,
            IllegalArgumentException.class})
    public ProblemDetail parameterInvalidException(Exception e) {
        log.error("parameter invalid exception", e);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(ErrorCode.PARAM_INVALID.msg());
        if (log.isDebugEnabled()) {
            problemDetail.setDetail(e.getMessage());
        }
        addTraceId(problemDetail);
        this.addCode(problemDetail, ErrorCode.PARAM_INVALID.code());
        return problemDetail;
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail noResourceFoundException(NoResourceFoundException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle(HttpStatus.NOT_FOUND.getReasonPhrase());
        if (log.isDebugEnabled()) {
            problemDetail.setDetail(e.getMessage());
        }
        addTraceId(problemDetail);
        return problemDetail;
    }

    @ExceptionHandler(BizException.class)
    public ProblemDetail bizException(BizException e) {
        log.error("biz exception code:{},msg:{}", e.getCode(), e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(e.getMessage());
        addTraceId(problemDetail);
        this.addCode(problemDetail, e.getCode());
        return problemDetail;
    }

    @ExceptionHandler(SysException.class)
    public ProblemDetail sysException(SysException e) {
        log.error("system exception code:{},msg:{}", e.getCode(), e.getMessage());
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle(e.getMessage());
        this.addTraceId(problemDetail);
        this.addCode(problemDetail, e.getCode());
        return problemDetail;
    }

    /**
     * using this if there is no match.
     *
     * @param e
     * @return
     */
    @ExceptionHandler({Exception.class, RuntimeException.class})
    public ProblemDetail exception(Exception e) {
        log.error("internal server error", e);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        if (log.isDebugEnabled()) {
            problemDetail.setDetail(e.getMessage());
        }
        this.addTraceId(problemDetail);
        return problemDetail;
    }

    private void addTraceId(ProblemDetail problemDetail) {
        problemDetail.setProperty(OtelUtils.TRACE_ID, OtelUtils.getTraceId());
    }

    private void addCode(ProblemDetail problemDetail, int code) {
        problemDetail.setProperty(ERROR_CODE, code);
        OtelUtils.setAttribute(ATTR_ERROR_CODE, code);
    }
}
