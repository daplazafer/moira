package com.dpf.moira.yaml;

import java.util.Collections;
import java.util.List;

public class DecisionTreeYml {

    private String id;
    private String start;
    private List<NodeYml> nodes;

    public DecisionTreeYml() {
    }

    public DecisionTreeYml(String id, String start, List<NodeYml> nodes) {
        this.id = id;
        this.start = start;
        this.nodes = nodes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStart() {
        return start;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public List<NodeYml> getNodes() {
        return nodes != null ? nodes : Collections.emptyList();
    }

    public void setNodes(List<NodeYml> nodes) {
        this.nodes = nodes;
    }

}
