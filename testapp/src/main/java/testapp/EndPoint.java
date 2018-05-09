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

import correlation.CorrelationClientFilter;

@Path("/")
@Stateless
public class EndPoint {
  
  private static final String zipkinURI = "http://192.168.99.2:9411/";

  @GET
  public String hello(@Context HttpServletRequest request) {
    try {
      Thread.sleep(new Random().nextInt(400));
    } catch (InterruptedException e) {
    }
    
    String result = ClientBuilder.newClient()
        .register(new CorrelationClientFilter(zipkinURI, request))
        .target("http://localhost:8080/")
        .path("test-app/second")
        .request()
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
    
    return "Hello from second call !";
  }
}
