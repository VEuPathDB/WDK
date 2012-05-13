package org.gusdb.wdk.controller.action;

import java.io.File;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.param.WdkEmptyEnumListException;

public class GetVocabAction extends Action {

    private static final Logger logger = Logger.getLogger(GetVocabAction.class);

    /*
     * (non-Javadoc)
     * 
     * @seeorg.apache.struts.action.Action#execute(org.apache.struts.action.
     * ActionMapping, org.apache.struts.action.ActionForm,
     * javax.servlet.http.HttpServletRequest,
     * javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        logger.trace("Entering GetVocabAction...");
        WdkModelBean wdkModel = ActionUtility.getWdkModel(servlet);
        try {
            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
            String paramName = request.getParameter("name");
            String dependedValue = request.getParameter("dependedValue");
            boolean getXml = Boolean.valueOf(request.getParameter("xml"));
            QuestionBean wdkQuestion = wdkModel.getQuestion(qFullName);
            EnumParamBean param = (EnumParamBean) wdkQuestion.getParamsMap().get(paramName);

            param.setDependedValue(dependedValue);

            // try the dependent value, and ignore empty list exception, since
            // it may be caused by the choices on the depended param.
            try {
                param.getDisplayMap();
            } catch (WdkEmptyEnumListException ex) {
                // do nothing.
                logger.debug("the choice of the depended param cause this: "
                        + ex);
            }

            request.setAttribute("vocabParam", param);

            String xmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR
                    + File.separator + CConstants.WDK_PAGES_DIR
                    + File.separator + "vocabXml.jsp";

            String htmlVocabFile = CConstants.WDK_DEFAULT_VIEW_DIR
                    + File.separator + CConstants.WDK_PAGES_DIR
                    + File.separator + "vocabHtml.jsp";

            ActionForward forward;

            if (getXml) {
                forward = new ActionForward(xmlVocabFile);
            } else {
                ShowQuestionAction.prepareQuestionForm(wdkQuestion,
                        getServlet(), request, (QuestionForm) form);
                forward = new ActionForward(htmlVocabFile);
            }

            logger.trace("Leaving GetVocabAction...");
            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
