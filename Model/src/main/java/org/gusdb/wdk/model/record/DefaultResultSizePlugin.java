package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;

public class DefaultResultSizePlugin implements ResultSize {

	@Override
	public Integer getResultSize(AnswerValue answerValue) throws WdkModelException, WdkUserException {
		return answerValue.getResultSize();
	}

}
