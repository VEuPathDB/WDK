package org.gusdb.wdk.service.provider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.Provider;

import org.apache.log4j.Logger;
import org.glassfish.jersey.server.ParamException.PathParamException;
import org.gusdb.fgputil.events.Events;
import org.gusdb.wdk.errors.ServerErrorBundle;
import org.gusdb.wdk.errors.ErrorContext.ErrorLocation;
import org.gusdb.wdk.errors.ErrorContext;
import org.gusdb.wdk.events.ErrorEvent;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.service.request.exception.ConflictException;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.statustype.UnprocessableEntityStatusType;
import org.json.JSONException;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

  private static final Logger LOG = Logger.getLogger(ExceptionMapper.class);

  @Context HttpServletRequest req;
  @Context ServletContext context;

  @Override
  public Response toResponse(Exception e) {

    LOG.error("Caught service error", e);

    try { throw e; }

    catch (NotFoundException | PathParamException e404) {
      return Response.status(Status.NOT_FOUND)
          .type(MediaType.TEXT_PLAIN).entity(e404.getMessage()).build();
    }

    catch (ForbiddenException e403) {
      return Response.status(Status.FORBIDDEN)
          .type(MediaType.TEXT_PLAIN).entity(e403.getMessage()).build();
    }

    catch (JSONException | RequestMisformatException | BadRequestException e400) {
      return Response.status(Status.BAD_REQUEST)
          .type(MediaType.TEXT_PLAIN).entity(ExceptionMapper.createCompositeExceptionMessage(e400)).build();
    }

    catch (ConflictException e409) {
      return Response.status(Status.CONFLICT)
          .type(MediaType.TEXT_PLAIN).entity(e409.getMessage()).build();
    }

    // Custom exception to handle client content issues
    catch (DataValidationException | WdkUserException e422) {
      return Response.status(new UnprocessableEntityStatusType())
          .type(MediaType.TEXT_PLAIN).entity(ExceptionMapper.createCompositeExceptionMessage(e422)).build();
    }

    catch (WebApplicationException eApp) {
      if(eApp.getCause() != null && eApp.getCause() instanceof Exception ) {
        return this.toResponse((Exception) eApp.getCause());
      }
      else {
        return Response.status(eApp.getResponse().getStatus())
          .type(MediaType.TEXT_PLAIN).entity(eApp.getMessage()).build();
      }
    }

    // Some other exception that must be handled by the application; send error event
    catch (Exception other) {
      WdkModel wdkModel = (WdkModel)context.getAttribute(Utilities.WDK_MODEL_KEY);
      ErrorContext errorContext = AbstractWdkService.getErrorContext(context, req, wdkModel, ErrorLocation.WDK_SERVICE);
      LOG.error("log4j marker: " + errorContext.getLogMarker());
      Events.trigger(new ErrorEvent(new ServerErrorBundle(other), errorContext));
      return Response.serverError()
          .type(MediaType.TEXT_PLAIN).entity("Internal Error").build();
    }
  }

  /**
   * Unwinds the exception stack, pulling out and assembling into one message, all
   * the exception messages but only if a JSONException or a WdkUserException exists
   * somewhere within the stack.  The unwinding stops when either exception is found.
   * If neither exception is found, the message of the top level exception only is
   * returned.  The underlying JSONException and WdkUserException message are potentially
   * very informative.  This method is protection against a developer who neglects
   * to bubble up that useful information.
   * @param e = top level exception
   * @return - string of concatenated exception messages
   */
  private static String createCompositeExceptionMessage(Exception e) {
    StringBuilder messages = new StringBuilder(e.getMessage() + System.lineSeparator());
    String compositeMessage = e.getMessage();
    Throwable t;
    Throwable descendent = e;
    boolean unwindable = false;
    while((t = descendent.getCause()) != null) {
      if(t.getMessage() != null) {
        messages.append(t.getMessage() + System.lineSeparator());
      }
      if(t instanceof JSONException || t instanceof WdkUserException) {
        unwindable = true;
        break;
      }
      descendent = t;
    }
    if(unwindable) {
      compositeMessage = messages.toString();
    }
    return compositeMessage;
  }
}
