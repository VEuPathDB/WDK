/**
 * 
 */
package org.gusdb.wdk.view;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;

/**
 * @author jerric
 * 
 */
public interface SummaryViewHandler {

    Map<String, Object> process(Step step) throws WdkModelException,
            WdkUserException;
}
