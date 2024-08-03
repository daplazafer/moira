package com.dpf.moira;


import com.dpf.moira.entity.NodeId;

import java.util.*;

class NodeRegistry {

    private final Map<NodeId, Map<Class<?>, Node<?, ?>>> nodeRegistryMap;

    NodeRegistry(Collection<Node<?, ?>> nodes) {
        Map<NodeId, Map<Class<?>, Node<?, ?>>> nodeMap = new HashMap<>();
        for (Node<?, ?> node : nodes) {
            var decision = node.getClass().getAnnotation(Decision.class);
            var nodeId = decision != null ? decision.id() : node.getClass().getSimpleName();
            Map<Class<?>, Node<?, ?>> scenarioMap = nodeMap.computeIfAbsent(new NodeId(nodeId), k -> new HashMap<>());
            scenarioMap.put(node.getScenarioClass(), node);
        }
        nodeRegistryMap = Collections.unmodifiableMap(nodeMap);
    }

    @SuppressWarnings("unchecked")
    <S> Optional<Node<S, ?>> get(NodeId nodeId, Class<S> scenarioClass) {
        var scenarioMap = nodeRegistryMap.get(nodeId);
        if (scenarioMap != null) {
            var node = scenarioMap.get(scenarioClass);
            if (node != null && scenarioClass.isAssignableFrom(node.getScenarioClass())) {
                return Optional.of((Node<S, ?>) node);
            }
        }
        return Optional.empty();
    }
}
