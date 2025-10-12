package org.itmo;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record BfsActorContext(
  int frontierSize,
  int chunkSize,
  int chunkCount,
  CompletableFuture<List<Integer>>[] futures
) {}
