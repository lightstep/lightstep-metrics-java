package com.lightstep.tracer.metrics;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.lightstep.tracer.grpc.IngestRequest;
import com.lightstep.tracer.grpc.IngestResponse;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class OkHttpSender extends ProtobufSender {
  private static final String OCTET_STREAM_TYPE = "application/octet-stream";
  private static final MediaType protoMediaType = MediaType.parse(OCTET_STREAM_TYPE);
  private static final byte[] EMPTY_BUFFER = new byte[0];

  private final AtomicReference<OkHttpClient> client;
  private final URL collectorURL;
  private final long deadlineMillis;

  public OkHttpSender(final String componentName, final String accessToken, final String serviceVersion,
            final String serviceUrl, final int deadlineMillis) {
    super(componentName, accessToken, serviceVersion, serviceUrl);
    this.deadlineMillis = deadlineMillis;
    this.client = new AtomicReference<>(start(deadlineMillis));

    try {
      this.collectorURL = new URL(serviceUrl);
    }
    catch (final MalformedURLException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  IngestResponse invoke(final IngestRequest.Builder request, final long timeout) throws IOException {
    final Call call = client().newCall(new Request.Builder()
        .url(collectorURL)
        .addHeader("Accept", OCTET_STREAM_TYPE)
        .addHeader("Content-Type", OCTET_STREAM_TYPE)
        .addHeader("Lightstep-Access-Token", accessToken)
        .post(RequestBody.create(protoMediaType, request.build().toByteArray()))
      .build());

    call.timeout().deadline(timeout, TimeUnit.MILLISECONDS);
    try (Response response = call.execute()) {

      // Don't try to further process requests with 4xx/5xx (mostly).
      // TODO: 5xx errors should be retried properly.
      if (response.code() != 200) {
        return IngestResponse.parseFrom(EMPTY_BUFFER);
      }

      try (ResponseBody body = response.body()) {
        return IngestResponse.parseFrom(body.byteStream());
      }
    }
  }

  private OkHttpClient client() {
    return client.get();
  }

  private void reconnect() {
    close();
    client.set(start(deadlineMillis));
  }

  private static OkHttpClient start(final long deadlineMillis) {
    return new OkHttpClient.Builder().connectTimeout(deadlineMillis, TimeUnit.MILLISECONDS).build();
  }

  @Override
  public void close() {
    final OkHttpClient client = client();
    if (client != null)
      client.dispatcher().executorService().shutdown();
  }
}
