package org.gusdb.wdk.model.report;

import org.gusdb.wdk.model.answer.AnswerValue;

/**
 * For now this is a dummy class that can be referenced in the model XML to
 * "pretend" to handle WDK service JSON reporter output.  In truth, this
 * reporter doesn't do anything; a request for standard JSON will be intercepted
 * by the service and returned by service functionality.  We may refactor in
 * the future.
 * 
 * @author rdoherty
 */
public class WdkServiceJsonReporter extends StubReporter {

  public WdkServiceJsonReporter(AnswerValue answerValue, int startIndex, int endIndex) {
    super(answerValue, startIndex, endIndex);
  }
}
