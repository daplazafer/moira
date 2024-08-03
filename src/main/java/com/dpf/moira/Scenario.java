package com.dpf.moira;

public class Scenario<S> {

    private final String executionId;
    private final S value;

    Scenario(String executionId, S value) {
        this.executionId = executionId;
        this.value = value;
    }

    public String getExecutionId() {
        return executionId;
    }

    public S get() {
        return value;
    }

}
