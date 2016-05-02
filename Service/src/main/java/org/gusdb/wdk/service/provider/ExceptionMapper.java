package org.gusdb.wdk.service.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.service.error.ErrorContext;
import org.gusdb.wdk.service.error.ErrorHandler;
import org.gusdb.wdk.service.error.ValueMaps;
import org.gusdb.wdk.service.error.ValueMaps.RequestAttributeValueMap;
import org.gusdb.wdk.service.error.ValueMaps.ServletContextValueMap;
import org.gusdb.wdk.service.error.ValueMaps.SessionAttributeValueMap;
import org.gusdb.wdk.service.request.ConflictException;
import org.gusdb.wdk.service.request.DataValidationException;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.json.JSONException;

@Provider
public class ExceptionMapper implements javax.ws.rs.ext.ExceptionMapper<Exception> {

  private static Logger LOG = Logger.getLogger(ExceptionMapper.class);
  
  //file defining error filters
  private static final String FILTER_FILE = "/WEB-INF/wdk-model/config/errorsTag.filter";
  
  @Context HttpServletRequest req;
  @Context ServletContext context;
  
  @Override
  public Response toResponse(Exception e) {

    LOG.error(e.getMessage(), e);
    try { throw e; }

    catch (NotFoundException | PathParamException e404) {
      return Response.status(Status.NOT_FOUND)
          .type(MediaType.TEXT_PLAIN).entity("Not Found").build();
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
          .type(MediaType.TEXT_PLAIN).entity(e422.getMessage()).build();
    }

    catch (WebApplicationException eApp) {
      return eApp.getResponse();
    }
    
    // Added email to site admins of data for exceptions not caught by filter
    catch (Exception other) {
      WdkModel wdkModel = ((WdkModelBean) context.getAttribute("wdkModel")).getModel();
      ErrorHandler handler = new ErrorHandler(other, getFilters(), getErrorContext(context, req, wdkModel));
      handler.handleErrors();
      return Response.serverError()
          .type(MediaType.TEXT_PLAIN).entity("Internal Error").build();
    }
  }
  
  /**
   * Loads filters from config file into Properties object
   * @return properties object containing filters
   * @throws IOException if unable to load filters
   */
  protected Properties getFilters() {
      Properties filters = new Properties();
      try(InputStream is = context.getResourceAsStream(FILTER_FILE)) {
        if (is != null) {
          filters.load(is);
        }
      }
      catch(IOException ioe) {
        ioe.printStackTrace();
      }
      return filters;
  }
  
  
  /**
   * Aggregate environment context data into an object for easy referencing
   * @param context current servlet context
   * @param request current HTTP servlet request
   * @param wdkModel this WDK Model
   * @return context data for this error
   */
  private static ErrorContext getErrorContext(ServletContext context,
          HttpServletRequest request, WdkModel wdkModel) {
    return new ErrorContext(
      wdkModel,
      context.getInitParameter("model"),
      request,
      ValueMaps.toMap(new ServletContextValueMap(context)),
      ValueMaps.toMap(new RequestAttributeValueMap(request)),
      ValueMaps.toMap(new SessionAttributeValueMap(request.getSession())));
  }
  
  /**
   * Unwinds the exception stack, pulling out and assembling into one message, all
   * the exception messages but only if a JSONException or a WdkUserException exists
   * somewhere within the stack.  The unwinding stops when either exception is found.
   * If neither exception is found, the message of the top leve exception only is
   * returned.  The underlying JSOMException and WdkUserException message are potentially
   * very informative.  This method is protection against a developer who neglects
   * to bubble up that useful information.
   * @param e = top level exception
   * @return - string of concatenated exception messages
   */
  protected static String createCompositeExceptionMessage(Exception e) {
    StringBuilder messages = new StringBuilder(e.getMessage() + System.lineSeparator());
    String compositeMessage = e.getMessage();
    Throwable t = null;
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
