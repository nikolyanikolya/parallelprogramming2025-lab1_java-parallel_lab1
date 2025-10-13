package org.itmo;

import org.openjdk.jcstress.annotations.*;
import org.openjdk.jcstress.infra.results.I_Result;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE;
import static org.openjdk.jcstress.annotations.Expect.ACCEPTABLE_INTERESTING;

@JCStressTest
@Outcome(id = "1", expect = ACCEPTABLE, desc = "All distances are calculated correctly")
@Outcome(id = "0", expect = ACCEPTABLE_INTERESTING, desc = "Some distances are calculated wrong")
@State
public class Bfs {
  final int poolSize = 3;
  final int startVertex = 1;
  final Graph graph = new RandomGraphGenerator().generateGraph(new Random(42), 5, 10, poolSize);
  List<Integer> frontiers;
  BfsActorContext context;
  AtomicBoolean finished = new AtomicBoolean(false);
  final Phaser phaser = new Phaser(poolSize + 1);

  {
    frontiers = graph.setUp(startVertex);
    context = graph.provideActorContext(frontiers);
  }

  @Actor
  public void mainActor() {
    try {
      while (!frontiers.isEmpty()) {
        phaser.arriveAndAwaitAdvance();
        phaser.arriveAndAwaitAdvance();
        frontiers = graph.combineFutures(context.futures());
        context = graph.provideActorContext(frontiers);
      }
    } finally {
      finished.set(true);
      phaser.arriveAndAwaitAdvance();
      phaser.arriveAndDeregister();
    }
  }

  @Actor
  public void poolActor0() {
    actor(0);
  }

  @Actor
  public void poolActor1() {
    actor(1);
  }

  @Actor
  public void poolActor2() {
    actor(2);
  }

  @Arbiter
  public void arbiter(I_Result r) {
    var parallelDist = Arrays.copyOf(graph.dist, graph.dist.length);
    var dist = graph.bfs(startVertex);

    r.r1 = (Arrays.equals(parallelDist, dist)) ? 1 : 0;
  }

  private void actor(int id) {
    while (true) {
      phaser.arriveAndAwaitAdvance();
      if (finished.get()) {
        phaser.arriveAndDeregister();
        break;
      }
      var chunkToCalculate = Math.min(Math.max(context.chunkCount() - 1, 0), id);
      graph.actor(frontiers, chunkToCalculate, context, Runnable::run).join();
      phaser.arriveAndAwaitAdvance();
    }
  }
}