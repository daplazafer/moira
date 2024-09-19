package com.dpf.moira.entity;

import java.util.Collections;
import java.util.Map;

public class Transitions {

    private final Map<DecisionNodeResult, NodeId> transitions;

    public Transitions(final Map<DecisionNodeResult, NodeId> transitions) {
        this.transitions = Collections.unmodifiableMap(transitions);
    }

    public Map<DecisionNodeResult, NodeId> getTransitions() {
        return transitions;
    }
}