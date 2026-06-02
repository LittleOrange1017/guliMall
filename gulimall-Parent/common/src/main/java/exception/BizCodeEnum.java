package exception;

public enum BizCodeEnum {
    UNKNOW_EXCEPTION(10000, "系统未知异常"),
    VAILD_EXCEPTION(10001, "参数格式校验失败"),
    SMS_CODE_EXCEPTION(10002, "验证码获取太频繁"),
    USER_EXIST_EXCEPTION(15001, "用户名已存在"),
    PHONE_EXIST_EXCEPTION(15002, "手机号已存在"),
    LOGINACTT_PASSWORD_ERROR(15003, "账号或密码错误"),
    USERNAME_NOT_EXIST(15004, "用户名不存在"),
    PASSWORD_ERROR(15005, "密码错误"),
    SOCIALUSER_IS_EXIST(15006,"社交用户已经存在");
    private final int code;
    private final String msg;
    BizCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public int getCode() { return code; }
    public String getMsg() { return msg; }
}
