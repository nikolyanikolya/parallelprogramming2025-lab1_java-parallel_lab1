package org.itmo;

import kotlin.Pair;
import org.junit.jupiter.api.Test;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class BFSTest {

  @Test
  public void bfsTest() throws IOException {
    int[] sizes = new int[]{10, 100, 1000, 10_000, 10_000, 50_000, 100_000, 1_000_000, 2_000_000};
    int[] connections = new int[]{50, 500, 5000, 50_000, 100_000, 1_000_000, 1_000_000, 10_000_000, 10_000_000};
    Random r = new Random(42);
    try (
      FileWriter fw = new FileWriter("tmp/results.txt");
    ) {
      for (int i = 0; i < sizes.length; i++) {
        System.out.println("--------------------------");
        System.out.println("Generating graph of size " + sizes[i] + " ...wait");
        Graph g = new RandomGraphGenerator().generateGraph(r, sizes[i], connections[i]);
        System.out.println("Generation completed!\nStarting bfs");
        var serialTimedValue = executeSerialBfsAndGetTime(g);
        var serialTime = serialTimedValue.getFirst();
        var serialValue = serialTimedValue.getSecond();
        var parallelTimedValue = executeParallelBfsAndGetTime(g);
        var parallelTime = parallelTimedValue.getFirst();
        var parallelValue = parallelTimedValue.getSecond();
        assertThat(Arrays.equals(serialValue, parallelValue)).isTrue();
        fw.append("Times for " + sizes[i] + " vertices and " + connections[i] + " connections: ");
        fw.append("\nSerial: " + serialTime + " ms");
        fw.append("\nParallel: " + parallelTime + " ms");
        fw.append("\n--------\n");
      }
      fw.flush();
    }
  }


  private Pair<Long, int[]> executeSerialBfsAndGetTime(Graph g) {
    long startTime = System.currentTimeMillis();
    var res = g.bfs(0);
    long endTime = System.currentTimeMillis();
    return new Pair<>(endTime - startTime, res);
  }

  private Pair<Long, int[]> executeParallelBfsAndGetTime(Graph g) {
    long startTime = System.currentTimeMillis();
    var res = g.parallelBFS(0);
    long endTime = System.currentTimeMillis();
    return new Pair<>(endTime - startTime, res);
  }

}
