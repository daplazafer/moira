package com.dpf.moira.entity;

import java.util.Collections;
import java.util.Map;

public record Workflow(WorkflowId id, NodeId start, Map<NodeId, Transitions> transitionsByNode) {

    public Workflow {
        transitionsByNode = Collections.unmodifiableMap(transitionsByNode);
    }

}
