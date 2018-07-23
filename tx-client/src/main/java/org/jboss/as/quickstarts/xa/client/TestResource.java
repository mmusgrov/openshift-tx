package org.jboss.as.quickstarts.xa.client;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;

import javax.enterprise.context.ApplicationScoped;

@Stateless
@ApplicationScoped
@Path("ejb")
public class TestResource {
    @EJB
    private TransactionalLocal localBean;

    @GET
    @Path("stateless/{arg}")
    @Produces("text/plain")
    public String testTransactionPresent(@Context SecurityContext context, @PathParam("arg") String arg) {
        return localBean.transactionStatus();
    }

    @GET
    @Path("stateful/{arg}")
    @Produces("text/plain")
    public String testSameTransactionEachCall(@Context SecurityContext context, @PathParam("arg") String arg) {
        return localBean.testSameTransactionEachCall();
    }

    @GET
    @Path("fault/{arg}")
    @Produces("text/plain")
    public String injectFault(@Context SecurityContext context, @PathParam("arg") String arg) {
        return localBean.injectFault(arg);
    }
}
