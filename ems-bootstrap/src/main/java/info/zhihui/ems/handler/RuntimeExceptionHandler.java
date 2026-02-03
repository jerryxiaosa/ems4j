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
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@Slf4j
@RestControllerAdvice
public class RuntimeExceptionHandler {

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
        return ResultUtil.error(Opt.ofNullable(e.getCode()).orElse(ResultCode.BUSINESS_ERROR.getCode()), e.getMessage());
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
        String message = "系统异常，请稍后再试";
        return ResultUtil.error(ResultCode.FAILED.getCode(), message);
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

}
