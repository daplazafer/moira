package com.dpf.moira;

public class Scenario<S> {

    private final String executionId;
    private final S scenario;

    public Scenario(String executionId, S scenario) {
        this.executionId = executionId;
        this.scenario = scenario;
    }

    public String getExecutionId() {
        return executionId;
    }

    public S get() {
        return scenario;
    }

}
