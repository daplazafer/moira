package com.dpf.moira;

import com.dpf.moira.yaml.WorkFlowYml;
import org.apache.commons.codec.digest.DigestUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResourceLoader {

    private final Map<String, String> fileHashes;

    private final Map<String, WorkFlowYml> cache;

    public ResourceLoader() {
        this.fileHashes = new HashMap<>();
        this.cache = new HashMap<>();
    }

    public List<WorkFlowYml> loadWorkflows(String locationPattern) {
        List<WorkFlowYml> workflows = new ArrayList<>();

        try {
            var resourceURL = getClass().getClassLoader().getResource(locationPattern);
            if (resourceURL != null) {
                var resourceURI = resourceURL.toURI();
                var path = Paths.get(resourceURI);

                try (var stream = Files.walk(path)) {
                    stream.filter(Files::isRegularFile)
                            .forEach(filePath -> {
                                try {
                                    if (hasFileChanged(filePath)) {
                                        try (InputStream inputStream = Files.newInputStream(filePath)) {
                                            var workflowYml = readWorkflowYml(inputStream);
                                            fileHashes.put(filePath.toString(), computeFileHash(filePath));
                                            cache.put(filePath.toString(), workflowYml);
                                            workflows.add(workflowYml);
                                        } catch (IOException e) {
                                            System.err.println("Failed to read YAML file: " + filePath + " " + e.getMessage());
                                        }
                                    } else {
                                        workflows.add(cache.get(filePath.toString()));
                                    }
                                } catch (IOException e) {
                                    System.err.println("Failed to check file change: " + filePath + " " + e.getMessage());
                                }
                            });
                }
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Failed to load YAML resources: " + e.getMessage());
        }

        return workflows;
    }

    private WorkFlowYml readWorkflowYml(InputStream inputStream) {
        var yaml = new Yaml();
        return yaml.loadAs(inputStream, WorkFlowYml.class);
    }

    private boolean hasFileChanged(Path filePath) throws IOException {
        var currentHash = computeFileHash(filePath);
        return !currentHash.equals(fileHashes.get(filePath.toString()));
    }

    private String computeFileHash(Path filePath) throws IOException {
        return DigestUtils.sha256Hex(Files.readAllBytes(filePath));
    }
}
