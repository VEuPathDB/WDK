package org.gusdb.wdk.controller.action.user;

import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.controller.AuthenticationService;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.controller.OpenIdUser;
import org.gusdb.wdk.controller.actionutil.ActionResult;
import org.gusdb.wdk.controller.actionutil.ParamDef;
import org.gusdb.wdk.controller.actionutil.ParamGroup;
import org.gusdb.wdk.controller.actionutil.ResponseType;
import org.gusdb.wdk.controller.actionutil.WdkAction;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.UserFactoryBean;

public class ProcessOpenIdAction extends WdkAction {

  private static final Logger LOG = Logger.getLogger(ProcessOpenIdAction.class.getName());
  
	@Override
	protected ResponseType getResponseType() {
		return ResponseType.html;
	}

	// choosing not to validate params here since most will be OpenId protocol-specific
	@Override protected boolean shouldValidateParams() { return false; }
	@Override protected Map<String, ParamDef> getParamDefs() { return null; }
	
	@Override
	protected ActionResult handleRequest(ParamGroup params) throws Exception {

	  LOG.info("handling open id response request; trying to log user in");
	  
	  // use authentication service to retrieve authenticated OpenID information from the provider
	  AuthenticationService auth = (AuthenticationService)
				getSessionAttribute(CConstants.WDK_OPENID_AUTH_SERVICE_KEY);
		RequestData req = getRequestData();
		OpenIdUser openIdUser = auth.verifyResponse(req.getRequestUrl(), req.getQueryString(), params.getParamMap());
		if (openIdUser == null) {
			throw new WdkUserException("Your OpenID could not be authenticated.  Please try again.");
		}
		
		// fetch user and assign to session
		UserBean guest = getCurrentUser();
		UserFactoryBean factory = getWdkModel().getUserFactory();
		UserBean user = factory.login(guest, openIdUser.getOpenId());
		if (user == null) {
		  // we should already have verified that a user exists with this OpenID
		  throw new WdkModelException("Previously discovered User cannot be found with OpenId " + openIdUser.getOpenId());
		}
		ProcessLoginAction.addLoginCookie(user, auth.rememberUser(), getWdkModel(), this);
		setCurrentUser(user);
		setSessionAttribute(CConstants.WDK_LOGIN_ERROR_KEY, "");
		
		// go back to user's original page after successful login
		return new ActionResult().setRedirect(true).setViewPath(auth.getReferringUrl());
	}

}
