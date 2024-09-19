package com.dpf.moira.yaml.mapper;

import com.dpf.moira.entity.*;
import com.dpf.moira.yaml.WorkFlowYml;

import java.util.Map;
import java.util.stream.Collectors;

public class WorkFlowYmlMapper {

    public static Workflow toEntity(WorkFlowYml yml) {
        WorkflowId decisionTreeId = new WorkflowId(yml.getId());
        NodeId start = new NodeId(yml.getStart());
        Map<NodeId, Transitions> transitionsByNode = yml.getNodes().stream()
                .collect(Collectors.toMap(
                        node -> new NodeId(node.getId()),
                        node -> new Transitions(
                                node.getTransitions().stream()
                                        .collect(Collectors.toUnmodifiableMap(
                                                transition -> new DecisionNodeResult(transition.getResult()),
                                                transition -> new NodeId(transition.getNext()))))));
        return new Workflow(decisionTreeId, start, transitionsByNode);
    }

}
