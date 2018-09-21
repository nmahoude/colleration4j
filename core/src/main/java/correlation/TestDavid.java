package correlation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.servlet.ServletContext;

@ApplicationScoped
public class TestDavid {

  public void initServletContext(@Observes @Initialized(ApplicationScoped.class) ServletContext context) {
    System.out.println("TEST TOTO");
  }
}
