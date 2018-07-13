package org.gusdb.wdk.service.request.strategy;

import static org.gusdb.fgputil.json.JsonUtil.getStringOrDefault;
import static org.gusdb.fgputil.json.JsonUtil.getBooleanOrDefault;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotFoundException;

import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.service.WdkService;
import org.json.JSONException;
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
   *   isHidden: whether strategy should be hidden (optional - default false)
   *   isPublic: whether strategy should be public (optional - default false)
   *   root: {
   *     id: 1234,
   *     left: {
   *       id: 2345,
   *       left: { id: 3456 },
   *       right: { id: 4567 }
   *     },
   *     right: {
   *       id: 5678,
   *       left: { id: 6789 }
   *     }
   *   }
   * }
   * 
   * @param name
   * @param savedName
   * @param description
   * @param isSaved
   * @param isHidden
   * @param isPublic
   * @param stepTree
   */
  public StrategyRequest(String name,
		                 String savedName,
		                 String description,
		                 boolean isSaved,
		                 boolean isHidden,
		                 boolean isPublic,
		                 TreeNode<Step> stepTree) {
    _name = name;
    _savedName = savedName;
    _description = description;
    _isSaved = isSaved;
    _isHidden = isHidden;
    _isPublic = isPublic;
    _stepTree = stepTree;
  }

  public static StrategyRequest createFromJson(JSONObject json, StepFactory stepFactory, User user, String projectId)
      throws WdkModelException, RequestMisformatException, DataValidationException {
    try {
    	  String name = json.getString(Keys.NAME);
    	  String savedName = getStringOrDefault(json, Keys.SAVED_NAME, "");
    	  String description = getStringOrDefault(json, Keys.DESCRIPTION, "");
    	  boolean isSaved = getBooleanOrDefault(json, Keys.IS_SAVED, false);
    	  boolean isHidden = getBooleanOrDefault(json, Keys.IS_HIDDEN, false);
    	  boolean isPublic = getBooleanOrDefault(json, Keys.IS_PUBLIC, false);
    	  JSONObject rootStepJson = json.getJSONObject(Keys.ROOT_STEP);
    	  long rootStepId = rootStepJson.getLong(Keys.ID);
    	  Step rootStep = stepFactory.getStepById(rootStepId).orElseThrow(() -> new NotFoundException("Step ID not found: " + rootStepId));
    	  TreeNode<Step> stepTree = buildStepTree(new TreeNode<Step>(rootStep), rootStepJson, stepFactory, user, projectId, new StringBuilder());
      return new StrategyRequest(name, savedName, description, isSaved, isHidden, isPublic, stepTree);
    }
    catch (JSONException e) {
      throw new RequestMisformatException(e.getMessage());
    }
    catch (WdkUserException e) {
    	  throw new DataValidationException(e);
    }
  }
  
  public static TreeNode<Step> buildStepTree(TreeNode<Step> stepTree,
		  JSONObject stepJson,
		  StepFactory stepFactory,
		  User user,
		  String projectId,
		  StringBuilder errors)
      throws WdkUserException, WdkModelException {
    if(stepJson.length() == 0) return stepTree;
    Step parentStep = stepTree.getContents();
    errors.append(validateStep(parentStep, user, projectId));
    if(stepJson.has(Keys.LEFT_STEP)) {
    	  JSONObject leftStepJson = stepJson.getJSONObject(Keys.LEFT_STEP);
    	  if(leftStepJson != null && leftStepJson.has(Keys.ID)) {
    	    long leftStepId = leftStepJson.getLong(Keys.ID);
    		Step leftStep = stepFactory.getStepById(leftStepId).orElseThrow(() -> new NotFoundException("Step ID not found: " + leftStepId));
    		parentStep.setPreviousStep(leftStep);
    	    TreeNode<Step> leftTreeNode = new TreeNode<>(leftStep);
    	    if(leftStepJson.has(Keys.LEFT_STEP)) {
    	      stepTree.addChildNode(buildStepTree(leftTreeNode, leftStepJson.getJSONObject(Keys.LEFT_STEP), stepFactory, user, projectId, errors));
    	    }
    	    else {
    	    	  stepTree.addChildNode(leftTreeNode);
    	    }
    	  }
    }
    if(stepJson.has(Keys.RIGHT_STEP)) {
  	  JSONObject rightStepJson = stepJson.getJSONObject(Keys.RIGHT_STEP);
  	  if(rightStepJson != null && rightStepJson.has(Keys.ID)) {
  	    long rightStepId = rightStepJson.getLong(Keys.ID);
  		Step rightStep = stepFactory.getStepById(rightStepId).orElseThrow(() -> new NotFoundException("Step ID not found: " + rightStepId));
  		parentStep.setChildStep(rightStep);
  	    TreeNode<Step> rightTreeNode = new TreeNode<>(rightStep);
  	    if(rightStepJson.has(Keys.RIGHT_STEP)) {
  	      stepTree.addChildNode(buildStepTree(rightTreeNode, rightStepJson.getJSONObject(Keys.RIGHT_STEP), stepFactory, user, projectId, errors));
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
  
  protected static String validateStep(Step step, User user, String projectId) throws WdkModelException {
	StringBuilder errors = new StringBuilder();
    if(step.getStrategyId() != null) {
    	  errors.append("Step " + step.getStepId() + " is already embedded in strategy with id " + step.getStrategyId() + "." + System.lineSeparator());
    }
    if(!projectId.equals(step.getProjectId())) {
    	  errors.append("Step " + step.getStepId() + " belongs to a project other than " + projectId + "." + System.lineSeparator());
    }
    if(step.getUser().getUserId() != user.getUserId()) {
      throw new ForbiddenException(WdkService.PERMISSION_DENIED);
    }
    if(step.isDeleted()) {
    	  errors.append("Step " + step.getStepId() + " is marked as deleted." + System.lineSeparator());
    }
    if(step.getPreviousStep() != null  || step.getPreviousStepId() != 0 ||
    	    step.getChildStep() != null || step.getChildStepId() != 0) {
    	  errors.append("Step " + step.getStepId() + " cannot already be wired." + System.lineSeparator());
    }
    return errors.toString();
  }
  
  protected static void validateWiring(TreeNode<Step> stepTree, StringBuilder errors) throws WdkUserException, WdkModelException {
	Step step = stepTree.getContents(); 
    if(step.isBoolean() && (step.getPreviousStep() == null || step.getChildStep() == null)) {
      errors.append("The boolean step " + step.getStepId() + " requires two input steps." + System.lineSeparator());
    }
    if(step.isTransform() && (step.getPreviousStep() == null || step.getChildStep() != null)) {
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
