package com.dpf.moira;

import com.dpf.moira.entity.DecisionTree;
import com.dpf.moira.entity.DecisionTreeId;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

class DecisionTreeRegistry {

    private final Map<DecisionTreeId, DecisionTree> decisionTreeRegistryMap;

    DecisionTreeRegistry(Collection<DecisionTree> decisionTrees) {
        decisionTreeRegistryMap = decisionTrees.stream()
                .collect(Collectors.toUnmodifiableMap(DecisionTree::id, Function.identity()));
    }

    Optional<DecisionTree> get(DecisionTreeId decisionTreeId) {
        return Optional.ofNullable(decisionTreeRegistryMap.get(decisionTreeId));
    }
}
