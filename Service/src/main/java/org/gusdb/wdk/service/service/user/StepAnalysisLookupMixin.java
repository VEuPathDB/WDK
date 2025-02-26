package org.gusdb.wdk.service.service.user;

import java.util.Optional;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.answer.factory.AnswerValueFactory;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.user.AbstractUserService.Access;

public interface StepAnalysisLookupMixin {

  WdkModel getWdkModel();
  UserBundle getUserBundle(Access requestedAccess) throws WdkModelException;
  long getStepId();

  /**
   * Retrieve and validate the step analysis instance identified by the given
   * analysis id.
   *
   * @param analysisId analysis ID from user input
   * @param userBundle user details
   * @param step step that owns this analysis
   * @param accessToken optional token that can allow access to non-owner
   * @return The step analysis instance that matches the input criteria
   * @throws WdkModelException if the step analysis instance could not be
   *                           loaded, the user could not be loaded, or the access token could not be
   *                           loaded.
   */
  default StepAnalysisInstance getAnalysis(
    long analysisId,
    UserBundle userBundle,
    Step step,
    String accessToken,
    ValidationLevel validationLevel
  ) throws WdkModelException {
    StepAnalysisInstance instance = getWdkModel()
        .getStepAnalysisFactory()
        .getInstanceById(userBundle.getRequestingUser(), analysisId, validationLevel)
        .orElseThrow(() -> new NotFoundException(AbstractWdkService.formatNotFound("step analysis: " + analysisId)));
    if (userBundle.getTargetUser().getUserId() != instance.getStep().getOwningUser().getUserId()) {
      // owner of this step does not match user in URL
      throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step analysis " + instance.getAnalysisId());
    }
    if (userBundle.isTargetRequestingUser() || instance.getAccessToken().equals(accessToken)) {
      return instance;
    }
    throw new ForbiddenException();
  }

  default StepAnalysis getStepAnalysisFromQuestion(Question question,
      String analysisName) throws DataValidationException {

    DataValidationException badStepAnalExcep = new DataValidationException(
        String.format("No step analysis with name %s exists for question %s",
        analysisName, question.getFullName()));

    try {
      return Optional.ofNullable(question.getStepAnalysis(analysisName))
        .orElseThrow(() -> badStepAnalExcep);
    } catch (WdkUserException e) {
      throw badStepAnalExcep;
    }
  }

  default StepAnalysisInstance getAnalysis(long analysisId, String accessToken, ValidationLevel validationLevel)
      throws WdkModelException {

    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    Step step = new StepFactory(userBundle.getRequestingUser()).getStepById(getStepId(), ValidationLevel.RUNNABLE)
        .orElseThrow(() -> new NotFoundException(String.format("Cannot find step with id %d", getStepId())));
    StepAnalysisInstance instance = getAnalysis(analysisId, userBundle, step, accessToken, validationLevel);
    long targetUser = userBundle.getTargetUser().getUserId();

    // Step cannot be found under the current user id path.
    if (targetUser != step.getOwningUser().getUserId())
      throw new NotFoundException(String.format("User %d does not own step %d",
          targetUser, getStepId()));

    // Analysis cannot be found under the current step id path.
    if (getStepId() != instance.getStep().getStepId())
      throw new NotFoundException(String.format(
        "Step %d does not contain analysis %d", getStepId(), analysisId));

    return instance;
  }

  default void validStepForAnalysisOrThrow(RunnableObj<Step> step, StepAnalysis stepAnalysis) throws DataValidationException, WdkModelException {
    // make sure answer value is valid for this plugin
    try {
      stepAnalysis.getAnalyzerInstance().validateAnswerValue(AnswerValueFactory.makeAnswer(Step.getRunnableAnswerSpec(step)));
    }
    catch (IllegalAnswerValueException e) {
      throw new DataValidationException(e.getMessage()); // will throw 422
    }
  }
}
