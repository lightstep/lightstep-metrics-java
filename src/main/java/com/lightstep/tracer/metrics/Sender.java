package com.lightstep.tracer.metrics;

import java.io.IOException;

public abstract class Sender<I, O> implements AutoCloseable {
  protected final String componentName;
  protected final String accessToken;
  protected final String serviceVersion;
  protected final String serviceUrl;

  Sender(final String componentName, final String accessToken, final String serviceVersion,
            final String serviceUrl) {
    this.componentName = componentName;
    this.accessToken = accessToken;
    this.serviceVersion = serviceVersion;
    this.serviceUrl = serviceUrl;
  }

  abstract <V extends Number>void createMessage(I request, long timestampSeconds, long durationSeconds,
      Metric<?,V> metric, long current, long previous) throws IOException;
  abstract I newRequest();
  abstract I setIdempotency(I request);
  abstract I setReporter(I request);
  abstract O invoke(I request, long timeout) throws Exception;

  private I request;
  private long previousTimestamp = System.currentTimeMillis() / 1000;
  private boolean readyToReport;

  final O exec(final long timeout) throws Exception {
    final I request = getRequest();
    if (request == null) {
      throw new IllegalStateException("Request should not be null");
    }

    if (!readyToReport) {
      // First report duration is nearly 0 therefore it should be dropped
      readyToReport = true;
      setRequest(null);
      return null;
    }

    final O response = invoke(request, timeout);
    setRequest(null);
    return response;
  }

  final I getRequest() {
    return this.request;
  }

  final void setRequest(final I request) {
    this.request = request;
  }

  final void updateSampleRequest(final MetricGroup[] metricGroups) throws IOException {
    final long timestampSeconds = System.currentTimeMillis() / 1000;
    final long durationSeconds = timestampSeconds - this.previousTimestamp;
    this.previousTimestamp = timestampSeconds;

    final I request = setReporter(
        setIdempotency(this.request != null ? this.request : newRequest()));
    for (final MetricGroup metricGroup : metricGroups) {
      metricGroup.execute(this, request, timestampSeconds, durationSeconds);
    }

    this.request = request;
  }
}
