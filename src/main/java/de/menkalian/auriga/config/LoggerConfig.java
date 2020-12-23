package de.menkalian.auriga.config;

public enum LoggerConfig {
    NONE("-", "-"),
    SLF4J("org.slf4j.Logger", "org.slf4j.LoggerFactory.getLogger({{CLASS}}.class)"),
    CUSTOM("-", "-");

    private String clazz;
    private String provisioning;

    LoggerConfig (String clazz, String provisioning) {
        this.clazz = clazz;
        this.provisioning = provisioning;
    }

    public String getClazz () {
        return clazz;
    }

    public void setClazz (String clazz) {
        this.clazz = clazz;
    }

    public String getProvisioning () {
        return provisioning;
    }

    public void setProvisioning (String provisioning) {
        this.provisioning = provisioning;
    }
}
