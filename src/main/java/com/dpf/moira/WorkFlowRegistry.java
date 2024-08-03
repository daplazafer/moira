package com.dpf.moira;

import com.dpf.moira.entity.Workflow;
import com.dpf.moira.entity.WorkflowId;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class WorkFlowRegistry {

    private final Map<WorkflowId, Workflow> decisionTreeRegistryMap;

    WorkFlowRegistry(Collection<Workflow> workflows) {
        decisionTreeRegistryMap = workflows.stream()
                .collect(Collectors.toUnmodifiableMap(Workflow::id, Function.identity()));
    }

    Optional<Workflow> get(WorkflowId workFlowId) {
        return Optional.ofNullable(decisionTreeRegistryMap.get(workFlowId));
    }
}
