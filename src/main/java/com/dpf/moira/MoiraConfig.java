package com.dpf.moira;

import com.dpf.moira.entity.*;
import com.dpf.moira.yaml.DecisionTreeYml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

class MoiraConfig {

    private static final Logger logger = LoggerFactory.getLogger(MoiraConfig.class);

    private static MoiraConfig INSTANCE;

    private MoiraConfig() {
    }

    public static MoiraConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoiraConfig();
        }
        return INSTANCE;
    }

    NodeRegistry nodeRegistry(Collection<Node<?, ?>> nodes) {
        return new NodeRegistry(nodes);
    }

    DecisionTreeRegistry decisionTreeRegistry(String yamlFilesPath) {

        List<DecisionTreeYml> decisionTrees = new ArrayList<>();

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        String locationPattern = "classpath:" + yamlFilesPath + "/*.yml";

        Yaml yaml = new Yaml(new LoaderOptions());

        try {
            Resource[] resources = resolver.getResources(locationPattern);

            for (Resource resource : resources) {
                try (InputStream inputStream = resource.getInputStream()) {
                    decisionTrees.add(yaml.loadAs(inputStream, DecisionTreeYml.class));
                } catch (Exception e) {
                    logger.error("Failed to load YAML file {} ", resource.getFilename(), e);
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load YAML resources", e);
        }

        return new DecisionTreeRegistry(decisionTrees.stream()
                .map(this::mapDecisionTree)
                .toList());
    }

    private DecisionTree mapDecisionTree(DecisionTreeYml decisionTreeYml) {
        var decisionTreeId = new DecisionTreeId(decisionTreeYml.getId());
        var start = new NodeId(decisionTreeYml.getStart());
        var transitionsByNode = decisionTreeYml.getNodes().stream()
                .collect(Collectors.toMap(
                        node -> new NodeId(node.getId()),
                        node -> new Transitions(
                                node.getTransitions().stream()
                                        .collect(Collectors.toUnmodifiableMap(
                                                transition -> new DecisionNodeResult(transition.getResult()),
                                                transition -> new NodeId(transition.getNext()))))));
        return new DecisionTree(decisionTreeId, start, transitionsByNode);
    }

}

