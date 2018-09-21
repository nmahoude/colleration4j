package testapp;

import java.util.Random;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;

import correlation.CorrelationClientFilter;

@Path("/")
@Stateless
@Consumes("application/json")
public class EndPoint {
  
  @Inject CorrelationClientFilter filter;
  
  @GET
  @Path("first")
//   @CorrelationInfo(tags= { new Tag("func", "hello"))
  public String hello( ) {
    try {
      Thread.sleep(new Random().nextInt(400));
    } catch (InterruptedException e) {
    }
    
    String result = ClientBuilder.newClient()
        .register(filter)
        .target("http://localhost:8081/")
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
