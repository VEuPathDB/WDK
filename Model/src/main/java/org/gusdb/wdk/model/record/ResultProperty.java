package org.gusdb.wdk.model.record;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface ResultProperty {
	
	Map<String, String> getPropertyValue(AnswerValue answerValue) throws WdkModelException, WdkUserException ;

}
