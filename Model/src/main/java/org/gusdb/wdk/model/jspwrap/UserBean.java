package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactoryHelpers.NameCheckInfo;
import org.gusdb.wdk.model.user.StepUtilities;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.UserSession;

/**
 * @author: Jerric
 * @created: May 25, 2006
 * @modified by: Jerric
 * @modified at: May 25, 2006
 * 
 */
public class UserBean {

  private static final Logger LOG = Logger.getLogger(UserBean.class);

  private final User _user;
  private final UserSession _userSession;
  private final WdkModel _wdkModel;

  // FIXME: This does not belong here
  private int _stepId;

  public UserBean(User user) {
    _user = user;
    _userSession = user.getSession();
    _wdkModel = user.getWdkModel();
    
  }

  public User getUser() {
    return _user;
  }

  public long getUserId() {
    return _user.getUserId();
  }

  public String getEmail() {
    return _user.getEmail();
  }

  public String getSignature() {
    return _user.getSignature();
  }

  public boolean isGuest() {
    return _user.isGuest();
  }

  public String getFirstName() {
    return _user.getProfileProperties().get("firstName");
  }

  public String getMiddleName() {
    return _user.getProfileProperties().get("middleName");
  }

  public String getLastName() {
    return _user.getProfileProperties().get("lastName");
  }

  public String getOrganization() {
    return _user.getProfileProperties().get("organization");
  }

  public String getFrontAction() {
    return _userSession.getFrontAction();
  }

  public Long getFrontStrategy() {
    return _userSession.getFrontStrategy();
  }

  public Long getFrontStep() {
    return _userSession.getFrontStep();
  }

  public Map<String, String> getGlobalPreferences() {
    return _user.getPreferences().getGlobalPreferences();
  }

  public Map<String, String> getProjectPreferences() {
    return _user.getPreferences().getProjectPreferences();
  }

  // =========================================================================
  // Methods for dataset operations
  // =========================================================================

  public DatasetBean getDataset(long userDatasetId) throws WdkModelException {
    return new DatasetBean(_wdkModel.getDatasetFactory().getDataset(_user, userDatasetId));
  }

  // =========================================================================
  // Methods for Persistent history operations
  // =========================================================================

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#clearHistories()
   */
  public void deleteSteps() throws WdkModelException {
    StepUtilities.deleteSteps(_user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.Answer)
   */
  public StepBean createStep(Long strategyId, QuestionBean question, Map<String, String> params,
      String filterName, boolean deleted, int assignedWeight) throws WdkModelException, WdkUserException {
    Step step = StepUtilities.createStep(_user, strategyId, question._question, params, filterName, deleted,
        assignedWeight);
    return new StepBean(this, step);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#deleteHistory(int)
   */
  public void deleteStep(int displayId) throws WdkModelException {
    _wdkModel.getStepFactory().deleteStep(displayId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistories()
   */
  public StepBean[] getSteps() throws WdkModelException {
    Step[] steps = StepUtilities.getSteps(_user);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public Map<String, List<StepBean>> getStepsByCategory() throws WdkModelException {
    Map<String, List<Step>> steps = StepUtilities.getStepsByCategory(_user);
    Map<String, List<StepBean>> category = new LinkedHashMap<String, List<StepBean>>();
    for (String type : steps.keySet()) {
      List<Step> list = steps.get(type);
      List<StepBean> beans = new ArrayList<StepBean>();
      for (Step step : list) {
        beans.add(new StepBean(this, step));
      }
      category.put(type, beans);
    }
    return category;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistories(java.lang.String)
   */
  public StepBean[] getSteps(String recordClassName) throws WdkModelException {
    Step[] steps = StepUtilities.getSteps(_user, recordClassName);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public StrategyBean getStrategy(long strategyId) throws WdkUserException, WdkModelException {
    return new StrategyBean(this, StepUtilities.getStrategy(_user, strategyId));
  }

  public Map<String, List<StrategyBean>> getStrategiesByCategory() throws Exception {
    try {
      Map<String, List<Strategy>> strategies = StepUtilities.getStrategiesByCategory(_user);
      return convertMap(strategies);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  private Map<String, List<StrategyBean>> convertMap(Map<String, List<Strategy>> strategies) {
    Map<String, List<StrategyBean>> category = new LinkedHashMap<String, List<StrategyBean>>();
    for (String type : strategies.keySet()) {
      List<Strategy> list = strategies.get(type);
      List<StrategyBean> beans = new ArrayList<StrategyBean>();
      for (Strategy strategy : list) {
        beans.add(new StrategyBean(this, strategy));
      }
      category.put(type, beans);
    }
    return category;
  }

  public int getPublicCount() throws WdkModelException {
    int count = _user.getWdkModel().getStepFactory().getPublicStrategyCount();
    LOG.debug("Found number of public strats: " + count);
    return count;
  }

  public List<StrategyBean> getInvalidStrategies() {
    // Strategy[] strategies = user.getInvalidStrategies();
    List<StrategyBean> beans = new ArrayList<StrategyBean>();
    // for (int i = 0; i < strategies.length; i++) {
    // beans[i] = new StrategyBean(this, strategies[i]);
    // }
    return beans;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getStrategyCount()
   */
  public int getStrategyCount() throws WdkModelException {
    return _wdkModel.getStepFactory().getStrategyCount(_user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistoryCount()
   */
  public int getStepCount() throws WdkModelException {
    return _wdkModel.getStepFactory().getStepCount(_user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getItemsPerPage()
   */
  public int getItemsPerPage() {
    return _user.getPreferences().getItemsPerPage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setItemsPerPage(int)
   */
  public void setItemsPerPage(int itemsPerPage) throws WdkModelException {
    _user.getPreferences().setItemsPerPage(itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return _user.toString();
  }

  public void addActiveStrategy(String strategyKey) throws NumberFormatException, WdkUserException,
      WdkModelException {
    _userSession.addActiveStrategy(strategyKey);
  }

  public void removeActiveStrategy(String strategyId) {
    _userSession.removeActiveStrategy(strategyId);
  }

  public void replaceActiveStrategy(long oldStrategyId, long newStrategyId, Map<Long, Long> stepIdsMap)
      throws WdkUserException, WdkModelException {
    _userSession.replaceActiveStrategy(oldStrategyId, newStrategyId, stepIdsMap);
  }

  /**
   * @see org.gusdb.wdk.model.user.User#deleteStrategies()
   */
  public void deleteStrategies() throws WdkModelException {
    _wdkModel.getStepFactory().deleteStrategies(_user, false);
  }

  /**
   * @param strategyId
   * @see org.gusdb.wdk.model.user.User#deleteStrategy(int)
   */
  public void deleteStrategy(int strategyId) throws WdkModelException {
    StepUtilities.deleteStrategy(_user, strategyId);
  }

  /**
   * @param rootAnswerChecksum
   * @return
   * @see org.gusdb.wdk.model.user.User#importStrategyByAnswer(java.lang.String)
   */
  public StrategyBean importStrategy(String strategyKey) throws WdkModelException, WdkUserException {
    Strategy strategy = StepUtilities.importStrategy(_user, strategyKey);
    return new StrategyBean(this, strategy);
  }

  public NameCheckInfo checkNameExists(StrategyBean strategy, String name, boolean saved)
      throws WdkModelException {
    return _wdkModel.getStepFactory().checkNameExists(strategy.strategy, name, saved);
  }

  /**
   * @param answer
   * @param saved
   * @return
   * @see org.gusdb.wdk.model.user.User#createStrategy(org.gusdb.wdk.model.user.Step, boolean)
   */
  public StrategyBean createStrategy(StepBean step, boolean saved, boolean hidden) throws WdkUserException,
      WdkModelException {
    return new StrategyBean(this, StepUtilities.createStrategy(step.step, saved, hidden));
  }

  public Map<String, List<StrategyBean>> getSavedStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getSavedStrategiesByCategory(_user);
    logFoundStrategies(strategies, "saved");
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getUnsavedStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getUnsavedStrategiesByCategory(_user);
    logFoundStrategies(strategies, "unsaved");
    return convertMap(strategies);
  }

  private void logFoundStrategies(Map<String, List<Strategy>> strategies, String condition) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loaded map of " + strategies.size() + " " + condition + " strategy categories:");
      int total = 0;
      for (Entry<String, List<Strategy>> entry : strategies.entrySet()) {
        LOG.debug("   " + entry.getKey() + ": " + entry.getValue().size() + " strategies.");
        total += entry.getValue().size();
      }
      LOG.debug("   Total: " + total);
    }
  }

  public Map<String, List<StrategyBean>> getRecentStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getRecentStrategiesByCategory(_user);
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getActiveStrategiesByCategory() {
    Map<String, List<Strategy>> strategies = StepUtilities.getActiveStrategiesByCategory(_user);
    return convertMap(strategies);
  }

  /**
   * @return { category/(type name)->{ activity->strategyBean } }
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<String, Map<String, List<StrategyBean>>> getStrategiesByCategoryActivity()
      throws WdkModelException, WdkUserException {
    Map<String, List<StrategyBean>> activeStrats = getActiveStrategiesByCategory();
    Map<String, List<StrategyBean>> savedStrats = getSavedStrategiesByCategory();
    Map<String, List<StrategyBean>> recentStrats = getRecentStrategiesByCategory();
    Map<String, Map<String, List<StrategyBean>>> categories = new LinkedHashMap<String, Map<String, List<StrategyBean>>>();
    WdkModel wdkModel = _user.getWdkModel();

    for (String rcName : activeStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClass(rcName);
      String category = recordClass.getDisplayName();
      List<StrategyBean> strategies = activeStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<StrategyBean>> activities = new LinkedHashMap<String, List<StrategyBean>>();
      activities.put("Opened", strategies);
      categories.put(category, activities);
    }

    for (String rcName : savedStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClass(rcName);
      String category = recordClass.getDisplayName();
      List<StrategyBean> strategies = savedStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<StrategyBean>> activities = categories.get(category);
      if (activities == null) {
        activities = new LinkedHashMap<String, List<StrategyBean>>();
        categories.put(category, activities);
      }
      activities.put("Saved", strategies);
    }

    for (String rcName : recentStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClass(rcName);
      String category = recordClass.getDisplayName();
      List<StrategyBean> strategies = recentStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<StrategyBean>> activities = categories.get(category);
      if (activities == null) {
        activities = new LinkedHashMap<String, List<StrategyBean>>();
        categories.put(category, activities);
      }
      activities.put("Recent", strategies);
    }
    return categories;
  }

  /**
   * @param displayId
   * @return
   * @see org.gusdb.wdk.model.user.User#getStep(int)
   */
  public StepBean getStep(long stepId) throws WdkModelException {
    return new StepBean(this, StepUtilities.getStep(_user, stepId));
  }

  /**
   * @param previousStep
   * @param childStep
   * @param operator
   * @param useBooleanFilter
   * @param filter
   * @return
   * @throws WdkUserException 
   * @see org.gusdb.wdk.model.user.User#createBooleanStep(org.gusdb.wdk.model.user.Step,
   *      org.gusdb.wdk.model.user.Step, org.gusdb.wdk.model.BooleanOperator, boolean,
   *      org.gusdb.wdk.model.AnswerFilterInstance)
   */
  public StepBean createBooleanStep(int strategyId, StepBean previousStep, StepBean childStep,
      String operator, String filterName) throws WdkModelException, WdkUserException {
    Step step = StepUtilities.createBooleanStep(_user, strategyId, previousStep.step, childStep.step, operator, filterName);
    return new StepBean(this, step);
  }

  public void setViewResults(String strategyKey, int stepId, int viewPagerOffset) {
    LOG.debug("setting view steps: " + strategyKey + ", " + stepId + ", " + viewPagerOffset);
    _user.getSession().setViewResults(strategyKey, stepId, viewPagerOffset);
  }

  public void resetViewResults() {
    _user.getSession().resetViewResults();
  }

  public String getViewStrategyId() {
    return _user.getSession().getViewStrategyKey();
  }

  public long getViewStepId() {
    return _user.getSession().getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return _user.getSession().getViewPagerOffset();
  }

  public StrategyBean[] getActiveStrategies() {
    List<StrategyBean> strategies = new ArrayList<StrategyBean>();
    for (Strategy strategy : _user.getSession().getActiveStrategies()) {
      strategies.add(new StrategyBean(this, strategy));
    }
    StrategyBean[] array = new StrategyBean[strategies.size()];
    strategies.toArray(array);
    return array;
  }

  /**
   * @param strategyKey
   * @return
   * @see org.gusdb.wdk.model.user.User#getStrategyOrder(java.lang.String)
   */
  public int getStrategyOrder(String strategyKey) {
    return _user.getSession().getStrategyOrder(strategyKey);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getActiveStrategyIds()
   */
  public long[] getActiveStrategyIds() {
    return _user.getSession().getActiveStrategyIds();
  }

  public void setStepId(String stepId) {
    _stepId = Integer.parseInt(stepId);
  }

  public StepBean getStepByCachedId() throws WdkModelException {
    return new StepBean(this, StepUtilities.getStep(_user, _stepId));
  }

  public StrategyBean copyStrategy(StrategyBean strategy, Map<Long, Long> stepIdMap)
      throws WdkUserException, WdkModelException {
    String name = strategy.getName();
    if (!name.toLowerCase().endsWith(", copy of")) name += ", Copy of";
    return copyStrategy(strategy, stepIdMap, name) ;
  }

  public StrategyBean copyStrategy(StrategyBean strategy, Map<Long, Long> stepIdMap, String name)
      throws WdkModelException, WdkUserException {
    return new StrategyBean(this, _wdkModel.getStepFactory().copyStrategy(strategy.strategy.getUser(), strategy.strategy, stepIdMap, name));
  }

  public void addToBasket(RecordClassBean recordClass, List<String[]> ids) throws WdkModelException {
    BasketFactory factory = _user.getWdkModel().getBasketFactory();
    factory.addToBasket(_user, recordClass.recordClass, ids);
  }

  public void addToBasket(StepBean step) throws WdkModelException, WdkUserException {
    BasketFactory factory = _user.getWdkModel().getBasketFactory();
    factory.addToBasket(_user, step.step);
  }

  public void removeFromBasket(RecordClassBean recordClass, List<String[]> ids) throws WdkModelException {
    BasketFactory factory = _user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(_user, recordClass.recordClass, ids);
  }

  public void removeFromBasket(StepBean step) throws WdkModelException, WdkUserException {
    BasketFactory factory = _user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(_user, step.step);
  }

  public void clearBasket(RecordClassBean recordClass) throws WdkModelException {
    BasketFactory factory = _user.getWdkModel().getBasketFactory();
    factory.clearBasket(_user, recordClass.recordClass);
  }

  public Map<RecordClassBean, Integer> getBasketCounts() throws WdkModelException {
    Map<RecordClass, Integer> counts = _wdkModel.getBasketFactory().getBasketCounts(_user);
    Map<RecordClassBean, Integer> beans = new LinkedHashMap<RecordClassBean, Integer>();
    for (RecordClass recordClass : counts.keySet()) {
      RecordClassBean bean = new RecordClassBean(recordClass);
      int count = counts.get(recordClass);
      beans.put(bean, count);
    }
    return beans;
  }

  public int getBasketCount() throws WdkModelException {
    Map<RecordClass, Integer> baskets = _wdkModel.getBasketFactory().getBasketCounts(_user);
    int total = 0;
    for (int count : baskets.values()) {
      total += count;
    }
    return total;
  }

  /**
   * @param recordClass
   * @param pkValues
   * @throws WdkUserException
   * @see org.gusdb.wdk.model.user.User#addToFavorite(org.gusdb.wdk.model.RecordClass, java.util.List)
   */
  @Deprecated // pending struts removal
  public void addToFavorite(RecordClassBean recordClass, List<Map<String, Object>> pkValues)
      throws WdkModelException, WdkUserException {
    _wdkModel.getFavoriteFactory().addToFavorite(_user, recordClass.recordClass, pkValues);
  }

  /**
   * @see org.gusdb.wdk.model.user.User#clearFavorite()
   */
  public void clearFavorite() throws WdkModelException {
    _wdkModel.getFavoriteFactory().deleteAllFavorites(_user);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteCount()
   */
  @Deprecated // pending struts removal
  public int getFavoriteCount() throws WdkModelException {
    return _wdkModel.getFavoriteFactory().getFavoriteCounts(_user);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavorites()
   */
  @Deprecated // pending struts removal
  public Map<RecordClassBean, List<FavoriteBean>> getFavorites() throws WdkModelException {
    Map<RecordClass, List<Favorite>> favorites = _wdkModel.getFavoriteFactory().getFavorites(_user);
    Map<RecordClassBean, List<FavoriteBean>> beans = new LinkedHashMap<RecordClassBean, List<FavoriteBean>>();
    for (RecordClass recordClass : favorites.keySet()) {
      List<FavoriteBean> beanList = new ArrayList<FavoriteBean>();
      List<Favorite> list = favorites.get(recordClass);
      for (Favorite favorite : list) {
        FavoriteBean bean = new FavoriteBean(favorite);
        beanList.add(bean);
      }
      beans.put(new RecordClassBean(recordClass), beanList);
    }
    return beans;
  }

  /**
   * @param recordClass
   * @param pkValues
   * @see org.gusdb.wdk.model.user.User#removeFromFavorite(org.gusdb.wdk.model.RecordClass, java.util.List)
   */
  @Deprecated // pending struts removal
  public void removeFromFavorite(RecordClassBean recordClass, List<Map<String, Object>> pkValues)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().removeFromFavorite(_user, recordClass.recordClass, pkValues);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param group
   * @see org.gusdb.wdk.model.user.User#setFavoriteGroups(org.gusdb.wdk.model.RecordClass, java.util.List,
   *      java.lang.String)
   */
  @Deprecated // pending struts removal
  public void setFavoriteGroups(RecordClassBean recordClass, List<Map<String, Object>> pkValues, String group)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().setGroups(_user, recordClass.recordClass, pkValues, group);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param note
   * @see org.gusdb.wdk.model.user.User#setFavoriteNotes(org.gusdb.wdk.model.RecordClass, java.util.List,
   *      java.lang.String)
   */
  @Deprecated // pending struts removal
  public void setFavoriteNotes(RecordClassBean recordClass, List<Map<String, Object>> pkValues, String note)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().setNotes(_user, recordClass.recordClass, pkValues, note);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteGroups()
   */
  @Deprecated // pending struts removal
  public String[] getFavoriteGroups() throws WdkModelException {
    return _wdkModel.getFavoriteFactory().getGroups(_user);
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  public int getBasketCount(List<String[]> records, RecordClassBean recordClass) throws WdkModelException {
    return _wdkModel.getBasketFactory().getBasketCounts(_user, records, recordClass.recordClass);
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  @Deprecated // pending struts removal
  public int getFavoriteCount(List<Map<String, Object>> records, RecordClassBean recordClass) {
    return _wdkModel.getFavoriteFactory().getFavoriteCount(_user, records, recordClass.recordClass);
  }

  /**
   * @return Map from question name to preferred summary view for that question
   * @throws Exception
   */
  public Map<String,SummaryView> getCurrentSummaryViews() {
    return exposeAsMap(questionName -> _user.getPreferences()
        .getCurrentSummaryView(_wdkModel.getQuestionOrFail(questionName)));
  }

  public void setCurrentSummaryView(QuestionBean question, SummaryView summaryView) throws WdkModelException {
    _user.getPreferences().setCurrentSummaryView(question._question, summaryView);
  }


  /**
   * @return Map from record class name to preferred record view for that recordClass
   * @throws Exception
   */
  public Map<String,RecordView> getCurrentRecordViews() {
    return exposeAsMap(recordClassName -> _user.getPreferences().getCurrentRecordView(recordClassName));
  }

  public void setCurrentRecordView(RecordClassBean recordClass, RecordView recordView)
      throws WdkModelException {
    _user.getPreferences().setCurrentRecordView(recordClass.recordClass, recordView);
  }

  private static <T> Map<String, T> exposeAsMap(FunctionWithException<String,T> getter) {
    return new HashMap<String, T>() {
      @Override
      public T get(Object objectName) {
        try {
          return getter.apply((String)objectName);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  public long getNewStrategyId() throws WdkModelException {
    return _wdkModel.getStepFactory().getNewStrategyId();
  }

}
