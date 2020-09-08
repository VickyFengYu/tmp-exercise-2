package com.camunda.invoice.approval;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Graph {
  private Map<String, ArrayList<String>> adjacencyList;
  private List<List<String>> possiblePathsList;

  public Graph() {
    initializeGraphTraverseHelper();
  }

  private void initializeGraphTraverseHelper() {
    adjacencyList = new HashMap<>();
    possiblePathsList = new ArrayList<>();
  }

  public void addEdge(String source, String dest) {
    ArrayList<String> tempList = adjacencyList.getOrDefault(source, new ArrayList<>());

    if (!tempList.contains(dest)) {
      tempList.add(dest);
    }

    adjacencyList.put(source, tempList);
    // System.out.println("addEdge => " + source + " : " + adjacencyList.get(source));
  }

  public void printOnePossiblePath(String source, String dest) {
    printAllPaths(source, dest, false);

    if (possiblePathsList.get(0).isEmpty()) {
      System.exit(-1);
    }

    System.out.println(
        "The path from approveInvoice to invoiceProcessed is: " + possiblePathsList.get(0));
  }

  public void printAllPaths(String source, String dest, boolean allPaths) {
    Map<String, Boolean> isVisited = new HashMap<>();
    ArrayList<String> tempPathList = new ArrayList<>();

    tempPathList.add(source);

    printAllPathsRecursiveUtil(source, dest, isVisited, tempPathList, allPaths);
    // System.out.println("possibleOPathsList=> " + possibleOPathsList);

    if (possiblePathsList.isEmpty()) {
      System.exit(-1);
    }
  }

  private void printAllPathsRecursiveUtil(
      String source,
      String dest,
      Map<String, Boolean> isVisited,
      List<String> tempPathList,
      boolean allPaths) {

    if (!possiblePathsList.isEmpty() && !allPaths) {
      return;
    }

    isVisited.put(source, true);

    if (source.equals(dest)) {
      //  System.out.println("valid tempPathList => " + tempPathList);
      possiblePathsList.add(new ArrayList<>());
      possiblePathsList.get(possiblePathsList.size() - 1).addAll(tempPathList);
      isVisited.put(source, false);
      return;
    }

    adjacencyList
        .getOrDefault(source, new ArrayList<>())
        .forEach(
            adjacency -> {
              if (isVisited.get(adjacency) == null || !isVisited.get(adjacency)) {
                tempPathList.add(adjacency);
                printAllPathsRecursiveUtil(adjacency, dest, isVisited, tempPathList, allPaths);

                tempPathList.remove(adjacency);
              }
            });

    isVisited.put(source, false);
  }
}
