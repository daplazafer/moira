package com.dpf.moira.entity;

import java.util.Collections;
import java.util.Map;

public record DecisionTree(DecisionTreeId id, NodeId start, Map<NodeId, Transitions> transitionsByNode) {

    public DecisionTree {
        transitionsByNode = Collections.unmodifiableMap(transitionsByNode);
    }

}
