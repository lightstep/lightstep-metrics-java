package com.lightstep.tracer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  private static final int DEFAULT_SAMPLE_PERIOD_SEC = 30;

  private SafeMetrics() {}

  public static Metrics createMetricsThread(String serviceName, String accessToken,
            String serviceVersion, String metricsUrl) {

    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    if (accessToken == null) {
      accessToken = "";
    }

    if (serviceVersion == null) {
      serviceVersion = "";
    }

    final Sender<?,?> sender = new OkHttpSender(serviceName, accessToken, serviceVersion,
                metricsUrl, DEFAULT_SAMPLE_PERIOD_SEC * 1000, false);
    return new Metrics(sender, DEFAULT_SAMPLE_PERIOD_SEC);
  }
}
