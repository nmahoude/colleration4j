package testapp;

import java.util.List;
import java.util.Random;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
  // @CorrelationInfo(tags= { new Tag("func", "hello"))
  @Inject
  public String hello( CorrelationClientFilter filter) {
    try {
      Thread.sleep(new Random().nextInt(400));
    } catch (InterruptedException e) {
    }
    
    String result = ClientBuilder.newClient()
        .register(filter)
        .target("http://localhost:8080/")
        .path("jmetrics-app/second")
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
