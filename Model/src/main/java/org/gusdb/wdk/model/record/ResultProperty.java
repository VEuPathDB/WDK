package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface ResultProperty {
	
	Integer getPropertyValue(AnswerValue answerValue, String propertyName) throws WdkModelException, WdkUserException ;

}
