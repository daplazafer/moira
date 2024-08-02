package com.dpf.moira.yaml;

import java.util.Collections;
import java.util.List;

public class NodeYml {

    private String id;
    private List<TransitionYml> transitions;

    public NodeYml() {
    }

    public NodeYml(String id, List<TransitionYml> transitions) {
        this.id = id;
        this.transitions = transitions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<TransitionYml> getTransitions() {
        return transitions != null ? transitions : Collections.emptyList();
    }

    public void setTransitions(List<TransitionYml> transitions) {
        this.transitions = transitions;
    }

}
