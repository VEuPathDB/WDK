package org.gusdb.wdk.model.answer;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;

/**
 * Classes implementing this interface provide a model (set of named properties)
 * for a SummaryView.
 * 
 * @author jerric
 */
public interface SummaryViewHandler {
    Map<String, Object> process(Step step, Map<String, String[]> parameters)
        throws WdkModelException, WdkUserException;
}
