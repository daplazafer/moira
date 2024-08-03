package com.dpf.moira;

import com.dpf.moira.entity.DecisionNodeResult;
import com.dpf.moira.entity.DecisionTree;
import com.dpf.moira.entity.DecisionTreeId;
import com.dpf.moira.entity.NodeId;
import com.dpf.moira.entity.Transitions;
import com.dpf.moira.properties.MoiraProperties;
import com.dpf.moira.properties.PropertiesLoader;
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

    private static final String PROPERTIES_FILE = "dop.properties";

    private static MoiraConfig INSTANCE;

    private final MoiraProperties properties;

    private MoiraConfig() {
        this.properties = new PropertiesLoader(PROPERTIES_FILE).loadProperties(MoiraProperties.class);
    }

    public static MoiraConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoiraConfig();
        }
        return INSTANCE;
    }

    MoiraProperties getProperties(){
        return this.properties;
    }

    NodeRegistry nodeRegistry(Collection<Node<?, ?>> nodes) {
        return new NodeRegistry(nodes);
    }

    DecisionTreeRegistry decisionTreeRegistry() {

        List<DecisionTreeYml> decisionTrees = new ArrayList<>();

        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        String locationPattern = "classpath:" + this.properties.getYamlFilesPath() + "/*.yml";

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

