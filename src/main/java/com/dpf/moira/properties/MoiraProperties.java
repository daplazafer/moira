package com.dpf.moira.properties;

public class MoiraProperties {

    private String workflowFilesPath = "dop";

    private boolean hotReloadMode = false;

    public String getWorkflowFilesPath() {
        return workflowFilesPath;
    }

    public void setWorkflowFilesPath(String workflowFilesPath) {
        this.workflowFilesPath = workflowFilesPath;
    }

    public boolean isHotReloadMode() {
        return hotReloadMode;
    }

    public void setHotReloadMode(boolean hotReloadMode) {
        this.hotReloadMode = hotReloadMode;
    }
}
