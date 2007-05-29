package org.gusdb.wdk.controller.action;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.DatasetParamBean;
import org.gusdb.wdk.model.jspwrap.FlatVocabParamBean;
import org.gusdb.wdk.model.jspwrap.HistoryParamBean;
import org.gusdb.wdk.model.jspwrap.ParamBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.QuestionSetBean;
import org.gusdb.wdk.model.jspwrap.UserBean;
import org.gusdb.wdk.model.jspwrap.WdkModelBean;

/**
 * form bean for showing a wdk question from a question set
 */

public class QuestionForm extends QuestionSetForm {

    /**
     * 
     */
    private static final long serialVersionUID = -7848685794514383434L;
    private QuestionBean question = null;
    private boolean validating = true;
    private boolean paramsFilled = false;

    public void reset() {
        super.reset();
        resetMappedProps();
    }

    /**
     * validate the properties that have been sent from the HTTP request, and
     * return an ActionErrors object that encapsulates any validation errors
     */
    public ActionErrors validate(ActionMapping mapping,
            HttpServletRequest request) {
        // set the question name into request
        request.setAttribute(CConstants.QUESTION_FULLNAME_PARAM, qFullName);
  
        ActionErrors errors = new ActionErrors();
        if (!validating) {
            return errors;
        }

        String clicked = request.getParameter(CConstants.PQ_SUBMIT_KEY);
        if (clicked != null
                && clicked.equals(CConstants.PQ_SUBMIT_EXPAND_QUERY)) {
            return errors;
        }

        QuestionBean wdkQuestion = getQuestion();
        if (wdkQuestion == null) {
            return errors;
        }

        ParamBean[] params = wdkQuestion.getParams();
        for (int i = 0; i < params.length; i++) {
            ParamBean p = params[i];
            try {
                String[] pVals = null;
                if (p instanceof FlatVocabParamBean
                        || p instanceof HistoryParamBean) {
                    pVals = getMyMultiProp(p.getName());
                    if (pVals == null) {
                        pVals = new String[]{ "" };
                    }
                } else {
                    pVals = new String[]{ getMyProp(p.getName()) };
                }

                String errMsg = null;
                for (int j = 0; j < pVals.length; j++) {
                    String oneMsg = p.validateValue(pVals[j]);
                    if (oneMsg != null) {
                        if (errMsg == null) errMsg = oneMsg;
                        else errMsg += "; " + oneMsg;
                    }
                }
                if (errMsg != null) {
                    errors.add(ActionErrors.GLOBAL_ERROR, new ActionError(
                            "mapped.properties", p.getPrompt(), errMsg));
                    request.setAttribute( CConstants.QUESTIONSETFORM_KEY, this );
                }
                // System.out.println("===== Validated " + p.getName() + ": '" + errMsg + "'");
            } catch (WdkModelException exp) {
                errors.add(ActionErrors.GLOBAL_MESSAGE, new ActionError(
                        "mapped.properties", p.getPrompt(), exp.getMessage()));
                request.setAttribute( CConstants.QUESTIONSETFORM_KEY, this );
            }
        }
        return errors;
    }

    public void cleanup() {
        Iterator it = getMyProps().keySet().iterator();
        Vector<String> v = new Vector<String>();
        while (it.hasNext()) {
            String key = (String) it.next();
            if (getQuestion() != null
                    && key.indexOf("_" + getQuestion().getName() + "_") > 0) {
                v.add(key);
            }
        }
        String[] extraKeys = new String[v.size()];
        v.toArray(extraKeys);
        for (int i = 0; i < extraKeys.length; i++) {
            getMyProps().remove(extraKeys[i]);
        }
    }

    public void setQuestion(QuestionBean s) {
        question = s;
    }

    public QuestionBean getQuestion() {
        if (question == null) {
            if (qFullName == null) return null;
            int dotI = qFullName.indexOf( '.' );
            String qSetName = qFullName.substring( 0, dotI );
            String qName = qFullName.substring( dotI + 1, qFullName.length() );
        
            WdkModelBean wdkModel = ( WdkModelBean ) getServlet().getServletContext().getAttribute(
                    CConstants.WDK_MODEL_KEY );
        
            QuestionSetBean wdkQuestionSet = ( QuestionSetBean ) wdkModel.getQuestionSetsMap().get(
                    qSetName );
            if (wdkQuestionSet == null) return null;
            question = ( QuestionBean ) wdkQuestionSet.getQuestionsMap().get( qName );
        }
        return question;
    }

    public void setNonValidating() {
        validating = false;
    }

    public void setParamsFilled(boolean paramsFilled) {
        this.paramsFilled = paramsFilled;
    }

    public boolean getParamsFilled() {
        return paramsFilled;
    }
}
