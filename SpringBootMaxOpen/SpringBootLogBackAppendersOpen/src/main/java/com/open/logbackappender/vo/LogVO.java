package com.open.logbackappender.vo;

public class LogVO implements java.io.Serializable {
    private static final long serialVersionUID = 1L;

    private String localIp = null;
    private String localSystemName = null;
    private String env = null;
    private String body = null;

    public String getLocalIp() {
        return localIp;
    }

    public void setLocalIp(String localIp) {
        this.localIp = localIp;
    }

    public String getLocalSystemName() {
        return localSystemName;
    }

    public void setLocalSystemName(String localSystemName) {
        this.localSystemName = localSystemName;
    }

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
