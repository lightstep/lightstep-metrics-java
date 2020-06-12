package com.lightstep.tracer.metrics;

import static com.lightstep.tracer.metrics.LightStepConstants.Tags.COMPONENT_NAME_KEY;
import static com.lightstep.tracer.metrics.LightStepConstants.Tags.SERVICE_VERSION_KEY;

import java.io.IOException;
import java.net.InetAddress;
import java.util.UUID;

import com.google.protobuf.Duration;
import com.google.protobuf.Timestamp;
import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;
import com.lightstep.tracer.grpc.KeyValue;
import com.lightstep.tracer.grpc.MetricKind;
import com.lightstep.tracer.grpc.MetricPoint;
import com.lightstep.tracer.grpc.Reporter;

abstract class ProtobufSender extends Sender<IngestRequest.Builder,IngestResponse> {
  private static final String JAVA_VERSION = System.getProperty("java.version");

  private final Reporter.Builder reporter;
  private final KeyValue.Builder[] labels;

  ProtobufSender(final String componentName, final String accessToken, final String serviceVersion,
            final String serviceUrl) {
    super(componentName, accessToken, serviceVersion, serviceUrl);

    final String hostname = getHostname();

    reporter = Reporter.newBuilder();
    reporter.addTags(KeyValue.newBuilder().setKey(COMPONENT_NAME_KEY).setStringValue(componentName));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.hostname").setStringValue(hostname));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.reporter_platform").setStringValue("java"));
    reporter.addTags(KeyValue.newBuilder().setKey("lightstep.reporter_platform_version").setStringValue(JAVA_VERSION));
    reporter.addTags(KeyValue.newBuilder().setKey(SERVICE_VERSION_KEY).setStringValue(serviceVersion));

    labels = new KeyValue.Builder[] {
      KeyValue.newBuilder().setKey(COMPONENT_NAME_KEY).setStringValue(componentName),
      KeyValue.newBuilder().setKey("lightstep.hostname").setStringValue(hostname),
      KeyValue.newBuilder().setKey(SERVICE_VERSION_KEY).setStringValue(serviceVersion)
    };
  }

  @Override
  final <V extends Number>void createMessage(final IngestRequest.Builder request,
      final long timestampSeconds, final long durationSeconds, final Metric<?,V> metric,
      final long current, final long previous) throws IOException {

    final MetricPoint.Builder builder = MetricPoint.newBuilder();
    builder.setMetricName(metric.getName());

    final Timestamp.Builder timestamp = Timestamp.newBuilder();
    timestamp.setSeconds(timestampSeconds);
    builder.setStart(timestamp);

    final Duration.Builder duration = Duration.newBuilder();
    duration.setSeconds(durationSeconds);
    builder.setDuration(duration);

    builder.setDoubleValue(metric.getValue(current, previous));
//    metric.getAdapter().setValue(builder, metric.compute(current, previous));

    if (metric instanceof CounterMetric)
      builder.setKind(MetricKind.COUNTER);
    else
      builder.setKind(MetricKind.GAUGE);

    // Add the predefined labels.
    for (int i = 0; i < labels.length; ++i) {
      builder.addLabels(labels[i]);
    }

    request.addPoints(builder.build());
  }

  private static String getHostname() {
    // FIXME: Technically, the following line is the proper "java way" to get
    // the hostname. However, this most always returns an internal IP address,
    // which may be incorrect for our needs?!
    try {
      return InetAddress.getLocalHost().getHostName();
    }
    catch (final IOException e) {
      return "";
    }
  }

  @Override
  IngestRequest.Builder newRequest() {
    return IngestRequest.newBuilder();
  }

  @Override
  IngestRequest.Builder setIdempotency(final IngestRequest.Builder request) {
    return request.setIdempotencyKey(UUID.randomUUID().toString());
  }

  @Override
  IngestRequest.Builder setReporter(final IngestRequest.Builder request) {
    return request.setReporter(reporter);
  }
}
