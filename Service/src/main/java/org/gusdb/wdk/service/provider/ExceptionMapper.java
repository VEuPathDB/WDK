package org.gusdb.wdk.service.provider;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
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

    // FIXME: Not sure why this is not catching (and then sending 404 as it should)
    catch (NotFoundException | PathParamException e404) {
      return Response.status(Status.NOT_FOUND)
          .type(MediaType.TEXT_PLAIN).entity("Not Found").build();
    }

    catch (JSONException | RequestMisformatException | WdkUserException e400) {
      String errorMsg = "Improperly formatted, incomplete, or incorrect service request body";
      return Response.status(Status.BAD_REQUEST)
          .type(MediaType.TEXT_PLAIN).entity(errorMsg + ": " + e400.getMessage()).build();
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
}
