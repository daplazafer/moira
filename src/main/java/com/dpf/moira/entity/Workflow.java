package com.dpf.moira.entity;

import java.util.Collections;
import java.util.Map;

public class Workflow {

    private final WorkflowId id;
    private final NodeId start;
    private final Map<NodeId, Transitions> transitionsByNode;

    public Workflow(final WorkflowId id, final NodeId start, final Map<NodeId, Transitions> transitionsByNode) {
        this.id = id;
        this.start = start;
        this.transitionsByNode = Collections.unmodifiableMap(transitionsByNode);
    }

    public WorkflowId getId() {
        return id;
    }

    public NodeId getStart() {
        return start;
    }

    public Map<NodeId, Transitions> getTransitionsByNode() {
        return transitionsByNode;
    }
}