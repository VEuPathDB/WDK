package org.gusdb.wdk.model.answer;

import java.util.Map;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.spec.AnswerSpec;
import org.gusdb.wdk.model.user.User;

/**
 * Classes implementing this interface provide a model (set of named properties)
 * for a SummaryView.
 * 
 * @author jerric
 */
@Deprecated
public interface SummaryViewHandler {

    /**
     * Provides a model for a summary view of the given answer spec.
     * 
     * @param answerSpec answer spec for which to provide a model
     * @param parameters parameters passed to configure this view
     * @param user current user
     * @return model for JSP to display this summary view
     * @throws WdkModelException if system exception occurs
     * @throws WdkUserException if input parameters are invalid
     */
    Map<String, Object> process(RunnableObj<AnswerSpec> answerSpec, Map<String, String[]> parameters, User user)
        throws WdkModelException, WdkUserException;

    /**
     * Process updates related to this summary view and return a configuration
     * for a reload of the summary view.
     * 
     * @param answerSpec answer spec for which to provide a model
     * @param parameters parameters passed to update this view
     * @param user current user
     * @return new query string to pass when forwarding to showSummaryView
     * @throws WdkModelException if system exception occurs
     * @throws WdkUserException if input parameters are invalid
     */
    String processUpdate(RunnableObj<AnswerSpec> answerSpec, Map<String, String[]> parameters, User user)
        throws WdkModelException, WdkUserException;
}
