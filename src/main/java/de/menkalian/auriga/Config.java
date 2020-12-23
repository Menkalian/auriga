package de.menkalian.auriga;

import de.menkalian.auriga.config.BaseConfig;
import de.menkalian.auriga.config.FormatPlaceholder;
import de.menkalian.auriga.config.LoggerConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import static de.menkalian.auriga.config.Constants.*;

public class Config {
    public static final String DEFAULT_CONFIG_LOCATION = "./auriga_cfg.xml";

    private final Properties loadedProperties = new Properties();

    private BaseConfig selectedBaseConfig;
    private FormatPlaceholder selectedFormatPlaceholder;
    private LoggerConfig selectedLoggerConfig;
    private String loggingMethod;
    private String methodTemplate;
    private String paramTemplate;

    public Config () {
        this(DEFAULT_CONFIG_LOCATION);
    }

    public Config (String configLocation) {
        try {
            File configFile = new File(configLocation);
            if (!configFile.exists()) {
                loadDefaultConfig();
                if (configFile.createNewFile()) {
                    loadedProperties.storeToXML(new PrintStream(configFile), "GENERATED FILE BY AURIGA");
                }
            } else {
                loadedProperties.loadFromXML(new FileInputStream(configFile));
            }

            loadConfigFromProperties();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadDefaultConfig () {
        loadedProperties.put(CONFIGKEY_BASE, BaseConfig.PRINT.toString());
        loadedProperties.put(CONFIGKEY_TEMPLATE_CALL, "Executing {{METHOD}} with Params: {\n{{PARAMS}}\n}\n");
        loadedProperties.put(CONFIGKEY_TEMPLATE_PARAM, "    {{PARAM_NAME}} : {{PARAM_TYPE}} = {{PARAM_VALUE}}");

        selectedBaseConfig = BaseConfig.PRINT;
        initBaseConfig();
    }

    private void initBaseConfig () {
        switch (selectedBaseConfig) {
            case PRINT:
                loadedProperties.put(CONFIGKEY_PLACEHOLDER, "PRINTF");
                loadedProperties.put(CONFIGKEY_METHOD, "System.out.printf");
                loadedProperties.put(CONFIGKEY_LOGGER, "NONE");
                break;
            case SLF4J:
                loadedProperties.put(CONFIGKEY_PLACEHOLDER, "SLF4J");
                loadedProperties.put(CONFIGKEY_METHOD, "log.debug");
                loadedProperties.put(CONFIGKEY_LOGGER, "SLF4J");
                break;
            default:
                System.err.println("UNKNOWN BASE CONFIG. Maybe you are using an outdated version. Loading defaults for PRINT!");
                loadedProperties.put(CONFIGKEY_PLACEHOLDER, "PRINTF");
                loadedProperties.put(CONFIGKEY_METHOD, "System.out.printf");
                loadedProperties.put(CONFIGKEY_LOGGER, "NONE");
                break;
        }
    }

    private void loadConfigFromProperties () {
        selectedBaseConfig = BaseConfig.valueOf(loadedProperties.getProperty(CONFIGKEY_BASE, BaseConfig.PRINT.toString()));
        selectedFormatPlaceholder = FormatPlaceholder.valueOf(loadedProperties.getProperty(CONFIGKEY_PLACEHOLDER, FormatPlaceholder.NONE.toString()));
        selectedLoggerConfig = LoggerConfig.valueOf(loadedProperties.getProperty(CONFIGKEY_LOGGER, LoggerConfig.NONE.toString()));

        if (selectedLoggerConfig == LoggerConfig.CUSTOM) {
            selectedLoggerConfig.setClazz(loadedProperties.getProperty(CONFIGKEY_LOGGER_CLASS));
            selectedLoggerConfig.setProvisioning(loadedProperties.getProperty(CONFIGKEY_LOGGER_PROVISIONING));
        }

        loggingMethod = loadedProperties.getProperty(CONFIGKEY_METHOD, "System.out.println");
        methodTemplate = loadedProperties.getProperty(CONFIGKEY_TEMPLATE_CALL, "Executing {{METHOD}} with Params: {\n{{PARAMS}}\n}\n");
        paramTemplate = loadedProperties.getProperty(CONFIGKEY_TEMPLATE_PARAM, "    {{PARAM_NAME}} : {{PARAM_TYPE}} = {{PARAM_VALUE}}");
    }

    public BaseConfig getSelectedBaseConfig () {
        return selectedBaseConfig;
    }

    public FormatPlaceholder getSelectedFormatType () {
        return selectedFormatPlaceholder;
    }

    public FormatPlaceholder getSelectedFormatPlaceholder () {
        return selectedFormatPlaceholder;
    }

    public LoggerConfig getSelectedLoggerConfig () {
        return selectedLoggerConfig;
    }

    public String getMethodTemplate () {
        return methodTemplate;
    }

    public String getParamTemplate () {
        return paramTemplate;
    }

    public String getLoggingMethod () {
        return loggingMethod;
    }
}
