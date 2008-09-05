/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.Strategy;

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
    public boolean getGuest() throws WdkUserException {
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
     *      java.lang.String, java.lang.String)
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
    // * @see org.gusdb.wdk.model.User#addAnswerValue(org.gusdb.wdk.model.AnswerValue)
    // */
    // public void addAnswerValue(AnswerValueBean answer) throws WdkUserException,
    // WdkModelException {
    // user.addAnswerValue(answer.answer);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see
    // org.gusdb.wdk.model.User#addAnswerValueFuzzy(org.gusdb.wdk.model.AnswerValue)
    // */
    // public void addAnswerValueFuzzy(AnswerValueBean answer) throws WdkUserException,
    // WdkModelException {
    // user.addAnswerValueFuzzy(answer.answer);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#combineAnswerValues(int, int,
    // java.lang.String)
    // */
    // public UserAnswerValueBean combineUserAnswerValues(int firstAnswerValueID,
    // int secondAnswerValueID, String operation, int start, int end,
    // Map<String, String> operatorMap) throws WdkUserException,
    // WdkModelException {
    // return new UserAnswerValueBean(this.user.combineUserAnswerValues(firstAnswerValueID,
    // secondAnswerValueID, operation, start, end, operatorMap));
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#combineAnswerValues(java.lang.String)
    // */
    // public UserAnswerValueBean combineAnswerValues(String expression, int start, int
    // end,
    // Map<String, String> operatorMap) throws WdkUserException,
    // WdkModelException {
    // return new UserAnswerValueBean(this.user.combineUserAnswerValues(expression,
    // start, end, operatorMap));
    // }
    //
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#deleteAnswerValue(int)
    // */
    // public void deleteUserAnswerValue(int answerId) throws WdkUserException {
    // this.user.deleteUserAnswerValue(answerId);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#clearAnswerValues()
    // */
    // public void clearUserAnswerValues() throws WdkUserException {
    // this.user.clearUserAnswerValues();
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#getAnswerValueByID(int)
    // */
    // public UserAnswerValueBean getUserAnswerValueByID(int answerID)
    // throws WdkUserException {
    // return new UserAnswerValueBean(this.user.getUserAnswerValueByID(answerID));
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#getAnswerValueByName(java.lang.String)
    // */
    // public UserAnswerValueBean getUserAnswerValueByName(String name)
    // throws WdkUserException {
    // return new UserAnswerValueBean(this.user.getUserAnswerValueByName(name));
    // }
    //
    // public int getUserAnswerValueIdByAnswerValue(AnswerValueBean answer)
    // throws WdkUserException {
    // return getUserAnswerValueByAnswerValueFuzzy(answer).getAnswerValueID();
    // }
    //
    // public UserAnswerValueBean getUserAnswerValueByAnswerValue(AnswerValueBean answer)
    // throws WdkUserException {
    // return new UserAnswerValueBean(user.getUserAnswerValueByAnswerValue(answer.answer));
    // }
    //
    // public UserAnswerValueBean getUserAnswerValueByAnswerValueFuzzy(AnswerValueBean answer)
    // throws WdkUserException {
    // return new UserAnswerValueBean(
    // user.getUserAnswerValueByAnswerValueFuzzy(answer.answer));
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#getAnswerValues()
    // */
    // public UserAnswerValueBean[] getUserAnswerValues() {
    // UserAnswerValue[] answers = user.getUserAnswerValues();
    // UserAnswerValueBean[] answerBeans = new UserAnswerValueBean[answers.length];
    // for (int i = 0; i < answers.length; i++) {
    // answerBeans[i] = new UserAnswerValueBean(answers[i]);
    // }
    // return answerBeans;
    // }
    //
    // public int getAnswerValueCount() {
    // return user.getUserAnswerValues().length;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#getRecordAnswerValueMap()
    // */
    // public Map<String, UserAnswerValueBean[]> getRecordAnswerValueMap() {
    // Map recUsrAnsMap = user.getRecordAnswerValueMap();
    // Map<String, UserAnswerValueBean[]> recUsrAnsBeanMap = new
    // LinkedHashMap<String, UserAnswerValueBean[]>();
    // for (Object r : recUsrAnsMap.keySet()) {
    // String rec = (String) r;
    // UserAnswerValue[] usrAns = (UserAnswerValue[]) recUsrAnsMap.get(rec);
    // UserAnswerValueBean[] answerBeans = new UserAnswerValueBean[usrAns.length];
    // for (int i = 0; i < usrAns.length; i++) {
    // answerBeans[i] = new UserAnswerValueBean(usrAns[i]);
    // }
    // recUsrAnsBeanMap.put(rec, answerBeans);
    // }
    // return recUsrAnsBeanMap;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see org.gusdb.wdk.model.User#renameAnswerValue(int, java.lang.String)
    // */
    // public void renameUserAnswerValue(int answerID, String name)
    // throws WdkUserException {
    // this.user.renameUserAnswerValue(answerID, name);
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
     *      java.lang.String, java.lang.String[])
     */
    public DatasetBean createDataset(String uploadFile, String[] values)
            throws WdkUserException, WdkModelException {
        DatasetBean bean = new DatasetBean(user.createDataset(uploadFile,
                values));
        return bean;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getDataset(java.lang.String)
     */
    public DatasetBean getDataset(String datasetChecksum)
            throws WdkUserException {
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
    public void deleteHistories() throws WdkUserException {
        user.deleteHistories();
    }

    public void deleteSteps()
	throws WdkUserException {
	user.deleteSteps();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.AnswerValue)
     */
    public HistoryBean createHistory(AnswerValueBean answer)
            throws WdkUserException, WdkModelException {
        History history = user.createHistory(answer.answer);
        return new HistoryBean(history);
    }

    public StepBean createStep(AnswerValueBean answer)
	throws WdkUserException, WdkModelException {
	Step userAnswer = user.createStep(answer.answer);
	return new StepBean(userAnswer);
    }

    public StrategyBean createStrategy(StepBean root, boolean saved)
	throws WdkUserException, WdkModelException {
	Strategy strategy = user.createStrategy(root.step, saved);
	return new StrategyBean(strategy);
    }

    public StrategyBean createStrategy(StepBean root, String name, boolean saved)
	throws WdkUserException, WdkModelException {
	Strategy strategy = user.createStrategy(root.step, name, saved);
	return new StrategyBean(strategy);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#deleteHistory(int)
     */
    public void deleteHistory(int historyId) throws WdkUserException,
            WdkModelException {
        user.deleteHistory(historyId);
    }

    public void deleteStep(int userAnswerId)
	throws WdkUserException, WdkModelException {
	user.deleteStep(userAnswerId);
    }

    public void deleteStrategy(int strategyId)
	throws WdkUserException, WdkModelException {
	user.deleteStrategy(strategyId);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories()
     */
    public HistoryBean[] getHistories() throws WdkUserException,
            WdkModelException {
        History[] histories = user.getHistories();
        HistoryBean[] beans = new HistoryBean[histories.length];
        for (int i = 0; i < histories.length; i++) {
            beans[i] = new HistoryBean(histories[i]);
        }
        return beans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories()
     */
    public HistoryBean[] getInvalidHistories() throws WdkUserException,
            WdkModelException {
        History[] histories = user.getInvalidHistories();
        HistoryBean[] beans = new HistoryBean[histories.length];
        for (int i = 0; i < histories.length; i++) {
            beans[i] = new HistoryBean(histories[i]);
        }
        return beans;
    }

    public void deleteInvalidHistories() throws WdkUserException,
            WdkModelException {
        user.deleteInvalidHistories();
    }

    public void deleteInvalidSteps()
	throws WdkUserException, WdkModelException {
	user.deleteInvalidSteps();
    }

    public Map<String, List<HistoryBean>> getHistoriesByCategory()
            throws WdkUserException, WdkModelException {
        Map<String, List<History>> histories = user.getHistoriesByCategory();
        Map<String, List<HistoryBean>> category = new LinkedHashMap<String, List<HistoryBean>>();
        for (String type : histories.keySet()) {
            List<History> list = histories.get(type);
            List<HistoryBean> beans = new ArrayList<HistoryBean>();
            for (History history : list) {
                beans.add(new HistoryBean(history));
            }
            category.put(type, beans);
        }
        return category;
    }

    public StrategyBean[] getInvalidStrategies()
	throws WdkUserException, WdkModelException {
	Strategy[] strategies = user.getInvalidStrategies();
	StrategyBean[] beans = new StrategyBean[strategies.length];
	for (int i = 0; i < strategies.length; ++i) {
	    beans[i] = new StrategyBean(strategies[i]);
	}
	return beans;
    }

    public Map<String, List<StrategyBean>> getStrategiesByCategory()
	throws WdkUserException, WdkModelException {
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
     * @see org.gusdb.wdk.model.user.User#getHistories(java.lang.String)
     */
    public HistoryBean[] getHistories(String dataType) throws WdkUserException,
            WdkModelException {
        History[] histories = user.getHistories(dataType);
        HistoryBean[] beans = new HistoryBean[histories.length];
        for (int i = 0; i < histories.length; i++) {
            beans[i] = new HistoryBean(histories[i]);
        }
        return beans;
    }

    public StrategyBean[] getStrategies(String dataType)
	throws WdkUserException, WdkModelException {
	Strategy[] strategies = user.getStrategies(dataType);
	StrategyBean[] beans = new StrategyBean[strategies.length];
	for (int i = 0; i < strategies.length; ++i) {
	    beans[i] = new StrategyBean(strategies[i]);
	}
	return beans;
    }

    public StepBean[] getSteps(String dataType)
	throws WdkUserException, WdkModelException {
	Step[] userAnswers = user.getSteps(dataType);
	StepBean[] beans = new StepBean[userAnswers.length];
	for (int i = 0; i < userAnswers.length; ++i) {
	    beans[i] = new StepBean(userAnswers[i]);
	}
	return beans;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistory(int)
     */
    public HistoryBean getHistory(int historyId) throws WdkUserException,
            WdkModelException {
        return new HistoryBean(user.getHistory(historyId));
    }

    public StepBean getStep(int userAnswerId)
	throws WdkUserException, WdkModelException {
	return new StepBean(user.getStep(userAnswerId));
    }

    public StrategyBean getStrategy(int userStrategyId)
	throws WdkUserException, WdkModelException {
	return new StrategyBean(user.getStrategy(userStrategyId));
    }

    public String validateExpression(String expression,
            Map<String, String> operatorMap) throws WdkModelException {
        return this.user.validateExpression(expression, operatorMap);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#combineHistory(java.lang.String)
     */
    public HistoryBean combineHistory(String expression)
            throws WdkUserException, WdkModelException {
        return new HistoryBean(user.combineHistory(expression));
    }

    public StepBean combineStep(String expression)
	throws WdkUserException, WdkModelException {
	return new StepBean(user.combineStep(expression));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistoryCount()
     */
    public int getHistoryCount() throws WdkUserException {
        return user.getHistoryCount();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getStrategyCount()
     */
    public int getStrategyCount() throws WdkUserException {
        return user.getStrategyCount();
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
     * @see org.gusdb.wdk.model.user.User#addSortingAttribute(java.lang.String,
     *      java.lang.String, boolean)
     */
    public String addSortingAttribute(String questionFullName, String attrName,
            boolean ascending) throws WdkUserException, WdkModelException {
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
     * @param attrName
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#addSummaryAttribute(java.lang.String,
     *      java.lang.String)
     */
    public String addSummaryAttribute(String questionFullName, String attrName)
            throws WdkUserException, WdkModelException {
        return user.addSummaryAttribute(questionFullName, attrName);
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
     * @param summaryChecksum
     * @return
     * @throws WdkUserException
     * @see org.gusdb.wdk.model.user.User#getSummaryAttributesByChecksum(java.lang.String)
     */
    public String[] getSummaryAttributesByChecksum(String summaryChecksum)
            throws WdkUserException {
        return user.getSummaryAttributesByChecksum(summaryChecksum);
    }

    /**
     * @param questionFullName
     * @param attrName
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#removeSummaryAttribute(java.lang.String,
     *      java.lang.String)
     */
    public String removeSummaryAttribute(String questionFullName,
            String attrName) throws WdkUserException, WdkModelException {
        return user.removeSummaryAttribute(questionFullName, attrName);
    }

    /**
     * @param questionFullName
     * @see org.gusdb.wdk.model.user.User#resetSummaryAttribute(java.lang.String)
     */
    public void resetSummaryAttribute(String questionFullName) {
        user.resetSummaryAttribute(questionFullName);
    }

    /**
     * @param questionFullName
     * @param attrName
     * @param moveLeft
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#arrangeSummaryAttribute(java.lang.String,
     *      java.lang.String, boolean)
     */
    public String arrangeSummaryAttribute(String questionFullName,
            String attrName, boolean moveLeft) throws WdkUserException,
            WdkModelException {
        return user.arrangeSummaryAttribute(questionFullName, attrName,
                moveLeft);
    }

    /**
     * @param questionFullName
     * @param summaryChecksum
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public String applySummaryChecksum(String questionFullName,
            String[] atributes) throws WdkModelException, WdkUserException {
        return user.applySummaryChecksum(questionFullName, atributes);
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

    /**
     * @param history
     * @param expression
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#updateHistory(org.gusdb.wdk.model.user.History,
     *      java.lang.String)
     */
    public void updateHistory(HistoryBean history, String expression)
            throws WdkUserException, WdkModelException {
        user.updateHistory(history.getHistory(), expression);
    }

    // ********************************* END ***********************************
}
