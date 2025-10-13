package org.itmo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class Graph {
  private final int V;
  private final int P;
  private final ArrayList<Integer>[] adjList;
  private final boolean[] visited;
  int[] dist;

  Graph(int vertices) {
    this(vertices, Runtime.getRuntime().availableProcessors());
  }

  Graph(int vertices, int P) {
    V = vertices;
    this.P = P;
    adjList = new ArrayList[vertices];
    visited = new boolean[V];
    dist = new int[V];
    for (int i = 0; i < vertices; ++i) {
      adjList[i] = new ArrayList<>();
    }
  }

  void addEdge(int src, int dest) {
    if (!adjList[src].contains(dest)) {
      adjList[src].add(dest);
    }
  }

  int[] parallelBFS(int startVertex) {
    try (
      var pool = Executors.newFixedThreadPool(P);
    ) {
      var frontiers = setUp(startVertex);

      while (!frontiers.isEmpty()) {
        frontiers = combineFutures(bfsStep(frontiers, pool));
      }
      return dist;
    }
  }

  List<Integer> setUp(int startVertex) {
    Arrays.fill(dist, -1);
    Arrays.fill(visited,false);
    List<Integer> frontiers = List.of(startVertex);
    visited[startVertex] = true;
    dist[startVertex] = 0;

    return frontiers;
  }

  CompletableFuture<List<Integer>>[] bfsStep(List<Integer> frontiers, Executor pool) {
    var actorContext = provideActorContext(frontiers);
    for (int i = 0; i < actorContext.chunkCount(); i++) {
      actor(frontiers, i, actorContext, pool);
    }
    return actorContext.futures();
  }

  BfsActorContext provideActorContext(List<Integer> frontiers) {
    int frontierSize = frontiers.size();
    var chunkSize = (frontierSize + P) / P;
    var chunkCount = (int) Math.ceil((double) frontierSize / chunkSize);
    CompletableFuture<List<Integer>>[] futures = new CompletableFuture[chunkCount];
    return new BfsActorContext(frontierSize, chunkSize, chunkCount, futures);
  }

  CompletableFuture<List<Integer>> actor(
    List<Integer> frontiers,
    int chunkIndex,
    BfsActorContext actorContext,
    Executor pool
  ) {
    var futures = actorContext.futures();
    futures[chunkIndex] = CompletableFuture.supplyAsync(() -> {
      int chunkSize = actorContext.chunkSize();
      int frontierSize = actorContext.frontierSize();
      int from = chunkIndex * chunkSize;
      int to = Math.min(from + chunkSize, frontierSize);
      List<Integer> nextLocalFrontiers = new ArrayList<>();
      for (int j = from; j < to; j++) {
        int vertex = frontiers.get(j);
        for (var neighbour : adjList[vertex]) {
          if (!visited[neighbour]) {
            visited[neighbour] = true;
            nextLocalFrontiers.add(neighbour);
            dist[neighbour] = dist[vertex] + 1;
          }
        }
      }
      return nextLocalFrontiers;
    }, pool);
    return futures[chunkIndex];
  }

  List<Integer> combineFutures(CompletableFuture<List<Integer>>[] futures) {
    CompletableFuture.allOf(futures).join();
    return Arrays.stream(futures)
          .flatMap(future -> future.join().stream())
          .toList();
  }

  int[] bfs(int startVertex) {
    boolean[] visited = new boolean[V];
    Arrays.fill(dist, -1);

    LinkedList<Integer> queue = new LinkedList<>();

    visited[startVertex] = true;
    queue.add(startVertex);
    dist[startVertex] = 0;

    while (!queue.isEmpty()) {
      startVertex = queue.poll();

      for (int n : adjList[startVertex]) {
        if (!visited[n]) {
          visited[n] = true;
          dist[n] = dist[startVertex] + 1;
          queue.add(n);
        }
      }
    }

    return dist;
  }

}
