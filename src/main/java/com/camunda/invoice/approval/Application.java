package com.camunda.invoice.approval;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Application {
  private static final int LEAST_REQUEST_PARAMETERS_NUMBER = 2;

  private final HttpClientHelper httpClientHelper;

  public Application(HttpClientHelper httpClientHelper) {
    this.httpClientHelper = httpClientHelper;
  }

  public static void main(String[] args) {
    Application app = new Application(new HttpClientHelper());

    try {
      String[] argsArray = app.readParametersFromCommandLine(args, LEAST_REQUEST_PARAMETERS_NUMBER);

      BpmnModelInstance modelInstance = app.httpClientHelper.retrieveBpmnModelInstance();

      FlowNode startNode = app.getStartNode(modelInstance, argsArray[0]);
      FlowNode endNode = app.getEndNode(modelInstance, argsArray[1]);

      Graph graph = app.buildGraph(startNode, endNode.getId());
      graph.printOnePossiblePath(startNode.getId(), endNode.getId());

    } catch (Exception e) {
      System.out.println("exception => " + Arrays.toString(e.getStackTrace()));
      System.exit(-1);
    }
  }

  private String[] readParametersFromCommandLine(String[] args, int parametersNumber)
      throws ParseException {
    if (ObjectUtils.isEmpty(args)) {
      System.exit(-1);
    }

    CommandLine commandLine = new DefaultParser().parse(new Options(), args);

    String[] parsedArgs = commandLine.getArgs();

    if (parsedArgs.length < parametersNumber) {
      System.exit(-1);
    }

    return parsedArgs;
  }

  private FlowNode getStartNode(BpmnModelInstance modelInstance, String startId) {
    if (StringUtils.isEmpty(startId)) {
      System.exit(-1);
    }

    FlowNode startNode = modelInstance.getModelElementById(startId);
    if (startNode == null) {
      System.exit(-1);
    }
    return startNode;
  }

  private FlowNode getEndNode(BpmnModelInstance modelInstance, String endId) {
    if (StringUtils.isEmpty(endId)) {
      System.exit(-1);
    }
    FlowNode endNode = modelInstance.getModelElementById(endId);

    if (endNode == null) {
      System.exit(-1);
    }
    return endNode;
  }

  private Graph buildGraph(FlowNode startNode, String endId) {
    Map<String, Boolean> isVisitMaps = new HashMap<>();
    return buildGraphRecursiveUtil(new Graph(), startNode, endId, isVisitMaps);
  }

  private Graph buildGraphRecursiveUtil(
      Graph graph, FlowNode startNode, String endId, Map<String, Boolean> isVisited) {
    Collection<SequenceFlow> sequenceFlowCollection = startNode.getOutgoing();

    if ((isVisited.get(startNode.getId()) != null && isVisited.get(startNode.getId()))
        || ObjectUtils.isEmpty(sequenceFlowCollection)
        || startNode.getId().equals(endId)) {
      return graph;
    }

    isVisited.put(startNode.getId(), true);

    sequenceFlowCollection.forEach(
        sequenceFlow -> {
          graph.addEdge(startNode.getId(), sequenceFlow.getTarget().getId());
          buildGraphRecursiveUtil(graph, sequenceFlow.getTarget(), endId, isVisited);
        });

    return graph;
  }
}
