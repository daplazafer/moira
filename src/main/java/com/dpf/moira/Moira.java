package com.dpf.moira;

import com.dpf.moira.entity.DecisionNodeResult;
import com.dpf.moira.entity.DecisionTree;
import com.dpf.moira.entity.DecisionTreeId;
import com.dpf.moira.entity.NodeId;
import com.dpf.moira.properties.MoiraProperties;
import com.dpf.moira.properties.PropertiesLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.UUID;

/**
 * Manages and executes decision trees using a registry of decision trees and nodes.
 */
public class Moira {

    private static final Logger logger = LoggerFactory.getLogger(Moira.class);

    private static final String PROPERTIES_FILE = "dop.properties";

    private final DecisionTreeRegistry decisionTreeRegistry;
    private final NodeRegistry nodeRegistry;

    public Moira(Collection<Node<?, ?>> nodes) {

        var config = MoiraConfig.getInstance();
        var properties = new PropertiesLoader(PROPERTIES_FILE).loadProperties(MoiraProperties.class);

        this.decisionTreeRegistry = config.decisionTreeRegistry(properties.getYamlFilesPath());
        this.nodeRegistry = config.nodeRegistry(nodes);
    }

    /**
     * Starts the execution of the decision tree asynchronously.
     *
     * @param decisionTreeId the ID of the decision tree to execute
     * @param context        the context to be passed to the decision nodes
     */
    public <C> void runAsync(String decisionTreeId, C context) {
        executeDecisionTree(decisionTreeId, context)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> logger.error("Error occurred while executing decision tree", error))
                .subscribe();
    }

    /**
     * Starts the execution of the decision tree and returns a Mono indicating completion.
     *
     * @param decisionTreeId the ID of the decision tree to execute
     * @param context        the context to be passed to the decision nodes
     * @return a Mono that completes when the execution of the decision tree is finished
     */
    public <C> Mono<Void> run(String decisionTreeId, C context) {
        return executeDecisionTree(decisionTreeId, context);
    }

    /**
     * Executes the decision tree starting with the specified ID and context.
     *
     * @param decisionTreeId the ID of the decision tree to execute
     * @param context        the context to be passed to the decision nodes
     * @return a Mono that completes when the execution of the decision tree is finished
     */
    private <C> Mono<Void> executeDecisionTree(String decisionTreeId, C context) {
        if (decisionTreeId == null) {
            throw new IllegalArgumentException("decisionTreeId cannot be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }

        DecisionTree decisionTree = decisionTreeRegistry.get(new DecisionTreeId(decisionTreeId))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Decision tree not found: %s with context type: %s",
                                decisionTreeId,
                                context.getClass().getName())
                ));

        var executionId = generateExecutionId();

        logger.debug("[{}] Starting execution of {}", executionId, decisionTreeId);
        return executeNode(decisionTree.start(), decisionTree, context, executionId);
    }

    private static String generateExecutionId() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private <C> Mono<Void> executeNode(NodeId nodeId, DecisionTree decisionTree, C context, String executionId) {
        return Mono.defer(() -> {
            Node<C, ?> node = (Node<C, ?>) nodeRegistry.get(nodeId, context.getClass())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Node not found: %s for context type: %s",
                                    nodeId.value(),
                                    context.getClass().getName())));

            var nodeIdValue = this.getNodeId(node);
            var nodeDescription = this.getNodeDescription(node);

            logger.debug("[{}] Executing <{}> ({}) with context: {}", executionId, nodeIdValue, nodeDescription, context);

            var result = node.execute(context);

            var nodeTransitions = decisionTree.transitionsByNode().get(nodeId).transitions();
            if (nodeTransitions.isEmpty()) {
                logger.debug("[{}] <{}> has ended execution successfully with context: {}", executionId, nodeIdValue, context);
                return Mono.empty();
            }

            logger.debug("[{}] <{}> decided result: {}", executionId, nodeIdValue, result);

            var nextNodeId = nodeTransitions.get(new DecisionNodeResult(result.name()));
            if (nextNodeId == null) {
                logger.error("[{}] No transition found for result: {}. Ending execution with error.", executionId, result);
                throw new RuntimeException(String.format("No transition found for result %s in %s during execution %s", result, nodeIdValue, executionId));
            }

            logger.debug("[{}] Transitioning to next node: <{}>", executionId, nextNodeId.value());
            return executeNode(nextNodeId, decisionTree, context, executionId);
        });
    }

    private String getNodeId(Node<?, ?> node) {
        var nodeClass = node.getClass();
        var decision = nodeClass.getAnnotation(Decision.class);
        return (decision != null) ? decision.id() : nodeClass.getSimpleName();
    }

    private String getNodeDescription(Node<?, ?> node) {
        var nodeClass = node.getClass();
        var decision = nodeClass.getAnnotation(Decision.class);
        return (decision != null) ? decision.description() : nodeClass.getSimpleName();
    }

}
