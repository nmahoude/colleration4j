package testapp;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

public class CorrelationIdProducer {
  public static final String TRACE_ID = "TRACE_ID";
  public static final String SPAN_ID = "SPAN_ID";
  public static final String PARENT_SPAN_ID = "PARENT_SPAN_ID";
  
  @Inject
  private HttpServletRequest request;
  
  @Produces
  @TraceId
  public String getTraceId() {
    return (String)request.getAttribute(TRACE_ID);
  }
  
  @Produces
  @SpanId
  public String getSpanId() {
    return (String)request.getAttribute(SPAN_ID);
  }
  
  @Produces
  @ParentSpanId
  public String getParentSpanId() {
    return (String)request.getAttribute(PARENT_SPAN_ID);
  }
  
  @Produces
  public SpanInfo getSpanInfo() {
    SpanInfo si = new SpanInfo();
    si.traceId = getTraceId();
    si.parentSpanId = getParentSpanId();
    si.spanId = getSpanId();
    return si;
  }
}
