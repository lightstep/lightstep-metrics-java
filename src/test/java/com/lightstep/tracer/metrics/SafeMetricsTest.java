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

    assertNull(SafeMetrics.createMetricsThread("Service", "https://", "accesstoken", "1.2.3"));
  }

  @Test(expected = NullPointerException.class)
  public void testNullServiceName() {
    SafeMetrics.createMetricsThread(null, "https://", "accesstoken", "1.2.3");
  }

  @Test(expected = NullPointerException.class)
  public void testNullMetricsUrl() {
    SafeMetrics.createMetricsThread("Service", null, "accesstoken", "1.2.3");
  }

  @Test
  public void testNullAccessToken() {
    Metrics m = SafeMetrics.createMetricsThread("Service", "https://", null, "1.2.3");
    assertNotNull(m);
  }

  @Test
  public void testNullServiceVersion() {
    Metrics m = SafeMetrics.createMetricsThread("Service", "https://", "accesstoken", null);
    assertNotNull(m);
  }
}
