package org.gusdb.wdk.controller.service;

import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.gusdb.wdk.model.jspwrap.UserBean;

public class StrategyResource {

    @Context
    private UriInfo ui;

    private UserBean user;

    public StrategyResource(UserBean user) {
        this.user = user;
    }

    @GET
    @Produces("application/json")
    public String getStrategies() {
        
        
        return null;
    }
}
