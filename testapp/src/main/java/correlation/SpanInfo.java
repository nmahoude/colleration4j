package correlation;

import javax.enterprise.inject.Vetoed;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@Vetoed
public class SpanInfo {
  String traceId;
  String parentSpanId;
  String spanId;
  private String ipv4;
  private int port;
  private String serviceName;
  private long start;
  private long duration;
  private String kind;
  
  public static SpanInfo client() {
    SpanInfo spanInfo = new SpanInfo();
    spanInfo.kind = "CLIENT";
    return spanInfo;
  }

  public static SpanInfo server() {
    SpanInfo spanInfo = new SpanInfo();
    spanInfo.kind = "SERVER";
    return spanInfo;
  }

  public SpanInfo withTraceId(String traceId) {
    this.traceId = traceId;
    return this;
  }

  public SpanInfo withParentSpanId(String parentSpanId) {
    this.parentSpanId = parentSpanId;
    return this;
  }

  public SpanInfo withSpanId(String spanId) {
    this.spanId = spanId;
    return this;
  }

  public SpanInfo withIpv4(String ipv4) {
    this.ipv4 = ipv4;
    return this;
  }

  public SpanInfo withPort(int port) {
    this.port = port;
    return this;
  }

  public SpanInfo withServiceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }
  
  public SpanInfo startAt(long start) {
    this.start = start;
    return this;
  }

  public SpanInfo withDuration(long duration) {
    this.duration = duration;
    return this;
  }

  public void sendTo(String zipkinUri) {
    JsonArrayBuilder spans = Json.createArrayBuilder();
    JsonObjectBuilder spanJson = Json.createObjectBuilder()
      .add("traceId", traceId.replaceAll("-", "").substring(0, 16))
      ;
    if (!"".equals(parentSpanId)) {
      spanJson.add("parentId", parentSpanId.replaceAll("-", "").substring(0, 16));
    }
    spanJson
      .add("id", spanId.replaceAll("-", "").substring(0, 16))
      .add("kind", kind)
      .add("timestamp", 1000 * start)
      .add("duration", 1000 * duration)
      ;
    
    JsonObject localEndpoint = Json.createObjectBuilder()
        .add("serviceName", serviceName)
        .add("ipv4", ipv4)
        .add("port", port)
        .build()
    ;
    spanJson.add("localEndpoint", localEndpoint);
  
    spans.add(spanJson.build());
    JsonArray build = spans.build();

    Response zipkinResponse = ClientBuilder.newClient().target(zipkinUri)
                .path("/api/v2/spans")
                .request().post(Entity.json(build));
    
    if (zipkinResponse.getStatus() >= 200 && zipkinResponse.getStatus() < 300) {
      System.err.println("Zipkin OK !");
    } else {
      System.err.println("Zipkin KO : " + zipkinResponse.getStatus());
      System.err.println(zipkinResponse.readEntity(String.class));
    }
  }
}
