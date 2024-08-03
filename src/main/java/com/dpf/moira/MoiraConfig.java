package com.dpf.moira;

import com.dpf.moira.entity.*;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

class MoiraConfig {

    private static final Logger logger = LoggerFactory.getLogger(MoiraConfig.class);

    private static final String PROPERTIES_FILE = "dop.properties";

    private static MoiraConfig INSTANCE;

    private final MoiraProperties properties;
    private final Map<String, String> fileHashes;

    public static MoiraConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new MoiraConfig();
        }
        return INSTANCE;
    }

    private MoiraConfig() {
        this.properties = new PropertiesLoader(PROPERTIES_FILE).loadProperties(MoiraProperties.class);
        this.fileHashes = new HashMap<>();
    }

    MoiraProperties getProperties() {
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
                var filePath = resource.getFilename();
                if (filePath != null) {
                    var path = Paths.get(resource.getURI());
                    var fileHash = computeFileHash(path);
                    if (!fileHash.equals(fileHashes.get(filePath))) {
                        fileHashes.put(filePath, fileHash);
                        try (InputStream inputStream = resource.getInputStream()) {
                            decisionTrees.add(yaml.loadAs(inputStream, DecisionTreeYml.class));
                        } catch (Exception e) {
                            logger.error("Failed to load YAML file {} ", filePath, e);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.error("Failed to load YAML resources", e);
        }

        return new DecisionTreeRegistry(decisionTrees.stream()
                .map(this::mapDecisionTree)
                .collect(Collectors.toList()));
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

    private String computeFileHash(Path filePath) throws IOException {
        return Files.readString(filePath).hashCode() + "";
    }

}
