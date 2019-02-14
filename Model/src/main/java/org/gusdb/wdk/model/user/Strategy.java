package org.gusdb.wdk.model.user;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.defaultOnException;
import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.wdk.model.user.StepContainer.withId;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.functional.TreeNode;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.gusdb.fgputil.json.JsonUtil;
import org.gusdb.fgputil.validation.ValidObjectFactory.RunnableObj;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkRuntimeException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.user.Step.StepBuilder;
import org.json.JSONException;
import org.json.JSONObject;

public class Strategy implements StrategyElement, StepContainer, Validateable<Strategy> {

  private static final Logger LOG = Logger.getLogger(Strategy.class);

  public static class StrategyBuilder {

    private final WdkModel _wdkModel;
    private final long _userId;
    private final long _strategyId;
    private String _projectId;
    private String _version;
    private String _name;
    private String _description;
    private boolean _isSaved = false;
    private String _savedName = null;
    private boolean _isDeleted = false;
    private Date _createdTime = new Date();
    private Date _lastModifiedTime;
    private Date _lastRunTime;
    private String _signature;
    private boolean _isPublic = false;
    private long _rootStepId = 0;

    private Map<Long,StepBuilder> _stepMap = new HashMap<>();

    public StrategyBuilder(WdkModel wdkModel, long userId, long strategyId) {
      _wdkModel = wdkModel;
      _projectId = wdkModel.getProjectId();
      _version = wdkModel.getVersion();
      _userId = userId;
      _strategyId = strategyId;
    }

    private StrategyBuilder(Strategy strategy) {
      _wdkModel = strategy._wdkModel;
      _userId = strategy.getUser().getUserId();
      _strategyId = strategy._strategyId;
      _projectId = strategy._projectId;
      _version = strategy._version;
      _name = strategy._name;
      _description = strategy._description;
      _isSaved = strategy._isSaved;
      _savedName = strategy._savedName;
      _isDeleted = strategy._isDeleted;
      _createdTime = strategy._createdTime;
      _lastModifiedTime = strategy._lastModifiedTime;
      _lastRunTime = strategy._lastRunTime;
      _signature = strategy._signature;
      _isPublic = strategy._isPublic;
      _rootStepId = strategy._rootStepId;
      _stepMap = Functions.getMapFromList(strategy._stepMap.values(),
          step -> new TwoTuple<>(step.getStepId(), Step.builder(step)));
    }

    public long getStrategyId() {
      return _strategyId;
    }

    public StrategyBuilder setProjectId(String projectId) {
      _projectId = projectId;
      return this;
    }

    public StrategyBuilder setVersion(String version) {
      _version = version;
      return this;
    }

    public StrategyBuilder setName(String name) {
      _name = name == null ? null :
        FormatUtil.shrinkUtf8String(name, StepFactory.COLUMN_NAME_LIMIT);
      return this;
    }

    public StrategyBuilder setDescription(String description) {
      _description = description;
      return this;
    }

    public StrategyBuilder setSaved(boolean isSaved) {
      _isSaved = isSaved;
      return this;
    }

    public StrategyBuilder setSavedName(String savedName) {
      _savedName = savedName == null ? null :
        FormatUtil.shrinkUtf8String(savedName, StepFactory.COLUMN_NAME_LIMIT);
      return this;
    }

    public StrategyBuilder setDeleted(boolean isDeleted) {
      _isDeleted = isDeleted;
      return this;
    }

    public StrategyBuilder setCreatedTime(Date createdTime) {
      _createdTime = createdTime;
      return this;
    }

    public StrategyBuilder setLastModifiedTime(Date lastModifiedTime) {
      _lastModifiedTime = lastModifiedTime;
      return this;
    }

    public StrategyBuilder setLastRunTime(Date lastRunTime) {
      _lastRunTime = lastRunTime;
      return this;
    }

    public StrategyBuilder setSignature(String signature) {
      _signature = signature;
      return this;
    }

    public StrategyBuilder setIsPublic(boolean isPublic) {
      _isPublic = isPublic;
      return this;
    }

    // note root step must be added just like any other step
    public StrategyBuilder setRootStepId(long rootStepId) {
      _rootStepId = rootStepId;
      return this;
    }

    public StrategyBuilder addStep(StepBuilder step) {
      _stepMap.put(step.getStepId(), step);
      return this;
    }

    public StrategyBuilder addSteps(Collection<StepBuilder> steps) {
      _stepMap.putAll(Functions.getMapFromList(steps, step -> new TwoTuple<>(step.getStepId(), step)));
      return this;
    }

    /**
     * Removes any existing StepBuilders from this builder and resets
     * the root step ID to 0.
     * 
     * @return this builder
     */
    public StrategyBuilder clearSteps() {
      _stepMap.clear();
      _rootStepId = 0;
      return this;
    }

    public Strategy build(UserCache userCache, ValidationLevel validationLevel)
        throws InvalidStrategyStructureException {
      return new Strategy(this, userCache.get(_userId), validationLevel);
    }

    public long getNumSteps() {
      return _stepMap.size();
    }
  }

  public static StrategyBuilder builder(WdkModel wdkModel, long userId, long strategyId) {
    return new StrategyBuilder(wdkModel, userId, strategyId);
  }

  public static StrategyBuilder builder(Strategy strategy) {
    return new StrategyBuilder(strategy);
  }

  private final WdkModel _wdkModel;
  private final User _user;
  private final long _strategyId;
  private final String _projectId;
  private final String _version;
  private final String _name;
  private final String _description;
  private final boolean _isSaved;
  private final String _savedName;
  private final boolean _isDeleted;
  private final Date _createdTime;
  private final Date _lastModifiedTime;
  private final Date _lastRunTime;
  private final String _signature;
  private final boolean _isPublic;
  private final long _rootStepId;
  private final Map<Long, Step> _stepMap;
  private final ValidationBundle _validationBundle;

  private Strategy(StrategyBuilder strategyBuilder, User user,
      ValidationLevel validationLevel)
          throws InvalidStrategyStructureException {
    _user = user;
    _wdkModel = strategyBuilder._wdkModel;
    _strategyId = strategyBuilder._strategyId;
    _projectId = strategyBuilder._projectId;
    _version = strategyBuilder._version;
    _name = strategyBuilder._name;
    _description = strategyBuilder._description;
    _isSaved = strategyBuilder._isSaved;
    _savedName = strategyBuilder._savedName;
    _isDeleted = strategyBuilder._isDeleted;
    _createdTime = strategyBuilder._createdTime;
    _lastModifiedTime = strategyBuilder._lastModifiedTime;
    _lastRunTime = strategyBuilder._lastRunTime;
    _signature = strategyBuilder._signature;
    _isPublic = strategyBuilder._isPublic;
    _rootStepId = strategyBuilder._rootStepId;
    _stepMap = new HashMap<>();
    // populates _stepMap
    buildSteps(strategyBuilder._stepMap, validationLevel);
    _validationBundle = ValidationBundle.builder(validationLevel)
        .aggregateStatus(_stepMap.values().toArray(new Step[0]))
        .build();
  }

  private void buildSteps(Map<Long, StepBuilder> steps,
      ValidationLevel validationLevel) throws InvalidStrategyStructureException {

    // Confirm project and user id match across strat and all steps, and that steps were properly assigned
    for (StepBuilder step : steps.values()) {
      String identifier = "Step " + step.getStepId();
      if (_user.getUserId() != step.getUserId()) {
        throw new InvalidStrategyStructureException(identifier + " does not have the same owner as its strategy, " + _strategyId);
      }
      if (!_projectId.equals(step.getProjectId())) {
        throw new InvalidStrategyStructureException(identifier + " does not have the same project as its strategy, " + _strategyId);
      }
      Long stepStratId = step.getStrategyId();
      if (stepStratId == null || _strategyId != stepStratId) {
        throw new InvalidStrategyStructureException(identifier + " was given to strategy " + _strategyId + " but belongs to strategy " + stepStratId + " (i.e. SQL is broken).");
      }
    }

    // temporarily build an actual tree of the steps from a copy of the step builder map
    Map<Long, StepBuilder> stepMap = new HashMap<>(steps); // make a copy since buildTree modifies it
    TreeNode<StepBuilder> builderTree = buildTree(stepMap, _rootStepId);
    // FIXME: uncomment when findCircularPaths is implemented
    //if (!builderTree.findCircularPaths().isEmpty()) {
    //  throw new InvalidStrategyStructureException("Strategy " + _strategyId + "'s tree has at least one circular dependency.");
    //}
    if (!stepMap.isEmpty()) {
      throw new InvalidStrategyStructureException("Strategy " + _strategyId + " has been " +
          "assigned the following steps which are not referenced in its tree: " +
          join(stepMap.values().stream().map(StepBuilder::getStepId), ", "));
    }

    LOG.debug("Generated the following tree of steps for strategy " +
        _strategyId + ":" + FormatUtil.NL + builderTree.toMultiLineString("  "));
    
    // Build StepBuilders from the bottom up into a tree of Steps, setting dirty
    // bits on steps downstream from dirty steps as it builds the tree.
    UserCache userCache = new UserCache(_user);
    Strategy thisStrategy = this;

    // map structure into a TreeNode<Step> so branches have access to the
    //   built children below them (need to assign dirty bit)
    builderTree.mapStructure(new StructureMapper<StepBuilder, TreeNode<Step>>() {
      @Override
      public TreeNode<Step> apply(StepBuilder builder, List<TreeNode<Step>> mappedChildren) {
        try {
          // figure out if dirty bit should be set (if this or any children dirty)
          boolean isDirty = builder.isResultSizeDirty() || reduce(
            mappedChildren,
            (dirtySoFar, childNode) -> dirtySoFar || childNode.getContents().isResultSizeDirty(),
            false);

          // build the step
          Step step = builder
            .setResultSizeDirty(isDirty)
            .build(userCache, validationLevel, thisStrategy);
  
          // add to strategy
          thisStrategy._stepMap.put(step.getStepId(), step);
  
          // build a node around the step and add children (builds tree of Steps)
          TreeNode<Step> node = new TreeNode<>(step);
          node.addChildNodes(mappedChildren, node2 -> true);
          return node;
        }
        catch (WdkModelException e) {
          throw new WdkRuntimeException(e);
        }
      }
    });
  }

  /**
   * Recursive function builds a tree of step builders from the passed map.
   *
   * @param steps map of step builders to put in tree
   * @param stepId step being added to the tree in the current recursive call
   * @return tree of steps whose root has the passed stepId
   * @throws InvalidStrategyStructureException if step ID referenced that does not exist in map
   */
  private TreeNode<StepBuilder> buildTree(Map<Long, StepBuilder> steps, long stepId) throws InvalidStrategyStructureException {
    StepBuilder step = steps.get(stepId);
    if (step == null) {
      throw new InvalidStrategyStructureException("Step " + stepId + ", referenced in the" +
          " tree of strategy " + _strategyId + " has either not been assigned" +
          " to that strategy or has been assigned more than once.");
    }
    // create a node for this step and remove from the map
    TreeNode<StepBuilder> node = new TreeNode<>(step);
    steps.remove(stepId);

    // check for answer params; if not present or undeterminable (bad question name), simply return
    String questionName = step.getAnswerSpec().getQuestionName();
    Optional<List<AnswerParam>> answerParams = _wdkModel.getQuestionByName(questionName)
        .map(question -> question.getQuery().getAnswerParams());

    // Check if answer params are present.  If not present in optional, then
    // question name is invalid in this step; if leaf step, that's ok- will just
    // be invalid, but if boolean/transform, then the branch below it is
    // irretrieveable and will result in a WdkModelException since extra steps
    // will be found in this strategy.  This is probably ok since we don't
    // change boolean/transform question names very often.  If it's NOT ok,
    // we'll have to reintroduce the child/previous DB cols back into this code.
    if (answerParams.isPresent()) {
      // answer params are present; find child steps
      List<String> answerParamValues = answerParams.get().stream()
          .map(param -> step.getParamValue(param.getName()))
          .collect(Collectors.toList());
      for (String paramValue : answerParamValues) {
        if (FormatUtil.isInteger(paramValue)) { // skip if non-numeric; param will fail validation
          long childStepId = Long.parseLong(paramValue);
          node.addChildNode(buildTree(steps, childStepId));
        }
      }
    }

    // all child nodes have been added; return node for this step
    return node;
  }

  public User getUser() {
    return _user;
  }

  public boolean isDeleted() {
    return _isDeleted;
  }

  public String getVersion() {
    return _version;
  }

  public String getName() {
    return _name;
  }

  public String getSavedName() {
    return _savedName;
  }

  public boolean isSaved() {
    return _isSaved;
  }

  public boolean isPublic() {
    return _isPublic;
  }

  public String getProjectId() {
    return _projectId;
  }

  public Step getRootStep() {
    return findFirstStep(withId(_rootStepId)).orElseThrow(
        () -> new WdkRuntimeException("Root step ID " + _rootStepId + " no longer present in strategy."));
  }

  @Override
  public Long getStrategyId() {
    return _strategyId;
  }

  /**
   * @return Returns the createTime.
   */
  public Date getCreatedTime() {
    return _createdTime;
  }

  public List<Step> getMainBranch() {
    return getRootStep().getMainBranch();
  }

  public int getLength() {
    return getRootStep().getLength();
  }

  public long getRootStepId() {
    return _rootStepId;
  }

  /**
   * @param overwrite
   *          if true, it will overwrite the strategy even if it's already saved; if false, we will create a
   *          new unsaved copy if the strategy is already saved.
   *
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Strategy update(boolean overwrite) throws WdkModelException, WdkUserException {
    return _wdkModel.getStepFactory().updateStrategy(this, overwrite);
  }

  public RecordClass getRecordClass() {
    return getRootStep().getRecordClass();
  }

  public Step getFirstStep() {
    Step step = getRootStep();
    while (step.getPrimaryInputStep() != null) {
      step = step.getPrimaryInputStep();
    }
    return step;
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   *
   * @return
   * @throws WdkModelException
   */
  public String getChecksum() throws WdkModelException {
    JSONObject jsStrategy = getJSONContent(true);
    String checksum = EncryptionUtil.encrypt(JsonUtil.serialize(jsStrategy));
    LOG.debug("Strategy #" + _strategyId + ", checksum=" + checksum + ", json:\n" + jsStrategy);
    return checksum;
  }

  public JSONObject getJSONContent() throws WdkModelException {
    return getJSONContent(false);
  }

  // TODO: see if this is really the JSON we want to send and also whether we want isValid in the checksum
  public JSONObject getJSONContent(boolean forChecksum) throws WdkModelException {
    JSONObject jsStrategy = new JSONObject();

    try {
      jsStrategy.put("id", _strategyId);
      jsStrategy.put("name", _name);
      jsStrategy.put("savedName", _savedName);
      jsStrategy.put("description", _description);
      jsStrategy.put("saved", _isSaved);
      jsStrategy.put("deleted", _isDeleted);
      jsStrategy.put("type", getRecordClass().getFullName());

      if (!forChecksum) {
        jsStrategy.put("valid", isValid());
        jsStrategy.put("version", getVersion());
        jsStrategy.put("resultSize", getEstimatedSize());
      }

      JSONObject stepContent = getRootStep().getJSONContent(_strategyId, forChecksum);
      jsStrategy.put("latestStep", stepContent);
    }
    catch (JSONException ex) {
      throw new WdkModelException(ex);
    }
    return jsStrategy;
  }

  @Override
  public boolean isValid() {
    return _validationBundle.getStatus().isValid();
  }

  /**
   * @return the lastRunTime
   */
  public Date getLastRunTime() {
    return getRootStep().getLastRunTime();
  }

  /**
   * @return the lastModifiedTime
   */
  public Date getLastModifiedTime() {
    return _lastModifiedTime;
  }

  /**
   * checksum of a strategy is different from signature in that signature is stable and it will never change
   * after the strategy is created, while checksum depends on many properties of a strategy, and it will
   * change when the strategies properties are changed.
   *
   * @return the signature
   */
  public String getSignature() {
    return _signature;
  }

  /**
   * @return the description
   */
  public String getDescription() {
    return _description;
  }


  public int getResultSize() {
    // FIXME: could root step really be null?  I think if root step is deleted, strategy id deleted?
    return (getRootStep() == null ? 0 : defaultOnException(() -> getRootStep().getResultSize(), 0));
  }

  public String getEstimatedSize() {
    // FIXME: could root step really be null?  I think if root step is deleted, strategy id deleted?
    int latestStepEstimateSize = getRootStep() == null ? 0 : getRootStep().getEstimatedSize();
    return (latestStepEstimateSize == Step.RESET_SIZE_FLAG ? "Unknown" : String.valueOf(latestStepEstimateSize));
  }

  @Override
  public long getId() {
    return getStrategyId();
  }

  public int getNumSteps() {
    return _stepMap.size();
  }

  /**
   * Returns the first step in this strategy that passes the search predicate.  If none exists,
   * throws IllegalArgumentException with a custom message.
   *
   * @param search step search
   * @return first step found that passes the predicate
   * @throws IllegalArgumentException if strategy does not contain a step that matches the search criteria
   */
  @Override
  public Optional<Step> findFirstStep(StepSearch search) {
    return _stepMap.values().stream().filter(search.getPredicate()).findFirst();
  }

  /**
   * @return all steps in this strategy
   */
  public List<Step> getAllSteps() {
    return new ArrayList<>(_stepMap.values());
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public static Optional<RunnableObj<Step>> getRunnableStep(RunnableObj<Strategy> strategy, long stepId) {
    return strategy.getObject().findFirstStep(withId(stepId)).map(step -> step.getRunnable().getLeft());
  }

}
