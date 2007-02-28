/**
 * 
 */
package org.gusdb.wdk.model.report;

import java.util.Map;

import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.WdkModelException;

/**
 * @author xingao
 * 
 */
public interface IReporter {

    public void config(Map<String, String> config);

    public String format(Answer answer) throws WdkModelException;
}
