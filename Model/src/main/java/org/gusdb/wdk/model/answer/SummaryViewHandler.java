package org.gusdb.wdk.model.answer;

import java.util.Map;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * Classes implementing this interface provide a model (set of named properties)
 * for a SummaryView.
 * 
 * @author jerric
 */
public interface SummaryViewHandler {

    /**
     * Provides a model for a summary view of the given step.
     * 
     * @param step step for which to provide a model
     * @param parameters parameters passed to configure this view
     * @param user current user
     * @param wdkModel WDK model
     * @return model
     * @throws WdkModelException if system exception occurs
     * @throws WdkUserException if input parameters are invalid
     */
    Map<String, Object> process(Step step, Map<String, String[]> parameters, User user, WdkModel wdkModel)
        throws WdkModelException, WdkUserException;

    /**
     * Process updates related to this summary view and return a configuration
     * for a reload of the summary view.
     * 
     * @param step step for which to provide a model
     * @param parameters parameters passed to update this view
     * @param user current user
     * @param wdkModel WDK model
     * @return new query string to pass when forwarding to showSummaryView
     * @throws WdkModelException if system exception occurs
     * @throws WdkUserException if input parameters are invalid
     */
    String processUpdate(Step step, Map<String, String[]> parameters, User user, WdkModel wdkModel)
        throws WdkModelException, WdkUserException;
}
