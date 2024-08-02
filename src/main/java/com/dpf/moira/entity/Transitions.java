package com.dpf.moira.entity;

import java.util.Collections;
import java.util.Map;

public record Transitions(Map<DecisionNodeResult, NodeId> transitions) {

    public Transitions {
        transitions = Collections.unmodifiableMap(transitions);
    }

}
