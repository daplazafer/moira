package com.dpf.moira;

import com.dpf.moira.entity.DecisionNodeResult;
import com.dpf.moira.entity.NodeId;
import com.dpf.moira.entity.Workflow;
import com.dpf.moira.entity.WorkflowId;
import com.dpf.moira.properties.MoiraProperties;
import com.dpf.moira.properties.PropertiesLoader;
import com.dpf.moira.yaml.mapper.WorkFlowYmlMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Collection;
import java.util.UUID;

/**
 * Executes workflows.
 */
public final class Moira {

    private static final Logger logger = LoggerFactory.getLogger(Moira.class);

    private static final String PROPERTIES_FILE = "moira.properties";

    private final MoiraProperties properties;

    private WorkFlowRegistry workFlowRegistry;

    private final NodeRegistry nodeRegistry;

    private final ResourceLoader resourceLoader;

    private final boolean isHotReload;

    public Moira(Collection<Node<?, ?>> nodes) {

        this.properties = new PropertiesLoader(PROPERTIES_FILE).loadProperties(MoiraProperties.class);
        this.resourceLoader = new ResourceLoader();
        this.nodeRegistry = new NodeRegistry(nodes);
        this.workFlowRegistry = getWorkFlowRegistry();
        this.isHotReload = properties.isHotReloadMode();
    }

    private WorkFlowRegistry getWorkFlowRegistry() {
        var location = this.properties.getWorkflowFilesPath();
        var workflows = resourceLoader.loadWorkflows(location).stream()
                .map(WorkFlowYmlMapper::toEntity)
                .toList();
        return new WorkFlowRegistry(workflows);
    }

    /**
     * Starts the execution of the workflow asynchronously.
     *
     * @param workflow the ID of the workflow to execute
     * @param scenario the scenario to be passed to the decision nodes
     */
    public <S> void decideAsync(String workflow, S scenario) {
        executeWorkflow(workflow, scenario)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> logger.error("Error occurred while executing workflow {}", workflow, error))
                .subscribe();
    }

    /**
     * Starts the execution of the workflow and returns a Mono indicating completion.
     *
     * @param workflow the ID of the workflow to execute
     * @param scenario the scenario to be passed to the decision nodes
     * @return a Mono that completes when the execution of the workflow is finished
     */
    public <S> Mono<Void> decide(String workflow, S scenario) {
        return executeWorkflow(workflow, scenario);
    }

    /**
     * Executes the workflow starting with the specified ID and scenario.
     *
     * @param workflow the ID of the workflow to execute
     * @param scenario the scenario to be passed to the decision nodes
     * @return a Mono that completes when the execution of the workflow is finished
     */
    private <C> Mono<Void> executeWorkflow(String workflow, C scenario) {
        if (workflow == null) {
            throw new IllegalArgumentException("workflow cannot be null");
        }
        if (scenario == null) {
            throw new IllegalArgumentException("scenario cannot be null");
        }
        if (isHotReload) {
            this.workFlowRegistry = this.getWorkFlowRegistry();
        }

        var decisionTree = workFlowRegistry.get(new WorkflowId(workflow))
                .orElseThrow(() -> new IllegalArgumentException(
                        String.format("Workflow '%s' not found for scenario: %s",
                                workflow,
                                scenario.getClass().getName())
                ));

        var executionId = generateExecutionId();

        logger.debug("[{}] Starting execution of {}", executionId, workflow);
        return executeNode(decisionTree.start(), decisionTree, new Scenario<>(executionId, scenario));
    }

    private static String generateExecutionId() {
        return UUID.randomUUID().toString();
    }

    @SuppressWarnings("unchecked")
    private <S> Mono<Void> executeNode(NodeId nodeId, Workflow workFlow, Scenario<S> scenario) {
        return Mono.defer(() -> {
            Node<S, ?> node = (Node<S, ?>) nodeRegistry.get(nodeId, scenario.get().getClass())
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format("Node not found: %s for scenario: %s",
                                    nodeId.value(),
                                    scenario.getClass().getName())));

            var nodeIdValue = this.getNodeId(node);
            var nodeDescription = this.getNodeDescription(node);

            logger.debug("[{}] Executing <{}> ({}) with scenario: {}", scenario.getExecutionId(), nodeIdValue, nodeDescription, scenario.get());

            var result = node.execute(scenario);

            var nodeTransitions = workFlow.transitionsByNode().get(nodeId).transitions();
            if (nodeTransitions.isEmpty()) {
                logger.debug("[{}] <{}> has ended execution successfully with scenario: {}", scenario.getExecutionId(), nodeIdValue, scenario.get());
                return Mono.empty();
            }

            logger.debug("[{}] <{}> decided result: {}", scenario.getExecutionId(), nodeIdValue, result);

            var nextNodeId = nodeTransitions.get(new DecisionNodeResult(result.name()));
            if (nextNodeId == null) {
                logger.error("[{}] No transition found for result: {}. Ending execution with error.", scenario.getExecutionId(), result);
                throw new RuntimeException(String.format("No transition found for result %s in %s during execution %s", result, nodeIdValue, scenario.getExecutionId()));
            }

            logger.debug("[{}] Transitioning to next node: <{}>", scenario.getExecutionId(), nextNodeId.value());
            return executeNode(nextNodeId, workFlow, scenario);
        });
    }

    private String getNodeId(Node<?, ?> node) {
        var nodeClass = node.getClass();
        var decision = nodeClass.getAnnotation(Decision.class);
        return (decision != null)
                ? decision.id()
                : nodeClass.getSimpleName();
    }

    private String getNodeDescription(Node<?, ?> node) {
        var nodeClass = node.getClass();
        var decision = nodeClass.getAnnotation(Decision.class);
        return (decision != null && !decision.description().isBlank())
                ? decision.description()
                : getNodeId(node);
    }

}
