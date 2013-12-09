/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.user.BasketFactory;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.DatasetFactory;
import org.gusdb.wdk.model.user.Favorite;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;

/**
 * @author: Jerric
 * @created: May 25, 2006
 * @modified by: Jerric
 * @modified at: May 25, 2006
 * 
 */
public class UserBean /* implements Serializable */{

  private static Logger logger = Logger.getLogger(UserBean.class);

  private User user;

  private int stepId;

  public UserBean() {}

  /**
     * 
     */
  public UserBean(User user) {
    this.user = user;
  }

  public User getUser() {
    return user;
  }

  /**
   * @param wdkModel
   * @see org.gusdb.wdk.model.user.User#setWdkModel(org.gusdb.wdk.model.WdkModel)
   */
  public void setWdkModel(WdkModelBean wdkModel) {
    user.setWdkModel(wdkModel.getModel());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getUserId()
   */
  public int getUserId() {
    return user.getUserId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#addUserRole(java.lang.String)
   */
  public void addUserRole(String userRole) {
    user.addUserRole(userRole);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getAddress()
   */
  public String getAddress() {
    return user.getAddress();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getCity()
   */
  public String getCity() {
    return user.getCity();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getCountry()
   */
  public String getCountry() {
    return user.getCountry();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getOpenId()
   */
  public String getOpenId() {
    return user.getOpenId();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getDepartment()
   */
  public String getDepartment() {
    return user.getDepartment();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getEmail()
   */
  public String getEmail() {
    return user.getEmail();
  }

  public void setEmail(String email) {
    user.setEmail(email);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getFirstName()
   */
  public String getFirstName() {
    return user.getFirstName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getLastName()
   */
  public String getLastName() {
    return user.getLastName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getMiddleName()
   */
  public String getMiddleName() {
    return user.getMiddleName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getOrganization()
   */
  public String getOrganization() {
    return user.getOrganization();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getPhoneNumber()
   */
  public String getPhoneNumber() {
    return user.getPhoneNumber();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getState()
   */
  public String getState() {
    return user.getState();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getTitle()
   */
  public String getTitle() {
    return user.getTitle();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getUserRoles()
   */
  public String[] getUserRoles() {
    return user.getUserRoles();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getZipCode()
   */
  public String getZipCode() {
    return user.getZipCode();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#isGuest()
   */
  public boolean isGuest() {
    return user.isGuest();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#removeUserRole(java.lang.String)
   */
  public void removeUserRole(String userRole) {
    user.removeUserRole(userRole);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setAddress(java.lang.String)
   */
  public void setAddress(String address) {
    user.setAddress(address);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setCity(java.lang.String)
   */
  public void setCity(String city) {
    user.setCity(city);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setCountry(java.lang.String)
   */
  public void setCountry(String country) {
    user.setCountry(country);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setOpenId(java.lang.String)
   */
  public void setOpenId(String openId) {
    user.setOpenId(openId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setDepartment(java.lang.String)
   */
  public void setDepartment(String department) {
    user.setDepartment(department);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setFirstName(java.lang.String)
   */
  public void setFirstName(String firstName) {
    user.setFirstName(firstName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setLastName(java.lang.String)
   */
  public void setLastName(String lastName) {
    user.setLastName(lastName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setMiddleName(java.lang.String)
   */
  public void setMiddleName(String middleName) {
    user.setMiddleName(middleName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setOrganization(java.lang.String)
   */
  public void setOrganization(String organization) {
    user.setOrganization(organization);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setPhoneNumber(java.lang.String)
   */
  public void setPhoneNumber(String phoneNumber) {
    user.setPhoneNumber(phoneNumber);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setState(java.lang.String)
   */
  public void setState(String state) {
    user.setState(state);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setTitle(java.lang.String)
   */
  public void setTitle(String title) {
    user.setTitle(title);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setZipCode(java.lang.String)
   */
  public void setZipCode(String zipCode) {
    user.setZipCode(zipCode);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getSignature()
   */
  public String getSignature() {
    return user.getSignature();
  }

  public String getFrontAction() {
    return user.getFrontAction();
  }

  public Integer getFrontStrategy() {
    return user.getFrontStrategy();
  }

  public Integer getFrontStep() {
    return user.getFrontStep();
  }

  public void setFrontAction(String frontAction) {
    user.setFrontAction(frontAction);
  }

  public void setFrontStrategy(int frontStrategy) {
    user.setFrontStrategy(frontStrategy);
  }

  public void setFrontStep(int frontStep) {
    user.setFrontStep(frontStep);
  }

  public void resetFrontAction() {
    user.resetFrontAction();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#changePassword(java.lang.String,
   * java.lang.String, java.lang.String)
   */
  public void changePassword(String oldPassword, String newPassword,
      String confirmPassword) throws WdkUserException {
    user.changePassword(oldPassword, newPassword, confirmPassword);
  }

  //
  // //
  // *************************************************************************
  // // Copied from the original code - to be updated soon
  // //
  // *************************************************************************
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#addAnswer(org.gusdb.wdk.model.Answer)
  // */
  // public void addAnswer(AnswerBean answer) throws WdkUserException,
  // WdkModelException {
  // user.addAnswer(answer.answer);
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see
  // org.gusdb.wdk.model.User#addAnswerFuzzy(org.gusdb.wdk.model.Answer)
  // */
  // public void addAnswerFuzzy(AnswerBean answer) throws WdkUserException,
  // WdkModelException {
  // user.addAnswerFuzzy(answer.answer);
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#combineAnswers(int, int,
  // java.lang.String)
  // */
  // public UserAnswerBean combineUserAnswers(int firstAnswerID,
  // int secondAnswerID, String operation, int start, int end,
  // Map<String, String> operatorMap) throws WdkUserException,
  // WdkModelException {
  // return new UserAnswerBean(this.user.combineUserAnswers(firstAnswerID,
  // secondAnswerID, operation, start, end, operatorMap));
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#combineAnswers(java.lang.String)
  // */
  // public UserAnswerBean combineAnswers(String expression, int start, int
  // end,
  // Map<String, String> operatorMap) throws WdkUserException,
  // WdkModelException {
  // return new UserAnswerBean(this.user.combineUserAnswers(expression,
  // start, end, operatorMap));
  // }
  //
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#deleteAnswer(int)
  // */
  // public void deleteUserAnswer(int answerId) throws WdkUserException {
  // this.user.deleteUserAnswer(answerId);
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#clearAnswers()
  // */
  // public void clearUserAnswers() throws WdkUserException {
  // this.user.clearUserAnswers();
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#getAnswerByID(int)
  // */
  // public UserAnswerBean getUserAnswerByID(int answerID)
  // throws WdkUserException {
  // return new UserAnswerBean(this.user.getUserAnswerByID(answerID));
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#getAnswerByName(java.lang.String)
  // */
  // public UserAnswerBean getUserAnswerByName(String name)
  // throws WdkUserException {
  // return new UserAnswerBean(this.user.getUserAnswerByName(name));
  // }
  //
  // public int getUserAnswerIdByAnswer(AnswerBean answer)
  // throws WdkUserException {
  // return getUserAnswerByAnswerFuzzy(answer).getAnswerID();
  // }
  //
  // public UserAnswerBean getUserAnswerByAnswer(AnswerBean answer)
  // throws WdkUserException {
  // return new UserAnswerBean(user.getUserAnswerByAnswer(answer.answer));
  // }
  //
  // public UserAnswerBean getUserAnswerByAnswerFuzzy(AnswerBean answer)
  // throws WdkUserException {
  // return new UserAnswerBean(
  // user.getUserAnswerByAnswerFuzzy(answer.answer));
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#getAnswers()
  // */
  // public UserAnswerBean[] getUserAnswers() {
  // UserAnswer[] answers = user.getUserAnswers();
  // UserAnswerBean[] answerBeans = new UserAnswerBean[answers.length];
  // for (int i = 0; i < answers.length; i++) {
  // answerBeans[i] = new UserAnswerBean(answers[i]);
  // }
  // return answerBeans;
  // }
  //
  // public int getAnswerCount() {
  // return user.getUserAnswers().length;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#getRecordAnswerMap()
  // */
  // public Map<String, UserAnswerBean[]> getRecordAnswerMap() {
  // Map recUsrAnsMap = user.getRecordAnswerMap();
  // Map<String, UserAnswerBean[]> recUsrAnsBeanMap = new
  // LinkedHashMap<String, UserAnswerBean[]>();
  // for (Object r : recUsrAnsMap.keySet()) {
  // String rec = (String) r;
  // UserAnswer[] usrAns = (UserAnswer[]) recUsrAnsMap.get(rec);
  // UserAnswerBean[] answerBeans = new UserAnswerBean[usrAns.length];
  // for (int i = 0; i < usrAns.length; i++) {
  // answerBeans[i] = new UserAnswerBean(usrAns[i]);
  // }
  // recUsrAnsBeanMap.put(rec, answerBeans);
  // }
  // return recUsrAnsBeanMap;
  // }
  //
  // /*
  // * (non-Javadoc)
  // *
  // * @see org.gusdb.wdk.model.User#renameAnswer(int, java.lang.String)
  // */
  // public void renameUserAnswer(int answerID, String name)
  // throws WdkUserException {
  // this.user.renameUserAnswer(answerID, name);
  // }

  public Map<String, String> getGlobalPreferences() {
    return user.getGlobalPreferences();
  }

  public Map<String, String> getProjectPreferences() {
    return user.getProjectPreferences();
  }

  public void setGlobalPreference(String prefName, String prefValue) {
    user.setGlobalPreference(prefName, prefValue);
  }

  public void setProjectPreference(String prefName, String prefValue) {
    user.setProjectPreference(prefName, prefValue);
  }

  public void unsetGlobalPreference(String prefName) {
    user.unsetGlobalPreference(prefName);
  }

  public void unsetProjectPreference(String prefName) {
    user.unsetProjectPreference(prefName);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#clearPreferences()
   */
  public void clearPreferences() {
    user.clearPreferences();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#save()
   */
  public void save() throws WdkModelException {
    user.save();
  }

  // =========================================================================
  // Methods for dataset operations
  // =========================================================================

  public DatasetBean createDataset(List<String[]> data, String contentType,
      String originalContent, String uploadFile) throws WdkModelException,
      WdkUserException {
    DatasetFactory factory = user.getWdkModel().getDatasetFactory();
    Dataset dataset = factory.createOrGetDataset(user, data, originalContent,
        contentType, uploadFile);
    DatasetBean bean = new DatasetBean(dataset);
    return bean;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getDataset(java.lang.String)
   */
  public DatasetBean getDataset(int userDatasetId) throws WdkModelException {
    return new DatasetBean(user.getDataset(userDatasetId));
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
    user.deleteSteps();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.Answer)
   */
  public StepBean createStep(QuestionBean question, Map<String, String> params,
      String filterName, boolean deleted, boolean validate, int assignedWeight)
      throws WdkModelException {
    Step step = user.createStep(question.question, params, filterName, deleted,
        validate, assignedWeight);
    return new StepBean(this, step);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#deleteHistory(int)
   */
  public void deleteStep(int displayId) throws WdkModelException {
    user.deleteStep(displayId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistories()
   */
  public StepBean[] getSteps() throws WdkModelException {
    Step[] steps = user.getSteps();
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
    Step[] steps = user.getInvalidSteps();
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public void deleteInvalidSteps() throws WdkModelException {
    user.deleteInvalidSteps();
  }

  public void deleteInvalidStrategies() throws WdkModelException {
    user.deleteInvalidStrategies();
  }

  public Map<String, List<StepBean>> getStepsByCategory()
      throws WdkModelException {
    Map<String, List<Step>> steps = user.getStepsByCategory();
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
    Step[] steps = user.getSteps(recordClassName);
    StepBean[] beans = new StepBean[steps.length];
    for (int i = 0; i < steps.length; i++) {
      beans[i] = new StepBean(this, steps[i]);
    }
    return beans;
  }

  public StrategyBean getStrategy(int displayId) throws WdkUserException,
      WdkModelException {
    return new StrategyBean(this, user.getStrategy(displayId));
  }

  public Map<String, List<StrategyBean>> getStrategiesByCategory()
      throws Exception {
    try {
      Map<String, List<Strategy>> strategies = user.getStrategiesByCategory();
      return convertMap(strategies);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  private Map<String, List<StrategyBean>> convertMap(
      Map<String, List<Strategy>> strategies) {
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
    return user.getStrategyCount();
  }

  public void validateExpression(String expression) throws WdkModelException {
    user.validateExpression(expression);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#combineHistory(java.lang.String)
   */
  public StepBean combineStep(String expression, boolean useBooleanFilter)
      throws WdkModelException {
    return new StepBean(this, user.combineStep(expression, useBooleanFilter,
        false));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getHistoryCount()
   */
  public int getStepCount() throws WdkModelException {
    return user.getStepCount();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#getItemsPerPage()
   */
  public int getItemsPerPage() {
    return user.getItemsPerPage();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.user.User#setItemsPerPage(int)
   */
  public void setItemsPerPage(int itemsPerPage) throws WdkModelException {
    user.setItemsPerPage(itemsPerPage);
  }

  /**
   * @param questionFullName
   * @param attrName
   * @param ascending
   * @see org.gusdb.wdk.model.user.User#addSortingAttribute(java.lang.String,
   *      java.lang.String, boolean)
   */
  public String addSortingAttribute(String questionFullName, String attrName,
      boolean ascending) throws WdkModelException {
    return user.addSortingAttribute(questionFullName, attrName, ascending);
  }

  /**
   * @param questionFullName
   * @param sortingChecksum
   */
  public void applySortingChecksum(String questionFullName,
      String sortingChecksum) {
    user.applySortingChecksum(questionFullName, sortingChecksum);
  }

  /**
   * @param questionFullName
   * @return
   * @see org.gusdb.wdk.model.user.User#getSortingAttributes(java.lang.String)
   */
  public Map<String, Boolean> getSortingAttributes(String questionFullName)
      throws WdkModelException {
    return user.getSortingAttributes(questionFullName);
  }

  /**
   * @param sortingChecksum
   * @return
   * @see org.gusdb.wdk.model.user.User#getSortingAttributesByChecksum(java.lang.String)
   */
  public Map<String, Boolean> getSortingAttributesByChecksum(
      String sortingChecksum) throws WdkModelException {
    return user.getSortingAttributesByChecksum(sortingChecksum);
  }

  /**
   * @param questionFullName
   * @return
   * @see org.gusdb.wdk.model.user.User#getSummaryAttributes(java.lang.String)
   */
  public String[] getSummaryAttributes(String questionFullName)
      throws WdkModelException {
    return user.getSummaryAttributes(questionFullName);
  }

  /**
   * @param questionFullName
   * @see org.gusdb.wdk.model.user.User#resetSummaryAttribute(java.lang.String)
   */
  public void resetSummaryAttribute(String questionFullName) {
    user.resetSummaryAttributes(questionFullName);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#createRemoteKey()
   */
  public String createRemoteKey() throws WdkUserException, WdkModelException {
    return user.createRemoteKey();
  }

  /**
   * @param remoteKey
   * @see org.gusdb.wdk.model.user.User#verifyRemoteKey(java.lang.String)
   */
  public void verifyRemoteKey(String remoteKey) throws WdkUserException {
    user.verifyRemoteKey(remoteKey);
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

  public void addActiveStrategy(String strategyKey)
      throws NumberFormatException, WdkUserException, WdkModelException {
    user.addActiveStrategy(strategyKey);
  }

  public void removeActiveStrategy(String strategyId) {
    user.removeActiveStrategy(strategyId);
  }

  public void replaceActiveStrategy(int oldStrategyId, int newStrategyId,
      Map<Integer, Integer> stepIdsMap) throws WdkUserException,
      WdkModelException {
    user.replaceActiveStrategy(oldStrategyId, newStrategyId, stepIdsMap);
  }

  /**
   * @see org.gusdb.wdk.model.user.User#deleteStrategies()
   */
  public void deleteStrategies() throws WdkModelException {
    user.deleteStrategies();
  }

  /**
   * @param strategyId
   * @see org.gusdb.wdk.model.user.User#deleteStrategy(int)
   */
  public void deleteStrategy(int strategyId) throws WdkModelException {
    user.deleteStrategy(strategyId);
  }

  /**
   * @param rootAnswerChecksum
   * @return
   * @see org.gusdb.wdk.model.user.User#importStrategyByAnswer(java.lang.String)
   */
  public StrategyBean importStrategy(String strategyKey)
      throws WdkModelException, WdkUserException {
    Strategy strategy = user.importStrategy(strategyKey);
    return new StrategyBean(this, strategy);
  }

  /**
   * @param answer
   * @param saved
   * @return
   * @see org.gusdb.wdk.model.user.User#createStrategy(org.gusdb.wdk.model.user.Step,
   *      boolean)
   */
  public StrategyBean createStrategy(StepBean step, boolean saved)
      throws WdkUserException, WdkModelException {
    return new StrategyBean(this, user.createStrategy(step.step, saved));
  }

  /**
   * @param answer
   * @param saved
   * @return
   * @see org.gusdb.wdk.model.user.User#createStrategy(org.gusdb.wdk.model.user.Step,
   *      boolean)
   */
  public StrategyBean createStrategy(StepBean step, boolean saved,
      boolean hidden) throws WdkUserException, WdkModelException {
    return new StrategyBean(this, user.createStrategy(step.step, saved, hidden));
  }

  /**
   * @param questionFullName
   * @param summaryChecksum
   * @see org.gusdb.wdk.model.user.User#applySummaryChecksum(java.lang.String,
   *      java.lang.String)
   */
  public void applySummaryChecksum(String questionFullName,
      String summaryChecksum) {
    user.applySummaryChecksum(questionFullName, summaryChecksum);
  }

  /**
   * @param questionFullName
   * @param summaryNames
   * @return
   * @see org.gusdb.wdk.model.user.User#setSummaryAttribute(java.lang.String,
   *      java.lang.String[])
   */
  public String setSummaryAttributes(String questionFullName,
      String[] summaryNames) throws WdkModelException {
    return user.setSummaryAttributes(questionFullName, summaryNames);
  }

  public boolean checkNameExists(StrategyBean strategy, String name,
      boolean saved) throws WdkModelException {
    return user.checkNameExists(strategy.strategy, name, saved);
  }

  public Map<String, List<StrategyBean>> getSavedStrategiesByCategory()
      throws WdkModelException {
    Map<String, List<Strategy>> strategies = user.getSavedStrategiesByCategory();
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getUnsavedStrategiesByCategory()
      throws WdkModelException {
    Map<String, List<Strategy>> strategies = user.getUnsavedStrategiesByCategory();
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getRecentStrategiesByCategory()
      throws WdkModelException {
    Map<String, List<Strategy>> strategies = user.getRecentStrategiesByCategory();
    return convertMap(strategies);
  }

  public Map<String, List<StrategyBean>> getActiveStrategiesByCategory()
      throws WdkModelException, WdkUserException {
    Map<String, List<Strategy>> strategies = user.getActiveStrategiesByCategory();
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
      if (strategies.size() == 0) continue;

      Map<String, List<StrategyBean>> activities = new LinkedHashMap<String, List<StrategyBean>>();
      activities.put("Opened", strategies);
      categories.put(category, activities);
    }

    for (String rcName : savedStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClass(rcName);
      String category = recordClass.getDisplayName();
      List<StrategyBean> strategies = savedStrats.get(rcName);
      if (strategies.size() == 0) continue;

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
      if (strategies.size() == 0) continue;

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
    return new StepBean(this, user.getStep(displayId));
  }

  /**
   * @param previousStep
   * @param childStep
   * @param operator
   * @param useBooleanFilter
   * @param filter
   * @return
   * @see org.gusdb.wdk.model.user.User#createBooleanStep(org.gusdb.wdk.model.user.Step,
   *      org.gusdb.wdk.model.user.Step, org.gusdb.wdk.model.BooleanOperator,
   *      boolean, org.gusdb.wdk.model.AnswerFilterInstance)
   */
  public StepBean createBooleanStep(StepBean previousStep, StepBean childStep,
      String operator, boolean useBooleanFilter, String filterName)
      throws WdkModelException {
    Step step = user.createBooleanStep(previousStep.step, childStep.step,
        operator, useBooleanFilter, filterName);
    return new StepBean(this, step);
  }

  public void setViewResults(String strategyKey, int stepId, int viewPagerOffset) {
    logger.debug("setting view steps: " + strategyKey + ", " + stepId + ", "
        + viewPagerOffset);
    user.setViewResults(strategyKey, stepId, viewPagerOffset);
  }

  public void resetViewResults() {
    user.resetViewResults();
  }

  public String getViewStrategyId() {
    return user.getViewStrategyKey();
  }

  public int getViewStepId() {
    return user.getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return user.getViewPagerOffset();
  }

  public StrategyBean[] getActiveStrategies() throws WdkUserException {
    List<StrategyBean> strategies = new ArrayList<StrategyBean>();
    for (Strategy strategy : user.getActiveStrategies()) {
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
    return user.getStrategyOrder(strategyKey);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getActiveStrategyIds()
   */
  public int[] getActiveStrategyIds() {
    return user.getActiveStrategyIds();
  }

  public void setStepId(String stepId) {
    this.stepId = Integer.parseInt(stepId);
  }

  public StepBean getStepByCachedId() throws WdkModelException {
    return new StepBean(this, user.getStep(stepId));
  }

  public StrategyBean copyStrategy(StrategyBean strategy)
      throws WdkUserException, WdkModelException {
    return new StrategyBean(this, user.copyStrategy(strategy.strategy));
  }

  public StrategyBean copyStrategy(StrategyBean strategy, int stepId)
      throws WdkModelException, WdkUserException {
    return new StrategyBean(this, user.copyStrategy(strategy.strategy, stepId));
  }

  public void addToBasket(RecordClassBean recordClass, List<String[]> ids)
      throws SQLException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.addToBasket(user, recordClass.recordClass, ids);
  }

  public void addToBasket(StepBean step) throws SQLException, WdkModelException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.addToBasket(user, step.step);
  }

  public void removeFromBasket(RecordClassBean recordClass, List<String[]> ids)
      throws SQLException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(user, recordClass.recordClass, ids);
  }

  public void removeFromBasket(StepBean step) throws WdkModelException,
      SQLException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.removeFromBasket(user, step.step);
  }

  public void clearBasket(RecordClassBean recordClass) throws SQLException {
    BasketFactory factory = user.getWdkModel().getBasketFactory();
    factory.clearBasket(user, recordClass.recordClass);
  }

  public String getBasket(RecordClassBean recordClass) throws SQLException {
    BasketFactory basketFactory = user.getWdkModel().getBasketFactory();
    return basketFactory.getBasketContent(user, recordClass.recordClass);
  }

  public Map<RecordClassBean, Integer> getBasketCounts()
      throws WdkModelException {
    Map<RecordClass, Integer> counts = user.getBasketCounts();
    Map<RecordClassBean, Integer> beans = new LinkedHashMap<RecordClassBean, Integer>();
    for (RecordClass recordClass : counts.keySet()) {
      RecordClassBean bean = new RecordClassBean(recordClass);
      int count = counts.get(recordClass);
      beans.put(bean, count);
    }
    return beans;
  }

  public int getBasketCount() throws WdkModelException {
    Map<RecordClass, Integer> baskets = user.getBasketCounts();
    int total = 0;
    for (int count : baskets.values()) {
      total += count;
    }
    return total;
  }

  /**
   * @param recordClass
   * @param pkValues
   * @see org.gusdb.wdk.model.user.User#addToFavorite(org.gusdb.wdk.model.RecordClass,
   *      java.util.List)
   */
  public void addToFavorite(RecordClassBean recordClass,
      List<Map<String, Object>> pkValues) throws WdkModelException {
    user.addToFavorite(recordClass.recordClass, pkValues);
  }

  /**
   * @see org.gusdb.wdk.model.user.User#clearFavorite()
   */
  public void clearFavorite() throws WdkModelException {
    user.clearFavorite();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteCount()
   */
  public int getFavoriteCount() throws WdkModelException {
    return user.getFavoriteCount();
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavorites()
   */
  public Map<RecordClassBean, List<FavoriteBean>> getFavorites()
      throws WdkModelException {
    Map<RecordClass, List<Favorite>> favorites = user.getFavorites();
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
   * @see org.gusdb.wdk.model.user.User#removeFromFavorite(org.gusdb.wdk.model.RecordClass,
   *      java.util.List)
   */
  public void removeFromFavorite(RecordClassBean recordClass,
      List<Map<String, Object>> pkValues) throws WdkModelException {
    user.removeFromFavorite(recordClass.recordClass, pkValues);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param group
   * @see org.gusdb.wdk.model.user.User#setFavoriteGroups(org.gusdb.wdk.model.RecordClass,
   *      java.util.List, java.lang.String)
   */
  public void setFavoriteGroups(RecordClassBean recordClass,
      List<Map<String, Object>> pkValues, String group)
      throws WdkModelException {
    user.setFavoriteGroups(recordClass.recordClass, pkValues, group);
  }

  /**
   * @param recordClass
   * @param pkValues
   * @param note
   * @see org.gusdb.wdk.model.user.User#setFavoriteNotes(org.gusdb.wdk.model.RecordClass,
   *      java.util.List, java.lang.String)
   */
  public void setFavoriteNotes(RecordClassBean recordClass,
      List<Map<String, Object>> pkValues, String note) throws WdkModelException {
    user.setFavoriteNotes(recordClass.recordClass, pkValues, note);
  }

  /**
   * @return
   * @see org.gusdb.wdk.model.user.User#getFavoriteGroups()
   */
  public String[] getFavoriteGroups() throws WdkModelException {
    return user.getFavoriteGroups();
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  public int getBasketCount(List<String[]> records, RecordClassBean recordClass)
      throws WdkModelException {
    return user.getBasketCounts(records, recordClass.recordClass);
  }

  /**
   * @param records
   * @param recordClass
   * @return
   */
  public int getFavoriteCount(List<Map<String, Object>> records,
      RecordClassBean recordClass) throws WdkModelException {
    return user.getFavoriteCount(records, recordClass.recordClass);
  }

  private Question currentQuestion;

  public void setCurrentQuestion(QuestionBean question) {
    this.currentQuestion = question.question;
  }

  public SummaryView getCurrentSummaryView() throws Exception {
    try {
      return user.getCurrentSummaryView(currentQuestion);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  public void setCurrentSummaryView(QuestionBean question,
      SummaryView summaryView) throws WdkModelException {
    user.setCurrentSummaryView(question.question, summaryView);
  }

  private RecordClass currentRecordClass;

  public void setCurrentRecordClass(RecordClassBean recordClass) {
    this.currentRecordClass = recordClass.recordClass;
  }

  public RecordView getCurrentRecordView() throws Exception {
    try {
      return user.getCurrentRecordView(currentRecordClass);
    } catch (Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  public void setCurrentRecordView(RecordClassBean recordClass,
      RecordView recordView) throws WdkModelException {
    user.setCurrentRecordView(recordClass.recordClass, recordView);
  }

}
