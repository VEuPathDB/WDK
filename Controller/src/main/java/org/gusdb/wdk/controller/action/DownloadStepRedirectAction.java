package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.model.WdkUserException;

public class DownloadStepRedirectAction extends Action {
	
	
	@Override
    public ActionForward execute( ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response )
            throws Exception {
		String stepIdstr = request.getParameter( "step_id" );
        if ( stepIdstr == null ) {
            stepIdstr = ( String ) request.getAttribute( "step_id" );
        }
        if ( stepIdstr != null ) {
          int stepId;
          try {
            stepId = Integer.parseInt( stepIdstr );
          } 
          catch(NumberFormatException ex) {
            throw new WdkUserException("The step id is invalid: " + stepIdstr);
          }
          ActionForward forward = new ActionForward("/app/step/" + stepId + "/download");
          forward.setRedirect(true);
          return forward;
        }
        else {
          throw new WdkUserException(
            "no step id is given for which to download the result" );
        }
	}
}
