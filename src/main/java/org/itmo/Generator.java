package org.itmo;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;


public class Generator {

  public static void main(String[] args) {
    // inputTimeline(false);
    // inputTimeline(true);
    resourcesTimeline();
  }

  private static void resourcesTimeline() {
    int vertexes = 1000000;
    int edges = 10000000;
    var mapper = new ObjectMapper();
    int p = Runtime.getRuntime().availableProcessors();
    int[] workers = IntStream.range(1, p + 1).toArray();;
    Random r = new Random(42);
    var metrics = new ArrayList<ResourcesTimelineItem>();
    try (
      FileWriter fw = new FileWriter("tmp/metrics/resourcesTimeline.json");
    ) {
      for (int worker : workers) {
        Graph g = new RandomGraphGenerator().generateGraph(r, vertexes, edges, worker);
        var time = executeParallelBfsAndGetTime(g);
        metrics.add(new ResourcesTimelineItem(worker, time));
      }

      fw.write(mapper.writeValueAsString(metrics));
      fw.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static void inputTimeline(boolean isParallel) {
    var mapper = new ObjectMapper();
    int[] sizes = new int[]{100_000, 200_000, 300_000, 400_000, 500_000, 600_000, 700_000, 800_000, 900_000, 1_000_000, 1_200_000, 1_400_000, 1_600_000, 1_800_000, 2_000_000};
    int[] connections = new int[]{300_000, 600_000, 900_000, 1_200_000, 1_500_000, 1_800_000, 2_100_000, 2_400_000, 2_700_000, 3_000_000, 3_600_000, 4_200_000, 4_800_000, 5_400_000, 6_000_000 };
    Random r = new Random(42);
    var metrics = new ArrayList<InputTimelineItem>();
    var fileName = isParallel ? "inputTimelineParallel" : "inputTimelineSerial";
    try (
      FileWriter fw = new FileWriter(String.format("tmp/metrics/%s.json", fileName));
    ) {
      for (int i = 0; i < sizes.length; i++) {
        Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
        Function<Graph, Long> timeSupplier = isParallel ? Generator::executeParallelBfsAndGetTime : Generator::executeSerialBfsAndGetTime;
        metrics.add(new InputTimelineItem(sizes[i], connections[i], timeSupplier.apply(g), isParallel));
      }

      fw.write(mapper.writeValueAsString(metrics));
      fw.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static Long executeSerialBfsAndGetTime(Graph g) {
    long startTime = System.currentTimeMillis();
    g.bfs(0);
    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }

  private static Long executeParallelBfsAndGetTime(Graph g) {
    long startTime = System.currentTimeMillis();
    g.parallelBFS(0);
    long endTime = System.currentTimeMillis();
    return endTime - startTime;
  }
}
