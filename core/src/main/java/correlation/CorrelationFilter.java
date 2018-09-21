package correlation;

import static correlation.CorrelationHeaders.PARENT_SPAN_ID;
import static correlation.CorrelationHeaders.SPAN_ID;
import static correlation.CorrelationHeaders.TRACE_ID;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    // TODO how to indicate errors to zipkin in v2 ???
    if (response.getStatus() >= 200 && response.getStatus() <300) {
      // ok
    } else {
      // TODO error! should tell zipkin ...
    }
    
    
    SpanInfo.server()
      .withTraceId(traceId)
      .withParentSpanId(parentSpanId)
      .withSpanId(newSpanId)
      .startAt(start)
      .withDuration(end-start)
      .withServiceName(request.getRequestURI())
      .withIpv4(request.getLocalAddr())
      .withPort(request.getLocalPort())
      .sendTo(Configuration.zipkinURi());
  }

}
