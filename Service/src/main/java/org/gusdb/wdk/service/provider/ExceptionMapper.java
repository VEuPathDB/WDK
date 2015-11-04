package org.gusdb.wdk.service.provider;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ParamException.PathParamException;
import org.gusdb.wdk.model.WdkUserException;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

  private static Logger LOG = Logger.getLogger(ExceptionMapper.class);

  @Override
  public Response toResponse(Exception e) {

    LOG.error(e.getMessage(), e);
    try { throw e; }

    // FIXME: Not sure why this is not catching (and then sending 404 as it should)
    catch (NotFoundException | PathParamException e404) {
      return Response.status(Status.NOT_FOUND)
          .type(MediaType.TEXT_PLAIN).entity("Not Found").build();
    }

    catch (WdkUserException e400) {
      return Response.status(Status.BAD_REQUEST)
          .type(MediaType.TEXT_PLAIN).entity("User error: " + e400.getMessage()).build();
    }

    catch (WebApplicationException eApp) {
      return eApp.getResponse();
    }

    catch (Exception other) {
      return Response.serverError()
          .type(MediaType.TEXT_PLAIN).entity("Internal Error").build();
    }
  }
}
