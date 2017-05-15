package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.FavoriteFactory;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.StepFactory.NameCheckInfo;
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

  private static Logger logger = Logger.getLogger(UserBean.class);

  private final User user;
  private final UserSession _userSession;
  private final WdkModel _wdkModel;

  private Question currentQuestion;
  private int stepId;

  public UserBean(User user) {
    this.user = user;
    _userSession = user.getSession();
    _wdkModel = user.getWdkModel();
    
  }

  public void setCurrentQuestion(QuestionBean question) {
    this.currentQuestion = question.question;
  }

  public User getUser() {
    return user;
  }

  public long getUserId() {
    return user.getUserId();
  }

  public String getEmail() {
    return user.getEmail();
  }

  public String getSignature() {
    return user.getSignature();
  }

  public boolean isGuest() {
    return user.isGuest();
  }

  public String getFirstName() {
    return user.getProfileProperties().get("firstName");
  }

  public String getMiddleName() {
    return user.getProfileProperties().get("middleName");
  }

  public String getLastName() {
    return user.getProfileProperties().get("lastName");
  }

  public String getOrganization() {
    return user.getProfileProperties().get("organization");
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
    return user.getPreferences().getGlobalPreferences();
  }

  public Map<String, String> getProjectPreferences() {
    return user.getPreferences().getProjectPreferences();
  }

  // =========================================================================
  // Methods for dataset operations
  // =========================================================================

  public DatasetBean getDataset(long userDatasetId) throws WdkModelException {
    return new DatasetBean(_wdkModel.getDatasetFactory().getDataset(user, userDatasetId));
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
    StepUtilities.deleteSteps(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.Answer)
   */
  public StepBean createStep(Long strategyId, QuestionBean question, Map<String, String> params,
      String filterName, boolean deleted, boolean validate, int assignedWeight) throws WdkModelException,
      WdkUserException {
    Step step = StepUtilities.createStep(user, strategyId, question.question, params, filterName, deleted, validate,
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
    Step[] steps = StepUtilities.getSteps(user);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistories()
   */
  public StepBean[] getInvalidSteps() throws WdkModelException {
    Step[] steps = StepUtilities.getInvalidSteps(user);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public void deleteInvalidSteps() throws WdkModelException {
    _wdkModel.getStepFactory().deleteInvalidSteps(user);
  }

  public void deleteInvalidStrategies() throws WdkModelException {
    _wdkModel.getStepFactory().deleteInvalidStrategies(user);
  }

  public Map<String, List<StepBean>> getStepsByCategory() throws WdkModelException {
    Map<String, List<Step>> steps = StepUtilities.getStepsByCategory(user);
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
    Step[] steps = StepUtilities.getSteps(user, recordClassName);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public StrategyBean getStrategy(int displayId) throws WdkUserException, WdkModelException {
    return new StrategyBean(this, StepUtilities.getStrategy(user, displayId));
  }

  public Map<String, List<StrategyBean>> getStrategiesByCategory() throws Exception {
    try {
      Map<String, List<Strategy>> strategies = StepUtilities.getStrategiesByCategory(user);
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
    int count = user.getWdkModel().getStepFactory().getPublicStrategyCount();
    logger.debug("Found number of public strats: " + count);
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
    return _wdkModel.getStepFactory().getStrategyCount(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistoryCount()
   */
  public int getStepCount() throws WdkModelException {
    return _wdkModel.getStepFactory().getStepCount(user);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getItemsPerPage()
   */
  public int getItemsPerPage() {
    return user.getPreferences().getItemsPerPage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setItemsPerPage(int)
   */
  public void setItemsPerPage(int itemsPerPage) throws WdkModelException {
    user.getPreferences().setItemsPerPage(itemsPerPage);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return this.user.toString();
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
    StepUtilities.deleteStrategies(user);
  }

  /**
   * @param strategyId
   * @see org.gusdb.wdk.model.user.User#deleteStrategy(int)
   */
  public void deleteStrategy(int strategyId) throws WdkModelException {
    StepUtilities.deleteStrategy(user, strategyId);
  }

  /**
   * @param rootAnswerChecksum
   * @return
   * @see org.gusdb.wdk.model.user.User#importStrategyByAnswer(java.lang.String)
   */
  public StrategyBean importStrategy(String strategyKey) throws WdkModelException, WdkUserException {
    Strategy strategy = StepUtilities.importStrategy(user, strategyKey);
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
    Map<String, List<Strategy>> strategies = StepUtilities.getSavedStrategiesByCategory(user);
    logFoundStrategies(strategies, "saved");
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getUnsavedStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getUnsavedStrategiesByCategory(user);
    logFoundStrategies(strategies, "unsaved");
    return convertMap(strategies);
  }

  private void logFoundStrategies(Map<String, List<Strategy>> strategies, String condition) {
    if (logger.isDebugEnabled()) {
      logger.debug("Loaded map of " + strategies.size() + " " + condition + " strategy categories:");
      int total = 0;
      for (Entry<String, List<Strategy>> entry : strategies.entrySet()) {
        logger.debug("   " + entry.getKey() + ": " + entry.getValue().size() + " strategies.");
        total += entry.getValue().size();
      }
      logger.debug("   Total: " + total);
    }
  }

  public Map<String, List<StrategyBean>> getRecentStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getRecentStrategiesByCategory(user);
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getActiveStrategiesByCategory() throws WdkModelException,
      WdkUserException {
    Map<String, List<Strategy>> strategies = StepUtilities.getActiveStrategiesByCategory(user);
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
    WdkModel wdkModel = user.getWdkModel();

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
  public StepBean getStep(int displayId) throws WdkModelException {
    return new StepBean(this, StepUtilities.getStep(user, displayId));
  }

  /**
   * @param previousStep
   * @param childStep
   * @param operator
   * @param useBooleanFilter
   * @param filter
   * @return
   * @see org.gusdb.wdk.model.user.User#createBooleanStep(org.gusdb.wdk.model.user.Step,
   *      org.gusdb.wdk.model.user.Step, org.gusdb.wdk.model.BooleanOperator, boolean,
   *      org.gusdb.wdk.model.AnswerFilterInstance)
   */
  public StepBean createBooleanStep(int strategyId, StepBean previousStep, StepBean childStep,
      String operator, String filterName) throws WdkModelException {
    Step step = StepUtilities.createBooleanStep(user, strategyId, previousStep.step, childStep.step, operator, filterName);
    return new StepBean(this, step);
  }

  public void setViewResults(String strategyKey, int stepId, int viewPagerOffset) {
    logger.debug("setting view steps: " + strategyKey + ", " + stepId + ", " + viewPagerOffset);
    user.getSession().setViewResults(strategyKey, stepId, viewPagerOffset);
  }

  public void resetViewResults() {
    user.getSession().resetViewResults();
  }

  public String getViewStrategyId() {
    return user.getSession().getViewStrategyKey();
  }

  public long getViewStepId() {
    return user.getSession().getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return user.getSession().getViewPagerOffset();
  }

  public StrategyBean[] getActiveStrategies() throws WdkUserException, WdkModelException {
    List<StrategyBean> strategies = new ArrayList<StrategyBean>();
    for (Strategy strategy : user.getSession().getActiveStrategies()) {
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
    return user.getSession().getStrategyOrder(strategyKey);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getActiveStrategyIds()
   */
  public long[] getActiveStrategyIds() {
    return user.getSession().getActiveStrategyIds();
  }

  public void setStepId(String stepId) {
    this.stepId = Integer.parseInt(stepId);
  }

  public StepBean getStepByCachedId() throws WdkModelException {
    return new StepBean(this, StepUtilities.getStep(user, stepId));
  }

  public StrategyBean copyStrategy(StrategyBean strategy, Map<Long, Long> stepIdMap)
      throws WdkUserException, WdkModelException {
    return new StrategyBean(this, _wdkModel.getStepFactory().copyStrategy(strategy.strategy, stepIdMap));
  }

  public StrategyBean copyStrategy(StrategyBean strategy, Map<Long, Long> stepIdMap, String name)
      throws WdkModelException, WdkUserException {
    return new StrategyBean(this, _wdkModel.getStepFactory().copyStrategy(strategy.strategy, stepIdMap, name));
  }

  public void addToBasket(RecordClassBean recordClass, List<String[]> ids) throws WdkModelException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.addToBasket(user, recordClass.recordClass, ids);
  }

  public void addToBasket(StepBean step) throws WdkModelException, WdkUserException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.addToBasket(user, step.step);
  }

  public void removeFromBasket(RecordClassBean recordClass, List<String[]> ids) throws WdkModelException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(user, recordClass.recordClass, ids);
  }

  public void removeFromBasket(StepBean step) throws WdkModelException, WdkUserException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(user, step.step);
  }

  public void clearBasket(RecordClassBean recordClass) throws SQLException, WdkModelException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.clearBasket(user, recordClass.recordClass);
  }

  public Map<RecordClassBean, Integer> getBasketCounts() throws WdkModelException {
    Map<RecordClass, Integer> counts = _wdkModel.getBasketFactory().getBasketCounts(user);
    Map<RecordClassBean, Integer> beans = new LinkedHashMap<RecordClassBean, Integer>();
    for (RecordClass recordClass : counts.keySet()) {
      RecordClassBean bean = new RecordClassBean(recordClass);
      int count = counts.get(recordClass);
      beans.put(bean, count);
    }
    return beans;
  }

  public int getBasketCount() throws WdkModelException {
    Map<RecordClass, Integer> baskets = _wdkModel.getBasketFactory().getBasketCounts(user);
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
  public void addToFavorite(RecordClassBean recordClass, List<Map<String, Object>> pkValues)
      throws WdkModelException, WdkUserException {
    _wdkModel.getFavoriteFactory().addToFavorite(user, recordClass.recordClass, pkValues);
  }

  /**
   * @see org.gusdb.wdk.model.user.User#clearFavorite()
   */
  public void clearFavorite() throws WdkModelException {
    _wdkModel.getFavoriteFactory().clearFavorite(user);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteCount()
   */
  public int getFavoriteCount() throws WdkModelException {
    return _wdkModel.getFavoriteFactory().getFavoriteCounts(user);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavorites()
   */
  public Map<RecordClassBean, List<FavoriteBean>> getFavorites() throws WdkModelException {
    Map<RecordClass, List<Favorite>> favorites = _wdkModel.getFavoriteFactory().getFavorites(user);
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
  public void removeFromFavorite(RecordClassBean recordClass, List<Map<String, Object>> pkValues)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().removeFromFavorite(user, recordClass.recordClass, pkValues);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param group
   * @see org.gusdb.wdk.model.user.User#setFavoriteGroups(org.gusdb.wdk.model.RecordClass, java.util.List,
   *      java.lang.String)
   */
  public void setFavoriteGroups(RecordClassBean recordClass, List<Map<String, Object>> pkValues, String group)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().setGroups(user, recordClass.recordClass, pkValues, group);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param note
   * @see org.gusdb.wdk.model.user.User#setFavoriteNotes(org.gusdb.wdk.model.RecordClass, java.util.List,
   *      java.lang.String)
   */
  public void setFavoriteNotes(RecordClassBean recordClass, List<Map<String, Object>> pkValues, String note)
      throws WdkModelException {
    _wdkModel.getFavoriteFactory().setNotes(user, recordClass.recordClass, pkValues, note);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteGroups()
   */
  public String[] getFavoriteGroups() throws WdkModelException {
    return _wdkModel.getFavoriteFactory().getGroups(user);
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  public int getBasketCount(List<String[]> records, RecordClassBean recordClass) throws WdkModelException {
    return _wdkModel.getBasketFactory().getBasketCounts(user, records, recordClass.recordClass);
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  public int getFavoriteCount(List<Map<String, Object>> records, RecordClassBean recordClass)
      throws WdkModelException {
    return getFavoriteCount(_wdkModel.getFavoriteFactory(), user, records, recordClass.recordClass);
  }

  private int getFavoriteCount(FavoriteFactory favoriteFactory, User user,
      List<Map<String, Object>> records, RecordClass recordClass)
      throws WdkModelException {
    int count = 0;
    for (Map<String, Object> item : records) {
      boolean inFavs = favoriteFactory.isInFavorite(user, recordClass, item);
      if (logger.isDebugEnabled()) {
        logger.debug("Is " + convert(item) + " in favorites? " + inFavs);
      }
      if (inFavs) {
        count++;
      }
    }
    return count;
  }

  private String convert(Map<String, Object> item) {
    StringBuilder sb = new StringBuilder("Map { ");
    for (String s : item.keySet()) {
      sb.append("{ ").append(s).append(", ").append(item.get(s)).append(" },");
    }
    sb.append(" }");
    return sb.toString();
  }

  public SummaryView getCurrentSummaryView() throws Exception {
    try {
      return user.getPreferences().getCurrentSummaryView(currentQuestion);
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  public void setCurrentSummaryView(QuestionBean question, SummaryView summaryView) throws WdkModelException {
    user.getPreferences().setCurrentSummaryView(question.question, summaryView);
  }

  public RecordView getCurrentRecordView() throws Exception {
    try {
      return user.getPreferences().getCurrentRecordView(currentQuestion.getRecordClass());
    }
    catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  public void setCurrentRecordView(RecordClassBean recordClass, RecordView recordView)
      throws WdkModelException {
    user.getPreferences().setCurrentRecordView(recordClass.recordClass, recordView);
  }

  public long getNewStrategyId() throws WdkModelException {
    return _wdkModel.getStepFactory().getNewStrategyId();
  }

}
