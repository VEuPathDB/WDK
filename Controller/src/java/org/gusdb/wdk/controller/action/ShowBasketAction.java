package org.gusdb.wdk.controller.action;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.RecordClassBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.user.BasketFactory;

/**
 * This action is called by the UI in order to "close" a strategy. It removes
 * the specified strategy id from the strategy id list stored in the session.
 */

public class ShowBasketAction extends Action {

    private static final String PARAM_RECORD_CLASS = "recordClass";
    private static final String MAPKEY_SHOW_BASKET = "showBasket";

    private static Logger logger = Logger.getLogger(ShowBasketAction.class);

    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.debug("Entering ShowBasketAction...");

        UserBean user = ActionUtility.getUser(servlet, request);
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String rcName = request.getParameter(PARAM_RECORD_CLASS);
            RecordClassBean recordClass = wdkModel.findRecordClass(rcName);
            QuestionBean question = recordClass.getRealtimeBasketQuestion();
            Map<String, String> params = new LinkedHashMap<String, String>();
            params.put(BasketFactory.PARAM_USER_SIGNATURE, user.getSignature());
            AnswerValueBean answerValue = question.makeAnswerValue(user, params);
            request.setAttribute(CConstants.WDK_ANSWER_KEY, answerValue);

            ActionForward forward = mapping.findForward(MAPKEY_SHOW_BASKET);
            forward.setRedirect(false);
            return forward;
        } catch (Exception ex) {
            logger.error(ex);
            ex.printStackTrace();
            throw ex;
        } finally {
            logger.debug("Leaving ShowBasketAction...");
        }
    }
}
