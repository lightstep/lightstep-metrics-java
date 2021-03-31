# Lightstep Java Metrics Reporter

[ ![Download](https://img.shields.io/maven-central/v/com.lightstep.tracer/java-metrics-reporter.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.lightstep.tracer%20java-metrics-reporter) [![Circle CI](https://circleci.com/gh/lightstep/lightstep-metrics-java.svg?style=shield)](https://circleci.com/gh/lightstep/lightstep-metrics-java)

This library automatically reports a set of predefined host metrics
(such as CPU and memory usage) to the Lightstep backend.

Java 8 or newer is required. When running Java 7, no metrics reporting
will be performed.

Note: Using this artifact requires tracing being done in the same process
and sharing the same service name/version and access token. If not done,
the reported metrics will be ignored at the backend.

### Maven

```xml
<dependency>
   <groupId>com.lightstep.tracer</groupId>
   <artifactId>java-metrics-reporter</artifactId>
   <version>VERSION</version>
</dependency>
```

### Usage

```java
import com.lightstep.tracer.metrics.Metrics;
import com.lightstep.tracer.metrics.OkHttpSender;
import com.lightstep.tracer.metrics.Sender;
...
  // Done once, at application initialization.
  Sender<?,?> sender = new OkHttpSender(
      "MyServiceName", // Service/Component name.
      "MyAccessToken", // Access Token. Nullable
      "1.2.3", // Service version. Nullable
      "https://" // Metrics url
      30000 // connect timeout in milliseconds
  );

  // Metrics inherits from Thread.
  Metrics metrics = new Metrics(
      sender, 
      30 // sample period in seconds
  );

  metrics.setDaemon(true);
  metrics.start();

  // Done at application/reporter shutdown.
  metrics.close();
```
