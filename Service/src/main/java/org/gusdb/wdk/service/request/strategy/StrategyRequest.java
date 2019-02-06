package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;

import javax.ws.rs.ForbiddenException;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.core.api.JsonKeys;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.json.JSONObject;

public class StrategyRequest {

  private String _name;
  private String _savedName;
  private String _description;
  private boolean _isSaved;
  private boolean _isHidden;
  private boolean _isPublic;
  private TreeNode<Step> _stepTree;

  /**
   * Strategy Request JSON as follows:
   * {
   *   name: strategy name
   *   savedName: saved strategy name (optional)
   *   description: strategy description (optional - default empty string)
   *   isSaved: whether strategy should be saved (optional - default false)
   *   isPublic: whether strategy should be public (optional - default false)
   *   root: {
   *     id: 1234,
   *     primaryInput: {
   *       id: 2345,
   *       primaryInput: { id: 3456 },
   *       secondaryInput: { id: 4567 }
   *     },
   *     secondaryInput: {
   *       id: 5678,
   *       primaryInput: { id: 6789 }
   *     }
   *   }
   * }
   * 
   * @param name
   * @param savedName
   * @param description
   * @param isSaved
   * @param isPublic
   * @param stepTree
   */
  public StrategyRequest(
      String name,
      String savedName,
      String description,
      boolean isSaved,
      boolean isPublic,
      TreeNode<Step> stepTree) {
    _name = name;
    _savedName = savedName;
    _description = description;
    _isSaved = isSaved;
    _isPublic = isPublic;
    _stepTree = stepTree;
  }

  public static StrategyRequest createFromJson(JSONObject json, StepFactory stepFactory, User user, String projectId)
      throws WdkModelException, DataValidationException {
    try {
      String name = json.getString(JsonKeys.NAME);
      String savedName = getStringOrDefault(json, JsonKeys.SAVED_NAME, "");
      String description = getStringOrDefault(json, JsonKeys.DESCRIPTION, "");
      boolean isSaved = getBooleanOrDefault(json, JsonKeys.IS_SAVED, false);
      boolean isPublic = getBooleanOrDefault(json, JsonKeys.IS_PUBLIC, false);
      
      // RRD 1/11 FIXME notes: this doesn't look quite right to me, instead, should:
      //    1. load the existing strategy
      //    2. load any new steps not in that strategy
      //    3. error if any newly added steps belong to another strat
      //    4. Use strategy and step builders to build a replacement strat based on incoming tree
      //    5. Validate the new strat (build with RUNNABLE)
      //    6. Save the strat
      //    7. Purge strategy ID and answer params from steps that were removed from strat, then save
      //          Can do this easily with StepBuilder.removeStrategy()
      
      JSONObject rootStepJson = json.getJSONObject(JsonKeys.ROOT_STEP);
      long rootStepId = rootStepJson.getLong(JsonKeys.ID);
      Step rootStep = stepFactory.getStepById(rootStepId, ValidationLevel.SEMANTIC)
          .orElseThrow(() -> new DataValidationException("Passed step ID " + rootStepId + " does not correspond to a step."));
      TreeNode<Step> stepTree = buildStepTree(new TreeNode<Step>(rootStep), rootStepJson, stepFactory, user, projectId, new StringBuilder());
      return new StrategyRequest(name, savedName, description, isSaved, isPublic, stepTree);
    }
    catch (WdkUserException e) {
      throw new DataValidationException(e);
    }
  }

  public static TreeNode<Step> buildStepTree(
      TreeNode<Step> stepTree,
      JSONObject stepJson,
      StepFactory stepFactory,
      User user,
      String projectId,
      StringBuilder errors)
          throws WdkUserException, WdkModelException {
    if(stepJson.length() == 0) return stepTree;
    Step parentStep = stepTree.getContents();
    errors.append(validateStep(parentStep, user, projectId));
    if(stepJson.has(JsonKeys.PRIMARY_INPUT_STEP)) {
    	  JSONObject leftStepJson = stepJson.getJSONObject(JsonKeys.PRIMARY_INPUT_STEP);
    	  if(leftStepJson != null && leftStepJson.has(JsonKeys.ID)) {
    		Step leftStep = stepFactory.getStepById(leftStepJson.getLong(JsonKeys.ID))
    		    .orElseThrow(() -> new WdkUserException("No step found with ID " + leftStepJson.getLong(JsonKeys.ID)));
    		//parentStep.setPreviousStep(leftStep);
    	    TreeNode<Step> leftTreeNode = new TreeNode<>(leftStep);
    	    if(leftStepJson.has(JsonKeys.PRIMARY_INPUT_STEP)) {
    	      stepTree.addChildNode(buildStepTree(leftTreeNode, leftStepJson.getJSONObject(JsonKeys.PRIMARY_INPUT_STEP), stepFactory, user, projectId, errors));
    	    }
    	    else {
    	    	  stepTree.addChildNode(leftTreeNode);
    	    }
    	  }
    }
    if(stepJson.has(JsonKeys.SECONDARY_INPUT_STEP)) {
  	  JSONObject rightStepJson = stepJson.getJSONObject(JsonKeys.SECONDARY_INPUT_STEP);
  	  if(rightStepJson != null && rightStepJson.has(JsonKeys.ID)) {
  		Step rightStep = stepFactory.getStepById(rightStepJson.getLong(JsonKeys.ID))
            .orElseThrow(() -> new WdkUserException("No step found with ID " + rightStepJson.getLong(JsonKeys.ID)));
  		//parentStep.setChildStep(rightStep);
  	    TreeNode<Step> rightTreeNode = new TreeNode<>(rightStep);
  	    if(rightStepJson.has(JsonKeys.SECONDARY_INPUT_STEP)) {
  	      stepTree.addChildNode(buildStepTree(rightTreeNode, rightStepJson.getJSONObject(JsonKeys.SECONDARY_INPUT_STEP), stepFactory, user, projectId, errors));
  	    }
	    else {
	    	  stepTree.addChildNode(rightTreeNode);
	    }
  	  }
    }
    validateWiring(stepTree, errors);
    if(errors.length() > 0) throw new WdkUserException(errors.toString());
    return stepTree;
  }
  
  protected static String validateStep(Step step, User user, String projectId) {
	StringBuilder errors = new StringBuilder();
    if(step.getStrategyId() != null) {
    	  errors.append("Step " + step.getStepId() + " is already embedded in strategy with id " + step.getStrategyId() + "." + System.lineSeparator());
    }
    if(!projectId.equals(step.getProjectId())) {
    	  errors.append("Step " + step.getStepId() + " belongs to a project other than " + projectId + "." + System.lineSeparator());
    }
    if(step.getUser().getUserId() != user.getUserId()) {
      throw new ForbiddenException(AbstractWdkService.PERMISSION_DENIED);
    }
    if(step.isDeleted()) {
    	  errors.append("Step " + step.getStepId() + " is marked as deleted." + System.lineSeparator());
    }
    if(step.getPrimaryInputStep() != null  || step.getPrimaryInputStepId() != 0 ||
    	    step.getSecondaryInputStep() != null || step.getSecondaryInputStepId() != 0) {
    	  errors.append("Step " + step.getStepId() + " cannot already be wired." + System.lineSeparator());
    }
    return errors.toString();
  }
  
  protected static void validateWiring(TreeNode<Step> stepTree, StringBuilder errors) throws WdkUserException, WdkModelException {
    Step step = stepTree.getContents();
    int numExpectedChildren = step.getAnswerSpec().getQuestion().getQuery().getAnswerParamCount();
    if (numExpectedChildren == 2 && (step.getPrimaryInputStep() == null || step.getSecondaryInputStep() == null)) {
      errors.append("The boolean step " + step.getStepId() + " requires two input steps." + System.lineSeparator());
    }
    if (numExpectedChildren == 1 && (step.getPrimaryInputStep() == null || step.getSecondaryInputStep() != null)) {
      errors.append("The transform step " + step.getStepId() + " requires exactly one input step." + System.lineSeparator());
    }
    for(TreeNode<Step> subTree : stepTree.getChildNodes()) {
      validateWiring(subTree, errors);
    }
  }

  public String getName() {
    return _name;
  }

  public String getSavedName() {
    return _savedName;
  }

  public String getDescription() {
    return _description;
  }
  
  public boolean isSaved() {
    return _isSaved;
  }
  
  public boolean isHidden() {
    return _isHidden;
  }
  
  public boolean isPublic() {
    return _isPublic;
  }

  public TreeNode<Step> getStepTree() {
    return _stepTree;
  }
}
