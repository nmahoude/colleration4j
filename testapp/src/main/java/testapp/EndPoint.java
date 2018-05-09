package testapp;

import java.util.List;
import java.util.Random;

import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

@Path("/")
@Stateless
public class EndPoint {
  
  @GET
  public String hello(@Context HttpServletRequest request) {
    try {
      Thread.sleep(new Random().nextInt(400));
    } catch (InterruptedException e) {
    }
    
    String result = ClientBuilder.newClient().target("http://localhost:8080/")
        .path("test-app/second")
        .request()
        .header(CorrelationIdProducer.TRACE_ID, (String)request.getAttribute(CorrelationIdProducer.TRACE_ID))
        .header(CorrelationIdProducer.PARENT_SPAN_ID, (String)request.getAttribute(CorrelationIdProducer.PARENT_SPAN_ID))
        .header(CorrelationIdProducer.SPAN_ID, (String)request.getAttribute(CorrelationIdProducer.SPAN_ID))
        .get(String.class);
    return "result for hello : " + result;
  }
  
  @GET
  @Path("/second")
  public String helloFromTwo(@Context HttpHeaders headers) {
    try {
      Thread.sleep(100 + new Random().nextInt(900));
    } catch (InterruptedException e) {
    }
    
    for (String header : headers.getRequestHeaders().keySet()) {
      List<String> value = headers.getRequestHeader(header);
      System.out.println("header : " + header + " -> "+ value);
    }
    return "Hello from second call !";
  }
}
