package org.gusdb.wdk.controller.service;

import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;
import org.json.JSONObject;

import com.sun.jersey.spi.container.servlet.PerSession;

@Path("/user")
@PerSession
public class UserResource {

    @Context
    private ServletContext servletContext;

    /**
     * Get the information of the current user
     * 
     * @param request
     * @param ui
     * @return
     * @throws JSONException
     * @throws WdkUserException
     */
    @GET
    public String getUser(@Context HttpServletRequest request,
            @Context UriInfo ui) throws JSONException, WdkUserException {
        // get current user
        UserBean user = ActionUtility.getUser(servletContext, request);
        return outputUser(ui, user);
    }

    @POST
    public String login(@Context HttpServletRequest request,
            @Context UriInfo ui, @FormParam("email") String email,
            @FormParam("password") String password) throws WdkUserException,
            JSONException, WdkModelException, NoSuchAlgorithmException,
            SQLException {
        // get current user
        UserBean user = ActionUtility.getUser(servletContext, request);
        if (!user.isGuest())
            throw new WdkUserException("The current user is already logged in."
                    + " You need to logout first before logging in as another "
                    + "user.");

        // login the user
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);
        user = wdkModel.getUserFactory().login(user, email, password);

        return outputUser(ui, user);
    }

    @Path("/{signature}/logout")
    public void logout(@Context HttpServletRequest request,
            @Context UriInfo ui, @PathParam("signature") String signature)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException {
        // get current user
        UserBean user = ActionUtility.getUser(servletContext, request);
        if (user.isGuest()) return;

        // replace current user with a guest
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);
        UserBean guest = wdkModel.getUserFactory().getGuestUser();
        request.getSession().setAttribute(CConstants.WDK_USER_KEY, guest);
    }

    private String outputUser(@Context UriInfo ui, UserBean user)
            throws JSONException, WdkUserException {
        JSONObject jsUser = new JSONObject();
        jsUser.put("signature", user.getSignature());
        jsUser.put("is_guest", user.isGuest());

        // build the uri to the user
        UriBuilder ub = ui.getAbsolutePathBuilder();
        URI userUri = ub.path(user.getSignature()).build();
        jsUser.put("uri", userUri.toASCIIString());
        return jsUser.toString();
    }
}
