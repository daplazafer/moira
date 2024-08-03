package com.dpf.moira.properties;

public class MoiraProperties {

    private String yamlFilesPath = "dop";

    private boolean hotReloadMode = false;

    public String getYamlFilesPath() {
        return yamlFilesPath;
    }

    public void setYamlFilesPath(String yamlFilesPath) {
        this.yamlFilesPath = yamlFilesPath;
    }

    public boolean isHotReloadMode() {
        return hotReloadMode;
    }

    public void setHotReloadMode(boolean hotReloadMode) {
        this.hotReloadMode = hotReloadMode;
    }
}
