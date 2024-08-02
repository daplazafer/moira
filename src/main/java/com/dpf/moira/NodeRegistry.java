package com.dpf.moira;


import com.dpf.moira.entity.NodeId;

import java.util.*;

class NodeRegistry {

    private final Map<NodeId, Map<Class<?>, Node<?, ?>>> nodeRegistryMap;

    NodeRegistry(Collection<Node<?, ?>> nodes) {
        Map<NodeId, Map<Class<?>, Node<?, ?>>> nodeMap = new HashMap<>();
        for (Node<?, ?> node : nodes) {
            var decision = node.getClass().getAnnotation(Decision.class);
            if (decision != null) {
                Map<Class<?>, Node<?, ?>> contextMap = nodeMap.computeIfAbsent(new NodeId(decision.id()), k -> new HashMap<>());
                contextMap.put(node.getContextClass(), node);
            }
        }
        nodeRegistryMap = Collections.unmodifiableMap(nodeMap);
    }

    @SuppressWarnings("unchecked")
    <C> Optional<Node<C, ?>> get(NodeId nodeId, Class<C> ctxClass) {
        var contextMap = nodeRegistryMap.get(nodeId);
        if (contextMap != null) {
            var node = contextMap.get(ctxClass);
            if (node != null && ctxClass.isAssignableFrom(node.getContextClass())) {
                return Optional.of((Node<C, ?>) node);
            }
        }
        return Optional.empty();
    }
}
