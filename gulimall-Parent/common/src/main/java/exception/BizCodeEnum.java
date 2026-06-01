package exception;

public enum BizCodeEnum {
    UNKNOW_EXCEPTION("10000", "系统未知异常"),
    VAILD_EXCEPTION("10001", "参数格式校验失败"),
    SMS_CODE_EXCEPTION("10002", "验证码获取太频繁"),
    USER_EXIST_EXCEPTION("15001", "用户名已存在"),
    PHONE_EXIST_EXCEPTION("15002", "手机号已存在"),
    LOGINACTT_PASSWORD_ERROR("15003", "账号或密码错误");
    private final String code;
    private final String msg;
    BizCodeEnum(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    public String getCode() { return code; }
    public String getMsg() { return msg; }
}
