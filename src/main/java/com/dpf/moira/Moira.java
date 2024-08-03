package com.dpf.moira;

import com.dpf.moira.entity.*;
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

    private DecisionTreeRegistry decisionTreeRegistry;

    private final NodeRegistry nodeRegistry;

    private final boolean isHotReload;

    public Moira(Collection<Node<?, ?>> nodes) {

        var config = MoiraConfig.getInstance();

        this.decisionTreeRegistry = config.decisionTreeRegistry();
        this.nodeRegistry = config.nodeRegistry(nodes);
        this.isHotReload = config.getProperties().isHotReloadMode();
    }

    /**
     * Starts the execution of the decision tree asynchronously.
     *
     * @param decisionTreeId the ID of the decision tree to execute
     * @param context        the context to be passed to the decision nodes
     */
    public <C> void decideAsync(String decisionTreeId, C context) {
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
    public <C> Mono<Void> decide(String decisionTreeId, C context) {
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
        if (isHotReload) {
            var config = MoiraConfig.getInstance();
            this.decisionTreeRegistry = config.decisionTreeRegistry();
        }

        var decisionTree = decisionTreeRegistry.get(new DecisionTreeId(decisionTreeId))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Decision tree not found: %s with context type: %s",
                                decisionTreeId,
                                context.getClass().getName())
                ));

        var executionId = generateExecutionId();

        logger.debug("[{}] Starting execution of {}", executionId, decisionTreeId);
        return executeNode(decisionTree.start(), decisionTree, new Context<>(executionId, context));
    }

    private static String generateExecutionId() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private <C> Mono<Void> executeNode(NodeId nodeId, DecisionTree decisionTree, Context<C> context) {
        return Mono.defer(() -> {
            Node<C, ?> node = (Node<C, ?>) nodeRegistry.get(nodeId, context.get().getClass())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Node not found: %s for context type: %s",
                                    nodeId.value(),
                                    context.getClass().getName())));

            var nodeIdValue = this.getNodeId(node);
            var nodeDescription = this.getNodeDescription(node);

            logger.debug("[{}] Executing <{}> ({}) with context: {}", context.executionId(), nodeIdValue, nodeDescription, context.get());

            var result = node.execute(context);

            var nodeTransitions = decisionTree.transitionsByNode().get(nodeId).transitions();
            if (nodeTransitions.isEmpty()) {
                logger.debug("[{}] <{}> has ended execution successfully with context: {}", context.executionId(), nodeIdValue, context.get());
                return Mono.empty();
            }

            logger.debug("[{}] <{}> decided result: {}", context.executionId(), nodeIdValue, result);

            var nextNodeId = nodeTransitions.get(new DecisionNodeResult(result.name()));
            if (nextNodeId == null) {
                logger.error("[{}] No transition found for result: {}. Ending execution with error.", context.executionId(), result);
                throw new RuntimeException(String.format("No transition found for result %s in %s during execution %s", result, nodeIdValue, context.executionId()));
            }

            logger.debug("[{}] Transitioning to next node: <{}>", context.executionId(), nextNodeId.value());
            return executeNode(nextNodeId, decisionTree, context);
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
