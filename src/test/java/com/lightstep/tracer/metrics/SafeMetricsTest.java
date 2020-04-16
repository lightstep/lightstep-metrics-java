package com.lightstep.tracer.metrics;

import static org.junit.Assert.*;

import org.junit.Test;

public class SafeMetricsTest {
  @Test
  public void test() {
    if (!System.getProperty("java.version").startsWith("1.7")) {
      System.err.println("This test is meant to be run with jdk1.7");
      return;
    }

    assertNull(SafeMetrics.createMetricsThread(null, null, null, null, 60));
  }
}
