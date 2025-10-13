package org.itmo;

public record InputTimelineItem(
  int v,
  int e,
  long time,
  boolean isParallel
){}
