package org.gusdb.wdk.service.service.user;

import java.util.Optional;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.analysis.StepAnalysis;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.analysis.IllegalAnswerValueException;
import org.gusdb.wdk.model.user.analysis.StepAnalysisInstance;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.user.UserService.Access;

public interface StepAnalysisLookupMixin {

  WdkModel getWdkModel();
  UserBundle getUserBundle(Access requestedAccess) throws WdkModelException;
  long getStepId();

  /**
   * Retrieve and validate the step analysis instance identified by the given
   * analysis id.
   *
   * @param analysisId Analysis ID from user input
   * @param userBundle    User details
   * @param accessToken   ?
   * @return The step analysis instance that matches the input criteria
   * @throws WdkModelException if the step analysis instance could not be
   *                           loaded, the user could not be loaded, or the access token could not be
   *                           loaded.
   */
  default StepAnalysisInstance getAnalysis(
    long analysisId,
    UserBundle userBundle,
    String accessToken
  ) throws WdkModelException {
    try {
      StepAnalysisInstance instance = getWdkModel().getStepAnalysisFactory()
          .getSavedAnalysisInstance(analysisId);
      if (userBundle.getTargetUser().getUserId() != instance.getStep().getUser().getUserId()) {
        // owner of this step does not match user in URL
        throw new NotFoundException("User " + userBundle.getTargetUser().getUserId() + " does not own step analysis " + instance.getAnalysisId());
      }
      if (userBundle.isSessionUser() || instance.getAccessToken().equals(accessToken)) {
        return instance;
      }
      throw new ForbiddenException();
    }
    catch (WdkUserException e) {
      throw new NotFoundException(AbstractWdkService.formatNotFound("step analysis: " + analysisId));
    }
  }

  /**
   * Creates StepAnalysisInstance from given step, analysis name, and answer
   * value checksum.
   *
   * @param step          The step for which a new analysis instance will be
   *                      created
   * @param analysis      The analysis type for the new analysis instance
   * @param answerValHash Hash of the relevant current state of the answer that
   *                      this analysis is based on.
   *
   * @return A new StepAnalysisInstance
   */
  default StepAnalysisInstance getStepAnalysisInstance(
      RunnableObj<Step> step,
      StepAnalysis analysis,
      String answerValHash) throws DataValidationException {
    try {
      return getWdkModel().getStepAnalysisFactory()
          .createAnalysisInstance(step.get(), analysis, answerValHash);
    }
    catch (WdkUserException | IllegalAnswerValueException | WdkModelException e) {
      throw new DataValidationException("Can't create valid step analysis", e);
    }
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

  default StepAnalysisInstance getAnalysis(long analysisId, String accessToken)
      throws WdkModelException {

    UserBundle userBundle = getUserBundle(Access.PUBLIC);
    StepAnalysisInstance instance = getAnalysis(analysisId, userBundle, accessToken);
    Step step = instance.getStep();
    long targetUser = userBundle.getTargetUser().getUserId();

    // Step cannot be found under the current user id path.
    if (targetUser != step.getUser().getUserId())
      throw new NotFoundException(String.format("User %d does not own step %d",
          targetUser, getStepId()));

    // Analysis cannot be found under the current step id path.
    if (getStepId() != step.getStepId())
      throw new NotFoundException(String.format(
        "Step %d does not contain analysis %d", getStepId(), analysisId));

    return instance;
  }
}
