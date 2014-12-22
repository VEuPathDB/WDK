package org.gusdb.wdk.service.provider;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

  private static Logger LOG = Logger.getLogger(ExceptionMapper.class);

  @Override
  public Response toResponse(Exception e) {
    LOG.error(e.getMessage(), e);
    if (e instanceof WebApplicationException) {
      return ((WebApplicationException)e).getResponse();
    }
    else {
      return Response.serverError()
          .entity("Internal Error")
          .type("text/plain")
          .build();
    }
  }
}
