package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerFilterInstance;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetFactory;
import org.gusdb.wdk.model.query.BooleanExpression;
import org.gusdb.wdk.model.query.BooleanOperator;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.json.JSONException;

/**
 * @author xingao
 * 
 */
public class User /* implements Serializable */{

  public final static String PREF_ITEMS_PER_PAGE = "preference_global_items_per_page";
  public final static String PREF_REMOTE_KEY = "preference_remote_key";

  public final static String SORTING_ATTRIBUTES_SUFFIX = "_sort";
  public final static String SUMMARY_ATTRIBUTES_SUFFIX = "_summary";

  private final static String SUMMARY_VIEW_PREFIX = "summary_view_";
  private final static String RECORD_VIEW_PREFIX = "record_view_";

  public static final int SORTING_LEVEL = 3;

  private Logger logger = Logger.getLogger(User.class);

  private WdkModel wdkModel;
  private UserFactory userFactory;
  private StepFactory stepFactory;
  private DatasetFactory datasetFactory;
  private int userId;
  private String signature;

  // basic user information
  private String email;
  private String lastName;
  private String firstName;
  private String middleName;
  private String title;
  private String organization;
  private String department;
  private String address;
  private String city;
  private String state;
  private String zipCode;
  private String phoneNumber;
  private String country;
  private String openId;

  private Set<String> userRoles;
  private boolean guest = true;

  /**
   * the preferences for the user: <prefName, prefValue>. It only contains the
   * preferences for the current project
   */
  private Map<String, String> globalPreferences;
  private Map<String, String> projectPreferences;

  // keep track in session , but don't serialize:
  // currently open strategies
  private transient ActiveStrategyFactory activeStrategyFactory;

  // keep track of most recent front end action
  private String frontAction = null;
  private Integer frontStrategy = null;
  private Integer frontStep = null;

  User(WdkModel model, int userId, String email, String signature) {
    this.userId = userId;
    this.email = email;
    this.signature = signature;

    userRoles = new LinkedHashSet<String>();

    globalPreferences = new LinkedHashMap<String, String>();
    projectPreferences = new LinkedHashMap<String, String>();

    setWdkModel(model);

    activeStrategyFactory = new ActiveStrategyFactory(this);
  }

  /**
   * The setter is called when the session is restored (deserialized)
   * 
   * @param wdkModel
   * @throws WdkUserException
   */
  public void setWdkModel(WdkModel wdkModel) {
    this.wdkModel = wdkModel;
    this.userFactory = wdkModel.getUserFactory();
    this.stepFactory = wdkModel.getStepFactory();
    this.datasetFactory = wdkModel.getDatasetFactory();
  }

  public WdkModel getWdkModel() {
    return this.wdkModel;
  }

  /**
   * @return Returns the userId.
   */
  public int getUserId() {
    return userId;
  }

  /**
   * @return Returns the signature.
   */
  public String getSignature() {
    return signature;
  }

  /**
   * @return Returns the email.
   */
  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  /**
   * @return Returns the address.
   */
  public String getAddress() {
    return address;
  }

  /**
   * @param address
   *          The address to set.
   */
  public void setAddress(String address) {
    this.address = address;
  }

  /**
   * @return Returns the city.
   */
  public String getCity() {
    return city;
  }

  /**
   * @param city
   *          The city to set.
   */
  public void setCity(String city) {
    this.city = city;
  }

  /**
   * @return Returns the country.
   */
  public String getCountry() {
    return country;
  }

  /**
   * @param country
   *          The country to set.
   */
  public void setCountry(String country) {
    this.country = country;
  }

  /**
   * @return Returns the department.
   */
  public String getDepartment() {
    return department;
  }

  /**
   * @param department
   *          The department to set.
   */
  public void setDepartment(String department) {
    this.department = department;
  }

  /**
   * @return Returns the firstName.
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * @param firstName
   *          The firstName to set.
   */
  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  /**
   * @return Returns the lastName.
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * @param lastName
   *          The lastName to set.
   */
  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  /**
   * @return Returns the middleName.
   */
  public String getMiddleName() {
    return middleName;
  }

  /**
   * @param middleName
   *          The middleName to set.
   */
  public void setMiddleName(String middleName) {
    this.middleName = middleName;
  }

  /**
   * @return User's full name (first middle last)
   */
  public String getDisplayName() {
    return (formatNamePart(getFirstName()) +
            formatNamePart(getMiddleName()) +
            formatNamePart(getLastName())).trim();
  }
  
  private String formatNamePart(String namePart) {
    return (namePart == null || namePart.isEmpty() ? "" : " " + namePart.trim());
  }
  
  /**
   * @return Returns the organization.
   */
  public String getOrganization() {
    return organization;
  }

  /**
   * @param organization
   *          The organization to set.
   */
  public void setOrganization(String organization) {
    this.organization = organization;
  }

  /**
   * @return Returns the phoneNumber.
   */
  public String getPhoneNumber() {
    return phoneNumber;
  }

  /**
   * @param phoneNumber
   *          The phoneNumber to set.
   */
  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  /**
   * @return Returns the state.
   */
  public String getState() {
    return state;
  }

  /**
   * @param state
   *          The state to set.
   */
  public void setState(String state) {
    this.state = state;
  }

  /**
   * @return Returns the title.
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title
   *          The title to set.
   */
  public void setTitle(String title) {
    this.title = title;
  }

  /**
   * @return Returns the zipCode.
   */
  public String getZipCode() {
    return zipCode;
  }

  /**
   * @param zipCode
   *          The zipCode to set.
   */
  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }

  /**
   * @return user's OpenID
   */
  public String getOpenId() {
    return openId;
  }

  /**
   * @param openId
   *          user's OpenID
   */
  public void setOpenId(String openId) {
    this.openId = openId;
  }

  /**
   * @return Returns the guest.
   * @throws WdkUserException
   */
  public boolean isGuest() {
    return guest;
  }

  /**
   * @return Returns the userRole.
   */
  public String[] getUserRoles() {
    String[] roles = new String[userRoles.size()];
    userRoles.toArray(roles);
    return roles;
  }

  /**
   * @param userRole
   *          The userRole to set.
   */
  public void addUserRole(String userRole) {
    this.userRoles.add(userRole);
  }

  public void removeUserRole(String userRole) {
    userRoles.remove(userRole);
  }

  public boolean containsUserRole(String userRole) {
    return userRoles.contains(userRole);
  }

  void setUserRole(Set<String> roles) {
    this.userRoles.clear();
    this.userRoles.addAll(roles);
  }

  public String getFrontAction() {
    return frontAction;
  }

  public Integer getFrontStrategy() {
    return frontStrategy;
  }

  public Integer getFrontStep() {
    return frontStep;
  }

  public void setFrontAction(String frontAction) {
    this.frontAction = frontAction;
  }

  public void setFrontStrategy(int frontStrategy) {
    System.out.println("Setting frontStrategy.");
    this.frontStrategy = Integer.valueOf(frontStrategy);
    System.out.println("Done.");
  }

  public void setFrontStep(int frontStep) {
    this.frontStep = Integer.valueOf(frontStep);
  }

  public void resetFrontAction() {
    frontAction = null;
    frontStrategy = null;
    frontStep = null;
  }

  /**
   * @param guest
   *          The guest to set.
   */
  void setGuest(boolean guest) {
    this.guest = guest;
  }

  /**
   * Create a step from the existing answerValue
   * 
   * @param answerValue
   * @return
   * @throws JSONException
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   * @throws NoSuchAlgorithmException
   */
  Step createStep(AnswerValue answerValue, boolean deleted, int assignedWeight)
      throws WdkModelException {
    Question question = answerValue.getQuestion();
    Map<String, String> paramValues = answerValue.getIdsQueryInstance().getParamStableValues();
    AnswerFilterInstance filter = answerValue.getFilter();
    int startIndex = answerValue.getStartIndex();
    int endIndex = answerValue.getEndIndex();

    return createStep(question, paramValues, filter, startIndex, endIndex,
        deleted, true, assignedWeight);
  }

  public Step createStep(Question question, Map<String, String> paramValues,
      String filterName, boolean deleted, boolean validate, int assignedWeight)
      throws WdkModelException {
    AnswerFilterInstance filter = null;
    RecordClass recordClass = question.getRecordClass();
    if (filterName != null) {
      filter = recordClass.getFilter(filterName);
    } else filter = recordClass.getDefaultFilter();
    return createStep(question, paramValues, filter, deleted, validate,
        assignedWeight);
  }

  public Step createStep(Question question, Map<String, String> paramValues,
      AnswerFilterInstance filter, boolean deleted, boolean validate,
      int assignedWeight) throws WdkModelException {
    int endIndex = getItemsPerPage();
    return createStep(question, paramValues, filter, 1, endIndex, deleted,
        validate, assignedWeight);
  }

  public Step createStep(Question question,
      Map<String, String> paramValues, AnswerFilterInstance filter,
      int pageStart, int pageEnd, boolean deleted, boolean validate,
      int assignedWeight) throws WdkModelException {
    Step step = stepFactory.createStep(this, question, paramValues, filter,
        pageStart, pageEnd, deleted, validate, assignedWeight);
    return step;
  }

  public Strategy createStrategy(Step step, boolean saved)
      throws WdkModelException, WdkUserException {
    return createStrategy(step, null, null, saved, null, false, false);
  }

  public Strategy createStrategy(Step step, boolean saved, boolean hidden)
      throws WdkModelException, WdkUserException {
    return createStrategy(step, null, null, saved, null, hidden, false);
  }

  // Transitional method...how to handle savedName properly?
  // Probably by expecting it if a name is given?
  public Strategy createStrategy(Step step, String name, boolean saved)
      throws WdkModelException, WdkUserException {
    return createStrategy(step, name, null, saved, null, false, false);
  }

  public Strategy createStrategy(Step step, String name,
      String savedName, boolean saved, String description, boolean hidden, boolean isPublic)
      throws WdkModelException, WdkUserException {
    Strategy strategy = stepFactory.createStrategy(this, step, name, savedName,
        saved, description, hidden, isPublic);

    // set the view to this one
    String strategyKey = Integer.toString(strategy.getStrategyId());
    this.activeStrategyFactory.openActiveStrategy(strategyKey);
    if (strategy.isValid()) {
      this.activeStrategyFactory.setViewStrategyKey(strategyKey);
      this.activeStrategyFactory.setViewStepId(step.getStepId());
    }
    return strategy;
  }

  /**
   * this method is only called by UserFactory during the login process, it
   * merges the existing history of the current guest user into the logged-in
   * user.
   * 
   * @param user
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public void mergeUser(User user) throws WdkModelException, WdkUserException {
    // TEST
    logger.debug("Merging user #" + user.getUserId() + " into user #" + userId
        + "...");

    // first of all we import all the strategies
    Set<Integer> importedSteps = new LinkedHashSet<Integer>();
    Map<Integer, Integer> strategiesMap = new LinkedHashMap<Integer, Integer>();
    Map<Integer, Integer> stepsMap = new LinkedHashMap<Integer, Integer>();
    for (Strategy strategy : user.getStrategies()) {
      // the root step is considered as imported
      Step rootStep = strategy.getLatestStep();

      // import the strategy
      Strategy newStrategy = this.importStrategy(strategy, stepsMap);

      importedSteps.add(rootStep.getStepId());
      strategiesMap.put(strategy.getStrategyId(), newStrategy.getStrategyId());
    }

    // the current implementation can only keep the root level of the
    // imported strategies open;
    int[] oldActiveStrategies = user.activeStrategyFactory.getRootStrategies();
    for (int oldStrategyId : oldActiveStrategies) {
      int newStrategyId = strategiesMap.get(oldStrategyId);
      activeStrategyFactory.openActiveStrategy(Integer.toString(newStrategyId));
    }

    // then import the steps that do not belong to any strategies; that is,
    // only the root steps who are not imported yet.
    for (Step step : user.getSteps()) {
      if (stepFactory.isStepDepended(step.getStepId())) continue;
      if (importedSteps.contains(step.getStepId())) continue;

      stepFactory.importStep(this, step, stepsMap);
    }

    // if a front action is specified, copy it over and update ids

    if (user.getFrontAction() != null) {
      setFrontAction(user.getFrontAction());
      if (strategiesMap.containsKey(user.getFrontStrategy())) {
        setFrontStrategy(strategiesMap.get(user.getFrontStrategy()));
      }
      if (stepsMap.containsKey(user.getFrontStep())) {
        setFrontStep(stepsMap.get(user.getFrontStep()));
      }
    }
  }

  public Map<Integer, Step> getStepsMap() throws WdkModelException {
    logger.debug("loading steps...");
    Map<Integer, Step> invalidSteps = new LinkedHashMap<Integer, Step>();
    Map<Integer, Step> allSteps = stepFactory.loadSteps(this, invalidSteps);

    return allSteps;
  }

  public Map<Integer, Strategy> getStrategiesMap() throws WdkModelException {
    logger.debug("loading strategies...");
    Map<Integer, Strategy> invalidStrategies = new LinkedHashMap<Integer, Strategy>();
    Map<Integer, Strategy> strategies = stepFactory.loadStrategies(this,
        invalidStrategies);

    return strategies;
  }

  public Map<String, List<Step>> getStepsByCategory() throws WdkModelException {
    Map<Integer, Step> steps = getStepsMap();
    Map<String, List<Step>> category = new LinkedHashMap<String, List<Step>>();
    for (Step step : steps.values()) {
      // not include the histories marked as 'deleted'
      if (step.isDeleted()) continue;

      String type = step.getRecordClass().getFullName();
      List<Step> list;
      if (category.containsKey(type)) {
        list = category.get(type);
      } else {
        list = new ArrayList<Step>();
        category.put(type, list);
      }
      list.add(step);
    }
    return category;
  }

  public Strategy[] getInvalidStrategies() throws WdkModelException {
    try {
      Map<Integer, Strategy> strategies = new LinkedHashMap<Integer, Strategy>();
      stepFactory.loadStrategies(this, strategies);

      Strategy[] array = new Strategy[strategies.size()];
      strategies.values().toArray(array);
      return array;
    } catch (WdkModelException ex) {
      System.out.println(ex);
      throw ex;
    }
  }

  public Strategy[] getStrategies() throws WdkModelException {
    Map<Integer, Strategy> map = getStrategiesMap();
    Strategy[] array = new Strategy[map.size()];
    map.values().toArray(array);
    return array;
  }

  public Map<String, List<Strategy>> getStrategiesByCategory()
      throws WdkModelException {
    Map<Integer, Strategy> strategies = getStrategiesMap();
    return formatStrategiesByRecordClass(strategies.values());
  }

  public Map<String, List<Strategy>> getUnsavedStrategiesByCategory()
      throws WdkModelException {
    List<Strategy> strategies = stepFactory.loadStrategies(this, false, false);
    return formatStrategiesByRecordClass(strategies);
  }

  /**
   * @return
   * @throws WdkUserException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
   * @throws JSONException
   * @throws SQLException
   */
  public Map<String, List<Strategy>> getSavedStrategiesByCategory()
      throws WdkModelException {
    List<Strategy> strategies = stepFactory.loadStrategies(this, true, false);
    return formatStrategiesByRecordClass(strategies);
  }

  public Map<String, List<Strategy>> getRecentStrategiesByCategory()
      throws WdkModelException {
    List<Strategy> strategies = stepFactory.loadStrategies(this, false, true);
    return formatStrategiesByRecordClass(strategies);
  }

  public Map<String, List<Strategy>> getActiveStrategiesByCategory()
      throws WdkModelException, WdkUserException {
    Strategy[] strategies = getActiveStrategies();
    List<Strategy> list = new ArrayList<Strategy>();
    for (Strategy strategy : strategies)
      list.add(strategy);
    return formatStrategiesByRecordClass(list);
  }

  private Map<String, List<Strategy>> formatStrategiesByRecordClass(
      Collection<Strategy> strategies) throws WdkModelException {
    Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
    for (RecordClassSet rcSet : wdkModel.getAllRecordClassSets()) {
      for (RecordClass recordClass : rcSet.getRecordClasses()) {
        String type = recordClass.getFullName();
        category.put(type, new ArrayList<Strategy>());
      }
    }
    for (Strategy strategy : strategies) {
      String rcName = strategy.getRecordClass().getFullName();
      List<Strategy> list;
      if (category.containsKey(rcName)) {
        list = category.get(rcName);
      } else {
        list = new ArrayList<Strategy>();
        category.put(rcName, list);
      }
      category.get(rcName).add(strategy);
    }
    return category;
  }

  public Map<Integer, Step> getStepsMap(String rcName) throws WdkModelException {
    Map<Integer, Step> steps = getStepsMap();
    Map<Integer, Step> selected = new LinkedHashMap<Integer, Step>();
    for (int stepDisplayId : steps.keySet()) {
      Step step = steps.get(stepDisplayId);
      if (rcName.equalsIgnoreCase(step.getRecordClass().getFullName()))
        selected.put(stepDisplayId, step);
    }
    return selected;
  }

  public Step[] getSteps(String rcName) throws WdkModelException {
    Map<Integer, Step> map = getStepsMap(rcName);
    Step[] array = new Step[map.size()];
    map.values().toArray(array);
    return array;
  }

  public Step[] getSteps() throws WdkModelException {
    Map<Integer, Step> map = getStepsMap();
    Step[] array = new Step[map.size()];
    map.values().toArray(array);
    return array;
  }

  public Step[] getInvalidSteps() throws WdkModelException {
    Map<Integer, Step> steps = new LinkedHashMap<Integer, Step>();
    stepFactory.loadSteps(this, steps);

    Step[] array = new Step[steps.size()];
    steps.values().toArray(array);
    return array;
  }

  public Map<Integer, Strategy> getStrategiesMap(String rcName)
      throws WdkModelException {
    Map<Integer, Strategy> strategies = getStrategiesMap();
    Map<Integer, Strategy> selected = new LinkedHashMap<Integer, Strategy>();
    for (int strategyId : strategies.keySet()) {
      Strategy strategy = strategies.get(strategyId);
      if (rcName.equalsIgnoreCase(strategy.getRecordClass().getFullName()))
        selected.put(strategyId, strategy);
    }
    return selected;
  }

  public Strategy[] getStrategies(String dataType) throws WdkModelException {
    Map<Integer, Strategy> map = getStrategiesMap(dataType);
    Strategy[] array = new Strategy[map.size()];
    map.values().toArray(array);
    return array;
  }

  public Step getStep(int stepID) throws WdkModelException {
    return stepFactory.loadStep(this, stepID);
  }

  public Strategy getStrategy(int userStrategyId) throws WdkModelException,
      WdkUserException {
    return getStrategy(userStrategyId, true);
  }

  public Strategy getStrategy(int userStrategyId, boolean allowDeleted)
      throws WdkModelException, WdkUserException {
    return stepFactory.loadStrategy(this, userStrategyId, allowDeleted);
  }

  public void deleteSteps() throws WdkModelException {
    deleteSteps(false);
  }

  public void deleteSteps(boolean allProjects) throws WdkModelException {
    stepFactory.deleteSteps(this, allProjects);
  }

  public void deleteInvalidSteps() throws WdkModelException {
    stepFactory.deleteInvalidSteps(this);
  }

  public void deleteInvalidStrategies() throws WdkModelException {
    stepFactory.deleteInvalidStrategies(this);
  }

  public void deleteStep(int displayId) throws WdkModelException {
    stepFactory.deleteStep(displayId);
  }

  public void deleteStrategy(int strategyId) throws WdkModelException {
    String strategyKey = Integer.toString(strategyId);
    int order = activeStrategyFactory.getOrder(strategyKey);
    if (order > 0) activeStrategyFactory.closeActiveStrategy(strategyKey);
    stepFactory.deleteStrategy(strategyId);
  }

  public void deleteStrategies() throws WdkModelException {
    activeStrategyFactory.clear();
    deleteStrategies(false);
  }

  public void deleteStrategies(boolean allProjects) throws WdkModelException {
    activeStrategyFactory.clear();
    stepFactory.deleteStrategies(this, allProjects);
  }

  public int getStepCount() throws WdkModelException {
    return stepFactory.getStepCount(this);
  }

  public int getStrategyCount() throws WdkModelException {
    return stepFactory.getStrategyCount(this);
  }

  public void setProjectPreference(String prefName, String prefValue) {
    if (prefValue == null) prefValue = prefName;
    projectPreferences.put(prefName, prefValue);
  }

  public void unsetProjectPreference(String prefName) {
    projectPreferences.remove(prefName);
  }

  public Map<String, String> getProjectPreferences() {
    return new LinkedHashMap<String, String>(projectPreferences);
  }

  public String getProjectPreference(String key) {
    return projectPreferences.get(key);
  }

  public void setGlobalPreference(String prefName, String prefValue) {
    if (prefValue == null) prefValue = prefName;
    globalPreferences.put(prefName, prefValue);
  }

  public String getGlobalPreference(String key) {
    return globalPreferences.get(key);
  }

  public void unsetGlobalPreference(String prefName) {
    globalPreferences.remove(prefName);
  }

  public Map<String, String> getGlobalPreferences() {
    return new LinkedHashMap<String, String>(globalPreferences);
  }

  public void clearPreferences() {
    globalPreferences.clear();
    projectPreferences.clear();
  }

  void setPreferences(List<Map<String, String>> preferences) {
    clearPreferences();
    globalPreferences.putAll(preferences.get(0));
    projectPreferences.putAll(preferences.get(1));
  }

  public void changePassword(String oldPassword, String newPassword,
      String confirmPassword) throws WdkUserException {
    userFactory.changePassword(email, oldPassword, newPassword, confirmPassword);
  }

  DatasetFactory getDatasetFactory() {
    return datasetFactory;
  }


  public Dataset getDataset(int datasetId) throws WdkModelException {
    return datasetFactory.getDataset(this, datasetId);
  }

  /**
   * set this method synchronized to make sure the preferences are not updated
   * at the same time.
   * 
   * @throws WdkModelException
   */
  public void save() throws WdkModelException {
    userFactory.saveUser(this);
  }

  public int getItemsPerPage() {
    String prefValue = getGlobalPreference(User.PREF_ITEMS_PER_PAGE);
    int itemsPerPage = (prefValue == null) ? 20 : Integer.parseInt(prefValue);
    return itemsPerPage;
  }

  public void setItemsPerPage(int itemsPerPage) throws WdkModelException {
    if (itemsPerPage <= 0) itemsPerPage = 20;
    else if (itemsPerPage > 1000) itemsPerPage = 1000;
    setGlobalPreference(User.PREF_ITEMS_PER_PAGE,
        Integer.toString(itemsPerPage));
    save();
  }

  public Step combineStep(String expression) throws WdkModelException {
    return combineStep(expression, false, false);
  }

  public Step combineStep(String expression, boolean useBooleanFilter,
      boolean deleted) throws WdkModelException {
    logger.debug("Boolean expression: " + expression);
    BooleanExpression exp = new BooleanExpression(this);
    Step step = exp.parseExpression(expression, useBooleanFilter);
    step.setBooleanExpression(expression);
    AnswerValue answerValue = step.getAnswerValue();

    logger.debug("Boolean answer size: " + answerValue.getResultSize());

    // save summary list, if no summary list exists
    String summaryKey = answerValue.getQuestion().getFullName()
        + SUMMARY_ATTRIBUTES_SUFFIX;
    if (!projectPreferences.containsKey(summaryKey)) {
      Map<String, AttributeField> summary = answerValue.getSummaryAttributeFieldMap();
      StringBuffer sb = new StringBuffer();
      for (String attrName : summary.keySet()) {
        if (sb.length() != 0) sb.append(",");
        sb.append(attrName);
      }
      projectPreferences.put(summaryKey, sb.toString());
      save();
    }

    return step;
  }

  public void validateExpression(String expression) throws WdkModelException {
    // construct BooleanQuestionNode
    BooleanExpression be = new BooleanExpression(this);
    be.parseExpression(expression, false);
  }

  public Map<String, Boolean> getSortingAttributes(String questionFullName)
      throws WdkModelException {
    Question question = wdkModel.getQuestion(questionFullName);

    String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX;
    String sortingChecksum = projectPreferences.get(sortKey);
    if (sortingChecksum == null) return null;

    QueryFactory queryFactory = wdkModel.getQueryFactory();
    Map<String, Boolean> sortingAttributes = queryFactory.getSortingAttributes(sortingChecksum);
    if (sortingAttributes != null) {
      // remove invalid columns
      Map<String, AttributeField> attributes = question.getAttributeFieldMap();
      String[] names = new String[sortingAttributes.size()];
      sortingAttributes.keySet().toArray(names);
      for (String name : names) {
        if (!attributes.containsKey(name)) sortingAttributes.remove(name);
      }

      return sortingAttributes;
    }

    // user doesn't have preference, use the default one of the question
    return question.getSortingAttributeMap();
  }

  public Map<String, Boolean> getSortingAttributesByChecksum(
      String sortingChecksum) throws WdkModelException {
    if (sortingChecksum == null) return null;
    QueryFactory queryFactory = wdkModel.getQueryFactory();
    return queryFactory.getSortingAttributes(sortingChecksum);
  }

  public String addSortingAttribute(String questionFullName, String attrName,
      boolean ascending) throws WdkModelException {
    Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>();
    sortingMap.put(attrName, ascending);
    Map<String, Boolean> previousMap = getSortingAttributes(questionFullName);
    if (previousMap == null)
      previousMap = new LinkedHashMap<String, Boolean>();
    for (String aName : previousMap.keySet()) {
      if (!sortingMap.containsKey(aName))
        sortingMap.put(aName, previousMap.get(aName));
    }

    // save and get sorting checksum
    QueryFactory queryFactory = wdkModel.getQueryFactory();
    String sortingChecksum = queryFactory.makeSortingChecksum(sortingMap);

    applySortingChecksum(questionFullName, sortingChecksum);
    return sortingChecksum;
  }

  public void applySortingChecksum(String questionFullName,
      String sortingChecksum) {
    String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX;
    projectPreferences.put(sortKey, sortingChecksum);
  }

  public String[] getSummaryAttributes(String questionFullName)
      throws WdkModelException {
    Question question = wdkModel.getQuestion(questionFullName);

    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
    String summaryChecksum = projectPreferences.get(summaryKey);
    String[] summary = null;
    boolean savedSummary = false;
    if (summaryChecksum != null && summaryChecksum.length() > 0) {
      // get summary list
      QueryFactory queryFactory = wdkModel.getQueryFactory();
      summary = queryFactory.getSummaryAttributes(summaryChecksum);
      if (summary != null && summary.length > 0) {
        savedSummary = true;

        // ignore invalid attribute names
        Map<String, AttributeField> attributes = question.getAttributeFieldMap();
        List<String> list = new ArrayList<String>();
        for (String attribute : summary) {
          if (attributes.containsKey(attribute)) list.add(attribute);
        }
        summary = new String[list.size()];
        list.toArray(summary);
      }
    }

    // if user does't have preference, use the default of the question
    if (!savedSummary) {
      Map<String, AttributeField> attributes = question.getSummaryAttributeFieldMap();
      summary = new String[attributes.size()];
      attributes.keySet().toArray(summary);
    }

    // always display weight for combined questions
    // if (question.getQuery().isCombined()) {
    // // check if weight already exists
    // boolean hasWeight = false;
    // for (String name : summary) {
    // if (name.equals(Utilities.COLUMN_WEIGHT)) {
    // hasWeight = true;
    // break;
    // }
    // }
    //
    // // add weight to the last item if it's not included
    // if (!hasWeight) {
    // String[] array = new String[summary.length + 1];
    // System.arraycopy(summary, 0, array, 0, summary.length);
    // array[summary.length] = Utilities.COLUMN_WEIGHT;
    // summary = array;
    // summaryChecksum = null;
    // }
    // }

    return summary;
  }

  public void resetSummaryAttributes(String questionFullName) {
    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
    projectPreferences.remove(summaryKey);
    logger.debug("reset used weight to false");
  }

  public String setSummaryAttributes(String questionFullName,
      String[] summaryNames) throws WdkModelException {
    // make sure all the attribute names exist
    Question question = (Question) wdkModel.resolveReference(questionFullName);
    Map<String, AttributeField> attributes = question.getAttributeFieldMap();

    // instead throwing out an error, just ignore the invalid columns
    List<String> validNames = new ArrayList<String>();
    for (String name : summaryNames) {
      if (attributes.containsKey(name)) validNames.add(name);
    }
    summaryNames = validNames.toArray(new String[0]);

    logger.debug("Saving Valid summary names: " + validNames);
    // create checksum
    QueryFactory queryFactory = wdkModel.getQueryFactory();
    String summaryChecksum = queryFactory.makeSummaryChecksum(summaryNames);

    applySummaryChecksum(questionFullName, summaryChecksum);
    return summaryChecksum;
  }

  /**
   * The method replace the previous checksum with the given one.
   * 
   * @param summaryChecksum
   * @throws WdkUserException
   * @throws WdkModelException
   * @throws NoSuchAlgorithmException
   */
  public void applySummaryChecksum(String questionFullName,
      String summaryChecksum) {
    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
    projectPreferences.put(summaryKey, summaryChecksum);
  }

  public String createRemoteKey() throws WdkUserException, WdkModelException {
    // user can remote key only if he/she is logged in
    if (isGuest())
      throw new WdkUserException("Guest user cannot create remote key.");

    // the key is a combination of user id and current time
    Date now = new Date();

    String key = Long.toString(now.getTime()) + "->" + Integer.toString(userId);
    key = UserFactory.encrypt(key);
    // save the remote key
    String saveKey = Long.toString(now.getTime()) + "<-" + key;
    globalPreferences.put(PREF_REMOTE_KEY, saveKey);
    save();

    return key;
  }

  public void verifyRemoteKey(String remoteKey) throws WdkUserException {
    // get save key and creating time
    String saveKey = globalPreferences.get(PREF_REMOTE_KEY);
    if (saveKey == null)
      throw new WdkUserException(
          "Remote login failed. The remote key doesn't exist.");
    String[] parts = saveKey.split("<-");
    if (parts.length != 2)
      throw new WdkUserException(
          "Remote login failed. The remote key is invalid.");
    long createTime = Long.parseLong(parts[0]);
    String createKey = parts[1].trim();

    // verify remote key
    if (!createKey.equals(remoteKey))
      throw new WdkUserException(
          "Remote login failed. The remote key doesn't match.");

    // check if the remote key is expired. There is an mandatory 10 minutes
    // expiration time for the remote key
    long now = (new Date()).getTime();
    if (Math.abs(now - createTime) >= (10 * 60 * 1000))
      throw new WdkUserException(
          "Remote login failed. The remote key is expired.");
  }

  public Strategy importStrategy(String strategyKey) throws WdkModelException,
      WdkUserException {
    Strategy oldStrategy;
    String[] parts = strategyKey.split(":");
    if (parts.length == 1) {
      // new strategy export url
      String strategySignature = parts[0];
      oldStrategy = stepFactory.loadStrategy(strategySignature);
    } else {
      String userSignature = parts[0];
      int displayId = Integer.parseInt(parts[1]);
      User user = userFactory.getUser(userSignature);
      oldStrategy = user.getStrategy(displayId, true);
    }
    return importStrategy(oldStrategy, null);
  }

  public Strategy importStrategy(Strategy oldStrategy,
      Map<Integer, Integer> stepIdsMap) throws WdkModelException,
      WdkUserException {
    Strategy newStrategy = stepFactory.importStrategy(this, oldStrategy,
        stepIdsMap);
    // highlight the imported strategy
    int rootStepId = newStrategy.getLatestStepId();
    String strategyKey = Integer.toString(newStrategy.getStrategyId());
    if (newStrategy.isValid()) setViewResults(strategyKey, rootStepId, 0);
    return newStrategy;
  }

  public Strategy[] getActiveStrategies() throws WdkUserException {
    int[] ids = activeStrategyFactory.getRootStrategies();
    List<Strategy> strategies = new ArrayList<Strategy>();
    for (int id : ids) {
      try {
        Strategy strategy = getStrategy(id);
        strategies.add(strategy);
      } catch (WdkModelException ex) {
        // something wrong with loading a strat, probably the strategy
        // doesn't exist anymore
        logger.warn("something wrong with loading a strat, probably "
            + "the strategy doesn't exist anymore. Please "
            + "investigate:\nUser #" + userId + ", strategy display Id: " + id
            + "\nException: ", ex);
      }
    }
    Strategy[] array = new Strategy[strategies.size()];
    strategies.toArray(array);
    return array;
  }

  public void addActiveStrategy(String strategyKey) throws WdkModelException,
      WdkUserException {
    activeStrategyFactory.openActiveStrategy(strategyKey);
    int pos = strategyKey.indexOf('_');
    if (pos >= 0) strategyKey = strategyKey.substring(0, pos);
    int strategyId = Integer.parseInt(strategyKey);
    stepFactory.updateStrategyViewTime(strategyId);
  }

  public void removeActiveStrategy(String strategyKey) {
    activeStrategyFactory.closeActiveStrategy(strategyKey);
  }

  public void replaceActiveStrategy(int oldStrategyId, int newStrategyId,
      Map<Integer, Integer> stepIdsMap) throws WdkModelException,
      WdkUserException {
    activeStrategyFactory.replaceStrategy(this, oldStrategyId, newStrategyId,
        stepIdsMap);
  }

  public void setViewResults(String strategyKey, int stepId, int pagerOffset) {
    this.activeStrategyFactory.setViewStrategyKey(strategyKey);
    this.activeStrategyFactory.setViewStepId(stepId);
    this.activeStrategyFactory.setViewPagerOffset(pagerOffset);
  }

  public void resetViewResults() {
    this.activeStrategyFactory.setViewStrategyKey(null);
    this.activeStrategyFactory.setViewStepId(null);
    this.activeStrategyFactory.setViewPagerOffset(null);
  }

  public String getViewStrategyKey() {
    return this.activeStrategyFactory.getViewStrategyKey();
  }

  public int getViewStepId() {
    return this.activeStrategyFactory.getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return this.activeStrategyFactory.getViewPagerOffset();
  }

  public boolean[] checkNameExists(Strategy strategy, String name, boolean saved)
      throws WdkModelException {
    return stepFactory.checkNameExists(strategy, name, saved);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof User) {
      User user = (User) obj;
      if (user.userId != userId) return false;
      if (!email.equals(user.email)) return false;
      if (!signature.equals(user.signature)) return false;

      return true;
    } else return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return userId;
  }

  public Step createBooleanStep(Step leftStep, Step rightStep,
      String booleanOperator, boolean useBooleanFilter, String filterName)
      throws WdkModelException {
    BooleanOperator operator = BooleanOperator.parse(booleanOperator);
    Question question = null;
    try {
      question = leftStep.getQuestion();
    } catch (WdkModelException ex) {
      // in case the left step has an invalid question, try the right
      question = rightStep.getQuestion();
    }
    AnswerFilterInstance filter = null;
    RecordClass recordClass = question.getRecordClass();
    if (filterName != null) {
      filter = question.getRecordClass().getFilter(filterName);
    } else filter = recordClass.getDefaultFilter();
    return createBooleanStep(leftStep, rightStep, operator, useBooleanFilter,
        filter);
  }

  public Step createBooleanStep(Step leftStep, Step rightStep,
      BooleanOperator operator, boolean useBooleanFilter,
      AnswerFilterInstance filter) throws WdkModelException {
    // make sure the left & right step belongs to the user
    if (leftStep.getUser().getUserId() != userId)
      throw new WdkModelException("The Left Step [" + leftStep.getStepId()
          + "] doesn't belong to the user #" + userId);
    if (rightStep.getUser().getUserId() != userId)
      throw new WdkModelException("The Right Step [" + rightStep.getStepId()
          + "] doesn't belong to the user #" + userId);

    // verify the record type of the operands
    RecordClass leftRecordClass = leftStep.getQuestion().getRecordClass();
    RecordClass rightRecordClass = rightStep.getQuestion().getRecordClass();
    if (!leftRecordClass.getFullName().equals(rightRecordClass.getFullName()))
      throw new WdkModelException("Boolean operation cannot be applied "
          + "to results of different record types. Left operand is "
          + "of type " + leftRecordClass.getFullName() + ", but the"
          + " right operand is of type " + rightRecordClass.getFullName());

    Question question = wdkModel.getBooleanQuestion(leftRecordClass);
    BooleanQuery booleanQuery = (BooleanQuery) question.getQuery();

    Map<String, String> params = new LinkedHashMap<String, String>();

    String leftName = booleanQuery.getLeftOperandParam().getName();
    String leftKey = Integer.toString(leftStep.getStepId());
    params.put(leftName, leftKey);

    String rightName = booleanQuery.getRightOperandParam().getName();
    String rightKey = Integer.toString(rightStep.getStepId());
    params.put(rightName, rightKey);

    String operatorString = operator.getBaseOperator();
    params.put(booleanQuery.getOperatorParam().getName(), operatorString);
    params.put(booleanQuery.getUseBooleanFilter().getName(),
        Boolean.toString(useBooleanFilter));

    Step booleanStep = createStep(question, params, filter, false, false, 0);
    booleanStep.setPreviousStep(leftStep);
    booleanStep.setChildStep(rightStep);
    return booleanStep;
  }

  public int getStrategyOrder(String strategyKey) {
    int order = activeStrategyFactory.getOrder(strategyKey);
    System.out.println("strat " + strategyKey + " order: " + order);
    return order;
  }

  public int[] getActiveStrategyIds() {
    return activeStrategyFactory.getRootStrategies();
  }

  public Strategy copyStrategy(Strategy strategy) throws WdkModelException,
      WdkUserException {
    Strategy copy = stepFactory.copyStrategy(strategy);
    return copy;
  }

  public Strategy copyStrategy(Strategy strategy, int stepId)
      throws WdkModelException, WdkUserException {
    Strategy copy = stepFactory.copyStrategy(strategy, stepId);
    return copy;
  }

  public void addToFavorite(RecordClass recordClass,
      List<Map<String, Object>> pkValues) throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    favoriteFactory.addToFavorite(this, recordClass, pkValues);
  }

  public void clearFavorite() throws WdkModelException {
    wdkModel.getFavoriteFactory().clearFavorite(this);
  }

  public int getFavoriteCount() throws WdkModelException {
    return wdkModel.getFavoriteFactory().getFavoriteCounts(this);
  }

  public Map<RecordClass, List<Favorite>> getFavorites()
      throws WdkModelException {
    return wdkModel.getFavoriteFactory().getFavorites(this);
  }

  public void removeFromFavorite(RecordClass recordClass,
      List<Map<String, Object>> pkValues) throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    favoriteFactory.removeFromFavorite(this, recordClass, pkValues);
  }

  public boolean isInFavorite(RecordClass recordClass,
      Map<String, Object> pkValue) throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    return favoriteFactory.isInFavorite(this, recordClass, pkValue);
  }

  public void setFavoriteNotes(RecordClass recordClass,
      List<Map<String, Object>> pkValues, String note) throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    favoriteFactory.setNotes(this, recordClass, pkValues, note);
  }

  public void setFavoriteGroups(RecordClass recordClass,
      List<Map<String, Object>> pkValues, String group)
      throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    favoriteFactory.setGroups(this, recordClass, pkValues, group);
  }

  public String[] getFavoriteGroups() throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    return favoriteFactory.getGroups(this);
  }

  public Map<RecordClass, Integer> getBasketCounts() throws WdkModelException {
    BasketFactory basketFactory = wdkModel.getBasketFactory();
    return basketFactory.getBasketCounts(this);
  }

  public int getBasketCounts(List<String[]> records, RecordClass recordClass)
      throws WdkModelException {
    int count = wdkModel.getBasketFactory().getBasketCounts(this, records,
        recordClass);
    if (logger.isDebugEnabled()) {
      logger.debug("How many of " + convert(records) + " in basket? " + count);
    }
    return count;
  }

  private String convert(List<String[]> records) {
    StringBuilder sb = new StringBuilder("List { ");
    for (String[] item : records) {
      sb.append("[ ");
      for (String s : item) {
        sb.append(s).append(", ");
      }
      sb.append(" ],");
    }
    sb.append(" }");
    return sb.toString();
  }

  public int getFavoriteCount(List<Map<String, Object>> records,
      RecordClass recordClass) throws WdkModelException {
    FavoriteFactory favoriteFactory = wdkModel.getFavoriteFactory();
    int count = 0;
    for (Map<String, Object> item : records) {
      boolean inFavs = favoriteFactory.isInFavorite(this, recordClass, item);
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

  public SummaryView getCurrentSummaryView(Question question)
      throws WdkUserException {
    String key = SUMMARY_VIEW_PREFIX + question.getFullName();
    String viewName = projectPreferences.get(key);
    SummaryView view;
    if (viewName == null) { // no summary view set, use the default one
      view = question.getDefaultSummaryView();
    } else {
      view = question.getSummaryView(viewName);
    }
    return view;
  }

  public void setCurrentSummaryView(Question question, SummaryView summaryView)
      throws WdkModelException {
    String key = SUMMARY_VIEW_PREFIX + question.getFullName();
    if (summaryView == null) { // remove the current summary view
      projectPreferences.remove(key);
    } else { // store the current summary view
      String viewName = summaryView.getName();
      projectPreferences.put(key, viewName);
    }
    save();
  }

  public RecordView getCurrentRecordView(RecordClass recordClass)
      throws WdkUserException {
    String key = RECORD_VIEW_PREFIX + recordClass.getFullName();
    String viewName = projectPreferences.get(key);
    RecordView view;
    if (viewName == null) { // no record view set, use the default one
      view = recordClass.getDefaultRecordView();
    } else {
      view = recordClass.getRecordView(viewName);
    }
    return view;
  }

  public void setCurrentRecordView(RecordClass recordClass,
      RecordView recordView) throws WdkModelException {
    String key = RECORD_VIEW_PREFIX + recordClass.getFullName();
    if (recordView == null) { // remove the current record view
      projectPreferences.remove(key);
    } else { // store the current record view
      String viewName = recordView.getName();
      projectPreferences.put(key, viewName);
    }
    save();
  }

  @Override
  public String toString() {
    return "User #" + userId + " - " + email;
  }
}
