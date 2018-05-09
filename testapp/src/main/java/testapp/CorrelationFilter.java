package testapp;

import static testapp.CorrelationIdProducer.PARENT_SPAN_ID;
import static testapp.CorrelationIdProducer.SPAN_ID;
import static testapp.CorrelationIdProducer.TRACE_ID;

import java.io.IOException;
import java.util.UUID;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

@WebFilter(urlPatterns="/*")
public class CorrelationFilter implements Filter {

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }
  
  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) servletRequest;
    HttpServletResponse response = (HttpServletResponse) servletResponse;
    
    
    String traceId = servletRequest.getParameter(TRACE_ID);
    if (traceId == null) {
      traceId = request.getHeader(TRACE_ID);
    }

    if (traceId == null) {
      traceId = UUID.randomUUID().toString();
      System.err.println("Creating new CORRELATION ID : "+ traceId);
    } else {
      System.err.println("Received TRACE_ID : "+ traceId);
    }
    
    String parentSpanId = servletRequest.getParameter(SPAN_ID);
    if (parentSpanId == null) {
      parentSpanId = request.getHeader(SPAN_ID);
    }
    if (parentSpanId == null) {
      parentSpanId="";
    }    
    String newSpanId = UUID.randomUUID().toString();
    
    servletRequest.setAttribute(TRACE_ID, traceId);
    servletRequest.setAttribute(PARENT_SPAN_ID, parentSpanId);
    servletRequest.setAttribute(SPAN_ID, newSpanId);

    long start = System.currentTimeMillis();
    chain.doFilter(request, response);
    long end = System.currentTimeMillis();

    if (response.getStatus() >= 200 && response.getStatus() <300) {
      // ok
    } else {
      // error! should tell zipkin ...
    }
    
    JsonArrayBuilder spans = Json.createArrayBuilder();
    JsonObjectBuilder spanJson = Json.createObjectBuilder()
      .add("traceId", traceId.replaceAll("-", "").substring(0, 16))
      ;
    if (!"".equals(parentSpanId)) {
      spanJson.add("parentId", parentSpanId.replaceAll("-", "").substring(0, 16));
    }
    spanJson
      .add("id", newSpanId.replaceAll("-", "").substring(0, 16))
      .add("kind", "SERVER")
      .add("timestamp", 1000 * start)
      .add("duration", 1000 * (end-start))
      ;
    
    JsonObject localEndpoint = Json.createObjectBuilder()
        .add("serviceName", request.getRequestURI())
        .add("ipv4", request.getLocalAddr())
        .add("port", request.getLocalPort())
        .build()
    ;
    spanJson.add("localEndpoint", localEndpoint);
  
    spans.add(spanJson.build());
    JsonArray build = spans.build();

    System.err.println("Spans : ");
    System.err.println("-------");
    System.err.println(build.toString());
    System.err.println("-------");
    Response zipkinResponse = ClientBuilder.newClient().target("http://192.168.99.2:9411/")
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
