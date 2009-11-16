package org.gusdb.wdk.controller.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.jspwrap.EnumParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.query.param.WdkEmptyEnumListException;

public class GetVocabAction extends ShowQuestionAction {

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
        try {
            String qFullName = request.getParameter(CConstants.QUESTION_FULLNAME_PARAM);
            String paramName = request.getParameter("name");
            String dependedValue = request.getParameter("dependedValue");
            boolean getXml = Boolean.valueOf(request.getParameter("xml"));
            QuestionBean wdkQuestion = getQuestionByFullName(qFullName);
            EnumParamBean param = (EnumParamBean) wdkQuestion.getParamsMap().get(
                    paramName);

            param.setDependedValue(dependedValue);

            // TO Charles: this step is unnecessary, the param map is always
            // recreated and returned, the value put into will be ignored. by
            // the way, you are changing a reference to Param object, and the
            // change will be carried over (that's why your code works) -
            // Jerric.
            wdkQuestion.getParamsMap().put(paramName, param);

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
            ActionForward forward;

            if (getXml) {
                forward = mapping.findForward("vocab_xml");
            } else {
                prepareQuestionForm(wdkQuestion, request, (QuestionForm) form);

                forward = mapping.findForward("vocab_html");
            }

            return forward;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }
}
