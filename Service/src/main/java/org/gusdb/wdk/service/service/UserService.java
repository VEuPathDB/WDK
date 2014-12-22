package org.gusdb.wdk.service.service;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.User;

@Path("/user")
public class UserService {

  private WdkModel _wdkModel;

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public User getById(@PathParam("id") int userId) throws WdkModelException {
    return _wdkModel.getUserFactory().getUser(userId);
  }

  @Context
  public void setServletContext(ServletContext context) {
    _wdkModel = ((WdkModelBean)context.getAttribute("wdkModel")).getModel();
  }
}
