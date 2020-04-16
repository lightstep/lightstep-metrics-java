package com.lightstep.tracer.metrics;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SafeMetrics {
  private static final Logger logger = LoggerFactory.getLogger(SafeMetrics.class);
  private static final boolean isJdk17 = System.getProperty("java.version").startsWith("1.7");

  private SafeMetrics() {}

  public static Metrics createMetricsThread(final String serviceName, final String accessToken,
            final String serviceVersion, final String serviceUrl, final int samplePeriodSeconds) {
    if (isJdk17) {
      logger.warn("Metrics supports jdk1.8+");
      return null;
    }

    // TODO: Can we unify samplePeriodSeconds in a single place?
    final Sender<?,?> sender = new OkHttpSender(serviceName, accessToken, serviceVersion,
                serviceUrl, samplePeriodSeconds * 1000, false);
    return new Metrics(sender, samplePeriodSeconds);
  }
}
