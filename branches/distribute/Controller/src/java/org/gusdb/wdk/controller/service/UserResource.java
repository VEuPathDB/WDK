package org.gusdb.wdk.controller.service;

import java.net.URI;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.controller.action.ActionUtility;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/user")
public class UserResource {

    @Context
    private ServletContext servletContext;
    @Context
    private UriInfo ui;

    /**
     * Get the information of the current user
     * 
     * @param signature
     * @return
     * @throws JSONException
     * @throws WdkUserException
     * @throws WdkModelException
     */
    @GET
    @Path("{signature}")
    @Produces("application/json")
    public String getUser(@PathParam("signature") String signature)
            throws JSONException, WdkUserException, WdkModelException {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);
        UserBean user = wdkModel.getUserFactory().getUser(signature);

        JSONObject jsUser = new JSONObject();
        jsUser.put("signature", user.getSignature());
        jsUser.put("is_guest", user.isGuest());

        // build the uri to the user
        UriBuilder ub = ui.getAbsolutePathBuilder();
        URI userUri = ub.path(user.getSignature()).build();
        jsUser.put("uri", userUri.toASCIIString());
        return jsUser.toString();
    }

    @Path("{signature}/strategy")
    public StrategyResource getStrategyResource(
            @PathParam("signature") String signature) throws WdkUserException,
            WdkModelException {
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servletContext);
        UserBean user = wdkModel.getUserFactory().getUser(signature);

        return new StrategyResource(user);
    }
}
