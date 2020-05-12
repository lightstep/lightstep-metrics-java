package com.lightstep.tracer.metrics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class SafeMetricsTest {
  @Test
  public void testJava7() {
    if (!System.getProperty("java.version").startsWith("1.7")) {
      System.err.println("This test is meant to be run with jdk1.7");
      return;
    }

    assertNull(SafeMetrics.createMetricsThread(null, null, null, null));
  }

  @Test
  public void testNullAccessToken() {
    Metrics m = SafeMetrics.createMetricsThread("Service", null, "1.2.3", "https://");
    assertNotNull(m);
  }

  @Test
  public void testNullServiceVersion() {
    Metrics m = SafeMetrics.createMetricsThread("Service", "accesstoken", null, "https://");
    assertNotNull(m);
  }
}
