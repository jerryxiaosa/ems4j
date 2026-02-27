package info.zhihui.ems.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.exception.SaTokenException;
import cn.hutool.core.lang.Opt;
import info.zhihui.ems.common.constant.ResultCode;
import info.zhihui.ems.common.exception.BusinessRuntimeException;
import info.zhihui.ems.common.exception.LoginException;
import info.zhihui.ems.common.exception.NotFoundException;
import info.zhihui.ems.common.exception.ParamException;
import info.zhihui.ems.common.utils.ResultUtil;
import info.zhihui.ems.common.vo.RestResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.exceptions.PersistenceException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.List;


@Slf4j
@RestControllerAdvice
public class RuntimeExceptionHandler {

    private static final String GENERIC_SYSTEM_MESSAGE = "系统异常，请稍后再试";
    private static final List<String> SENSITIVE_MESSAGE_KEYWORDS = List.of(
            "sql",
            "select",
            "insert",
            "update",
            "delete",
            "from",
            "where",
            "table",
            "column",
            "jdbc",
            "mybatis",
            "mapper",
            "bad sql grammar",
            "unknown column",
            "###"
    );

    @ExceptionHandler(NotFoundException.class)
    public RestResult<Void> handleRuntimeException(NotFoundException e) {
        log.error("handle NotFoundException: {}", e.getMessage());
        return ResultUtil.error(ResultCode.BUSINESS_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(ParamException.class)
    public RestResult<Void> handleRuntimeException(ParamException e) {
        log.error("handle ParamException: ", e);
        return ResultUtil.error(ResultCode.PARAMETER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public RestResult<Void> handleRuntimeException(ConstraintViolationException e) {
        log.error("handle ParamException: ", e);
        return ResultUtil.error(ResultCode.PARAMETER_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(BusinessRuntimeException.class)
    public RestResult<Void> handleRuntimeException(BusinessRuntimeException e) {
        log.error("handle BusinessRuntimeException: {}", e.getMessage());
        String message = sanitizeBusinessMessage(e.getMessage());
        return ResultUtil.error(Opt.ofNullable(e.getCode()).orElse(ResultCode.BUSINESS_ERROR.getCode()), message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public RestResult<Void> handleArgumentException(MethodArgumentNotValidException e) {
        log.error("handle MethodArgumentNotValidException: {}", e.getMessage());

        BindingResult bindingResult = e.getBindingResult();
        StringBuilder errorStr = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            String msg = String.format("%s%s;", fieldError.getField(), fieldError.getDefaultMessage());
            errorStr.append(msg);
        }
        return ResultUtil.error(ResultCode.PARAMETER_ERROR.getCode(), errorStr.toString());
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult<Void> handleRuntimeException(RuntimeException e) {
        log.error("handle runtime exception: ", e);
        return ResultUtil.error(ResultCode.FAILED.getCode(), GENERIC_SYSTEM_MESSAGE);
    }

    /**
     * 数据库相关异常统一兜底，避免 SQL 细节外泄。
     */
    @ExceptionHandler({DataAccessException.class, BadSqlGrammarException.class, PersistenceException.class, SQLException.class})
    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
    public RestResult<Void> handleDatabaseException(Exception e) {
        log.error("handle database exception: ", e);
        return ResultUtil.error(ResultCode.FAILED.getCode(), GENERIC_SYSTEM_MESSAGE);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public RestResult<Void> handleDuplicateKeyEx(DuplicateKeyException e) {
        log.warn("数据重复: {}", e.getMessage(), e);
        return ResultUtil.error("数据已存在，请确认");
    }

    @ExceptionHandler(NotPermissionException.class)
    public RestResult<Void> handleNotPermissionException(NotPermissionException e, HttpServletRequest request) {
        log.error("请求地址'{}',权限码校验失败'{}'", request.getRequestURI(), e.getMessage());
        return ResultUtil.error(ResultCode.PERMISSION_ERROR.getCode(), "没有访问权限");
    }

    @ExceptionHandler(NotRoleException.class)
    public RestResult<Void> handleNotRoleException(NotRoleException e, HttpServletRequest request) {
        log.error("请求地址'{}',角色权限校验失败'{}'", request.getRequestURI(), e.getMessage());
        return ResultUtil.error(ResultCode.PERMISSION_ERROR.getCode(), "角色异常");
    }

    @ExceptionHandler(LoginException.class)
    public RestResult<Void> handleRuntimeException(LoginException e) {
        log.error("handle UserException: {}", e.getMessage());
        return ResultUtil.error(ResultCode.ACCOUNT_ERROR.getCode(), e.getMessage());
    }

    @ExceptionHandler(NotLoginException.class)
    public RestResult<Void> handleNotLoginException(NotLoginException e, HttpServletRequest request) {
        log.error("请求地址'{}',获取登录信息失败'{}',无法访问系统资源", request.getRequestURI(), e.getMessage());
        return ResultUtil.error(ResultCode.NOT_LOGIN_ERROR.getCode(), "请先登录");
    }

    @ExceptionHandler(SaTokenException.class)
    public RestResult<Void> handleSaTokenException(SaTokenException e, HttpServletRequest request) {
        log.error("请求地址'{}',异常'{}'", request.getRequestURI(), e.getMessage());
        return ResultUtil.error(ResultCode.PERMISSION_ERROR.getCode(), "认证失败");
    }

    private String sanitizeBusinessMessage(String message) {
        if (message == null || message.isBlank()) {
            return GENERIC_SYSTEM_MESSAGE;
        }
        return containsSensitiveToken(message) ? GENERIC_SYSTEM_MESSAGE : message;
    }

    private boolean containsSensitiveToken(String message) {
        String lowerCaseMessage = message.toLowerCase();
        for (String keyword : SENSITIVE_MESSAGE_KEYWORDS) {
            if (lowerCaseMessage.contains(keyword)) {
                return true;
            }
        }
        return false;
    }
}
