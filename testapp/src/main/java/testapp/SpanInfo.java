package testapp;

import javax.enterprise.inject.Vetoed;

@Vetoed
public class SpanInfo {
  String traceId;
  String parentSpanId;
  String spanId;
}
