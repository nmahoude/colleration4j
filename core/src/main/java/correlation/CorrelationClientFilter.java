package correlation;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.ClientResponseContext;
import javax.ws.rs.client.ClientResponseFilter;

@RequestScoped
public class CorrelationClientFilter implements ClientRequestFilter, ClientResponseFilter{

  private final static ConcurrentHashMap<ClientRequestContext, Long> concurrentRequests = new ConcurrentHashMap<>();
  private String zipkinUri;
  
  @Inject  HttpServletRequest httpServletRequest;
  public CorrelationClientFilter() {
    this.zipkinUri = "http://192.168.99.2:9411/";
  }
  public CorrelationClientFilter(String zipkinUri, HttpServletRequest httpServletRequest) {
    this.zipkinUri = zipkinUri;
    this.httpServletRequest = httpServletRequest;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    registerRequest(requestContext);
    updateHeaders(requestContext);
  }
  
  private void updateHeaders(ClientRequestContext requestContext) {
    requestContext.getHeaders()
      .add(CorrelationHeaders.TRACE_ID, (String)httpServletRequest.getAttribute(CorrelationHeaders.TRACE_ID));
    requestContext.getHeaders()
      .add(CorrelationHeaders.PARENT_SPAN_ID, (String)httpServletRequest.getAttribute(CorrelationHeaders.PARENT_SPAN_ID));
    requestContext.getHeaders()
      .add(CorrelationHeaders.SPAN_ID, (String)httpServletRequest.getAttribute(CorrelationHeaders.SPAN_ID));
  }

  private void registerRequest(ClientRequestContext requestContext) {
    concurrentRequests.put(requestContext, System.currentTimeMillis());
  }

  @Override
  public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) throws IOException {
    Long start = concurrentRequests.remove(requestContext);
    if (start == null) {
      return;
    }
    long duration = (System.currentTimeMillis() - start);
    
    SpanInfo.client()
      .withTraceId(requestContext.getHeaderString(CorrelationHeaders.TRACE_ID))
      .withParentSpanId(requestContext.getHeaderString(CorrelationHeaders.PARENT_SPAN_ID))
      .withSpanId(requestContext.getHeaderString(CorrelationHeaders.SPAN_ID))
      .startAt(start)
      .withDuration(duration)
      .withIpv4(requestContext.getUri().getHost())
      .withPort(requestContext.getUri().getPort())
      .withServiceName(requestContext.getUri().getPath())
      .sendTo(zipkinUri);
  }

}
