/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.Dataset;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;
import org.gusdb.wdk.model.user.User;
import org.json.JSONException;

/**
 * @author: Jerric
 * @created: May 25, 2006
 * @modified by: Jerric
 * @modified at: May 25, 2006
 * 
 */
public class UserBean /* implements Serializable */{

    /**
     * 
     */
    private static final long serialVersionUID = -4296379954371247236L;

    private User user;

    public UserBean() {}

    /**
     * 
     */
    public UserBean(User user) {
        this.user = user;
    }

    User getUser() {
        return user;
    }

    /**
     * @param wdkModel
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.User#setWdkModel(org.gusdb.wdk.model.WdkModel)
     */
    public void setWdkModel(WdkModelBean wdkModel) throws WdkUserException {
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
    public boolean isGuest() throws WdkUserException {
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
    public void save() throws WdkUserException {
        user.save();
    }

    // =========================================================================
    // Methods for dataset operations
    // =========================================================================

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#createDataset(java.lang.String,
     * java.lang.String, java.lang.String[])
     */
    public DatasetBean createDataset(String uploadFile, String[] values)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException {
        Dataset dataset = user.createDataset(uploadFile, values);
        DatasetBean bean = new DatasetBean(dataset);
        return bean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getDataset(java.lang.String)
     */
    public DatasetBean getDataset(String datasetChecksum)
            throws WdkUserException, SQLException, WdkModelException {
        return new DatasetBean(user.getDataset(datasetChecksum));
    }

    // =========================================================================
    // Methods for Persistent history operations
    // =========================================================================

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#clearHistories()
     */
    public void deleteSteps() throws WdkUserException, SQLException {
        user.deleteSteps();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.Answer)
     */
    public StepBean createStep(QuestionBean question,
            Map<String, String> params, String filterName)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Step step = user.createStep(question.question, params, filterName);
        return new StepBean(step);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#deleteHistory(int)
     */
    public void deleteStep(int displayId) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        user.deleteStep(displayId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories()
     */
    public StepBean[] getSteps() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Step[] steps = user.getSteps();
        StepBean[] beans = new StepBean[steps.length];
        for (int i = 0; i < steps.length; i++) {
            beans[i] = new StepBean(steps[i]);
        }
        return beans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories()
     */
    public StepBean[] getInvalidSteps() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        Step[] steps = user.getInvalidSteps();
        StepBean[] beans = new StepBean[steps.length];
        for (int i = 0; i < steps.length; i++) {
            beans[i] = new StepBean(steps[i]);
        }
        return beans;
    }

    public void deleteInvalidSteps() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        user.deleteInvalidSteps();
    }

    public void deleteInvalidStrategies() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        user.deleteInvalidStrategies();
    }

    public Map<String, List<StepBean>> getHistoriesByCategory()
            throws WdkUserException, WdkModelException, SQLException,
            JSONException, NoSuchAlgorithmException {
        Map<String, List<Step>> steps = user.getStepsByCategory();
        Map<String, List<StepBean>> category = new LinkedHashMap<String, List<StepBean>>();
        for (String type : steps.keySet()) {
            List<Step> list = steps.get(type);
            List<StepBean> beans = new ArrayList<StepBean>();
            for (Step step : list) {
                beans.add(new StepBean(step));
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
    public StepBean[] getSteps(String recordClassName) throws WdkUserException,
            WdkModelException, SQLException, JSONException,
            NoSuchAlgorithmException {
        Step[] steps = user.getSteps(recordClassName);
        StepBean[] beans = new StepBean[steps.length];
        for (int i = 0; i < steps.length; i++) {
            beans[i] = new StepBean(steps[i]);
        }
        return beans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistory(int)
     */
    public StepBean getStep(int displayId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        return new StepBean(user.getStep(displayId));
    }

    public StrategyBean getStrategy(int displayId) throws WdkUserException,
            WdkModelException, JSONException, SQLException {
        return new StrategyBean(user.getStrategy(displayId));
    }

    public Map<String, List<StrategyBean>> getStrategiesByCategory()
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Map<String, List<Strategy>> strategies = user.getStrategiesByCategory();
        Map<String, List<StrategyBean>> category = new LinkedHashMap<String, List<StrategyBean>>();
        for (String type : strategies.keySet()) {
            List<Strategy> list = strategies.get(type);
            List<StrategyBean> beans = new ArrayList<StrategyBean>();
            for (Strategy strategy : list) {
                beans.add(new StrategyBean(strategy));
            }
            category.put(type, beans);
        }
        return category;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getStrategyCount()
     */
    public int getStrategyCount() throws WdkUserException, SQLException {
        return user.getStrategyCount();
    }

    public void validateExpression(String expression) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        user.validateExpression(expression);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#combineHistory(java.lang.String)
     */
    public StepBean combineStep(String expression, boolean useBooleanFilter)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        return new StepBean(user.combineStep(expression, useBooleanFilter,
                false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistoryCount()
     */
    public int getHistoryCount() throws WdkUserException {
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
    public void setItemsPerPage(int itemsPerPage) throws WdkUserException {
        user.setItemsPerPage(itemsPerPage);
    }

    /**
     * @param questionFullName
     * @param attrName
     * @param ascending
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @see org.gusdb.wdk.model.user.User#addSortingAttribute(java.lang.String,
     *      java.lang.String, boolean)
     */
    public String addSortingAttribute(String questionFullName, String attrName,
            boolean ascending) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException {
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
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#getSortingAttributes(java.lang.String)
     */
    public Map<String, Boolean> getSortingAttributes(String questionFullName)
            throws WdkUserException, WdkModelException {
        return user.getSortingAttributes(questionFullName);
    }

    /**
     * @param sortingChecksum
     * @return
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.User#getSortingAttributesByChecksum(java.lang.String)
     */
    public Map<String, Boolean> getSortingAttributesByChecksum(
            String sortingChecksum) throws WdkUserException {
        return user.getSortingAttributesByChecksum(sortingChecksum);
    }

    /**
     * @param questionFullName
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#getSummaryAttributes(java.lang.String)
     */
    public String[] getSummaryAttributes(String questionFullName)
            throws WdkUserException, WdkModelException {
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
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.User#createRemoteKey()
     */
    public String createRemoteKey() throws WdkUserException {
        return user.createRemoteKey();
    }

    /**
     * @param remoteKey
     * @throws WdkUserException
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
    public String toString() {
        return this.user.toString();
    }

    public ArrayList<Integer> getActiveStrategies() {
        return user.getActiveStrategies();
    }

    public void setActiveStrategies(ArrayList<Integer> activeStrategies) {
        user.setActiveStrategies(activeStrategies);
    }

    /**
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.User#deleteStrategies()
     */
    public void deleteStrategies() throws SQLException {
        user.deleteStrategies();
    }

    /**
     * @param strategyId
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     * @see org.gusdb.wdk.model.user.User#deleteStrategy(int)
     */
    public void deleteStrategy(int strategyId) throws WdkUserException,
            WdkModelException, SQLException {
        user.deleteStrategy(strategyId);
    }

    /**
     * @param rootAnswerChecksum
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws NoSuchAlgorithmException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.user.User#importStrategyByAnswer(java.lang.String)
     */
    public StrategyBean importStrategy(String strategyKey)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        Strategy strategy = user.importStrategy(strategyKey);
        return new StrategyBean(strategy);
    }

    /**
     * @param answer
     * @param saved
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws SQLException
     * @throws JSONException
     * @see org.gusdb.wdk.model.user.User#createStrategy(org.gusdb.wdk.model.user.Step,
     *      boolean)
     */
    public StrategyBean createStrategy(StepBean step, boolean saved)
            throws WdkUserException, WdkModelException, SQLException,
            JSONException {
        return new StrategyBean(user.createStrategy(step.step, saved));
    }

    /**
     * @param questionFullName
     * @param summaryChecksum
     * @throws WdkModelException
     * @throws WdkUserException
     * @throws NoSuchAlgorithmException
     * @see org.gusdb.wdk.model.user.User#applySummaryChecksum(java.lang.String,
     *      java.lang.String)
     */
    public void applySummaryChecksum(String questionFullName,
            String summaryChecksum) throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException {
        user.applySummaryChecksum(questionFullName, summaryChecksum);
    }

    /**
     * @param questionFullName
     * @param summaryNames
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws NoSuchAlgorithmException
     * @see org.gusdb.wdk.model.user.User#setSummaryAttribute(java.lang.String,
     *      java.lang.String[])
     */
    public String setSummaryAttributes(String questionFullName,
            String[] summaryNames) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException {
        return user.setSummaryAttributes(questionFullName, summaryNames);
    }

    public boolean checkNameExists(StrategyBean strategy, String name)
            throws SQLException {
        return user.checkNameExists(strategy.strategy, name);
    }

}
