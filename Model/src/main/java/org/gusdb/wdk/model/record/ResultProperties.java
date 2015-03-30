package org.gusdb.wdk.model.record;

import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public interface ResultProperties {
	
	Map<String, String> getPropertyValues(AnswerValue answerValue) throws WdkModelException, WdkUserException ;
	
	/**
	 * validate that the property names declared in the XML model are those that are expected.
	 * @param propNames
	 */
	void validatePropertyNames(List<String> propNames) throws WdkModelException;

}
