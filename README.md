# Lightstep Java Metrics Reporter

[ ![Download](https://api.bintray.com/packages/lightstep/maven/java-metrics/images/download.svg) ](https://bintray.com/lightstep/maven/) [![Circle CI](https://circleci.com/gh/lightstep/lightstep-metrics-java.svg?style=shield)](https://circleci.com/gh/lightstep/lightstep-metrics-java)

This library automatically reports a set of predefined host metrics
(such as CPU and memory usage) to the Lightstep backend.

Java 8 or newer is required. When running Java 7, no metrics reporting
will be performed.

Note: Using this artifact requires tracing being done in the same process
and sharing the same service name/version and access token. If not done,
the reported metrics will be ignored at the backend.

### Maven

```
<dependency>
   <groupId>com.lightstep.tracer</groupId>
   <artifactId>java-metrics-reporter</artifactId>
   <version>VERSION</version>
</dependency>
```

### Usage

```
import com.lightstep.tracer.metrics.Metrics;
import com.lightstep.tracer.metrics.SafeMetricsReporter;
...
  // Done once, at application initialization.
  Metrics metrics = SafeMetricsReporter.createMetricsThread(
      "MyServiceName", // Service/Component name
      "MyAccessToken",
      "1.2.3", // Service version
      "https://", // Service url
      30000, // Reporting period, in seconds.
  );

  // Metrics inherits from Thread.
  metrics.setDaemon(true);
  metrics.start();

  // Donce at application shutdown.
  metrics.close();
```
