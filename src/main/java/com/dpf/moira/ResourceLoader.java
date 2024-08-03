package com.dpf.moira;

import com.dpf.moira.yaml.WorkFlowYml;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ResourceLoader {

    private final Map<String, String> fileHashes;
    private final Map<String, WorkFlowYml> cache;

    ResourceLoader() {
        this.fileHashes = new HashMap<>();
        this.cache = new HashMap<>();
    }

    List<WorkFlowYml> loadWorkflows(String location) {

        List<WorkFlowYml> workflows = new ArrayList<>();

        try {
            URL resourceURL = getClass().getClassLoader().getResource(location);
            if (resourceURL != null) {
                URI resourceURI = resourceURL.toURI();
                Path path = Paths.get(resourceURI);

                FileUtils.listFiles(path.toFile(), new String[]{"yml"}, true)
                        .forEach(file -> {
                            try {
                                Path filePath = file.toPath();
                                if (hasFileChanged(filePath)) {
                                    try (InputStream inputStream = Files.newInputStream(filePath)) {
                                        WorkFlowYml workflowYml = readWorkflowYml(inputStream);
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
                                System.err.println("Failed to check file change: " + file + " " + e.getMessage());
                            }
                        });
            }
        } catch (URISyntaxException e) {
            System.err.println("Failed to load YAML resources: " + e.getMessage());
        }

        return workflows;
    }

    private WorkFlowYml readWorkflowYml(InputStream inputStream) {
        Yaml yaml = new Yaml();
        return yaml.loadAs(inputStream, WorkFlowYml.class);
    }

    private boolean hasFileChanged(Path filePath) throws IOException {
        String currentHash = computeFileHash(filePath);
        return !currentHash.equals(fileHashes.get(filePath.toString()));
    }

    private String computeFileHash(Path filePath) throws IOException {
        return DigestUtils.sha256Hex(Files.readAllBytes(filePath));
    }
}
