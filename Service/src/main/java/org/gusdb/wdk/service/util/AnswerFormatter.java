package org.gusdb.wdk.service.util;

import javax.ws.rs.core.StreamingOutput;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;

public interface AnswerFormatter <T> {

  public T formatAnswer(AnswerValueBean answerValue) throws WdkModelException;

  public String getAnswerAsString(AnswerValueBean answerValue) throws WdkModelException;

  public StreamingOutput getAnswerAsStream(AnswerValueBean answerValue) throws WdkModelException;

}
