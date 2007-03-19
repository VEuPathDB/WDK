/**
 * 
 */
package org.gusdb.wdk.model.jspwrap;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.user.History;
import org.gusdb.wdk.model.user.User;

/**
 * @author: Jerric
 * @created: May 25, 2006
 * @modified by: Jerric
 * @modified at: May 25, 2006
 * 
 */
public class UserBean {
    
    private User user;
    
    /**
     * 
     */
    public UserBean( User user ) {
        this.user = user;
    }
    
    User getUser() {
        return user;
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
    public void addUserRole( String userRole ) {
        user.addUserRole( userRole );
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
    public String[ ] getUserRoles() {
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
    public void removeUserRole( String userRole ) {
        user.removeUserRole( userRole );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setAddress(java.lang.String)
     */
    public void setAddress( String address ) {
        user.setAddress( address );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setCity(java.lang.String)
     */
    public void setCity( String city ) {
        user.setCity( city );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setCountry(java.lang.String)
     */
    public void setCountry( String country ) {
        user.setCountry( country );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setDepartment(java.lang.String)
     */
    public void setDepartment( String department ) {
        user.setDepartment( department );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setFirstName(java.lang.String)
     */
    public void setFirstName( String firstName ) {
        user.setFirstName( firstName );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setLastName(java.lang.String)
     */
    public void setLastName( String lastName ) {
        user.setLastName( lastName );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setMiddleName(java.lang.String)
     */
    public void setMiddleName( String middleName ) {
        user.setMiddleName( middleName );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setOrganization(java.lang.String)
     */
    public void setOrganization( String organization ) {
        user.setOrganization( organization );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setPhoneNumber(java.lang.String)
     */
    public void setPhoneNumber( String phoneNumber ) {
        user.setPhoneNumber( phoneNumber );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setState(java.lang.String)
     */
    public void setState( String state ) {
        user.setState( state );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setTitle(java.lang.String)
     */
    public void setTitle( String title ) {
        user.setTitle( title );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#setZipCode(java.lang.String)
     */
    public void setZipCode( String zipCode ) {
        user.setZipCode( zipCode );
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
    public void changePassword( String oldPassword, String newPassword,
            String confirmPassword ) throws WdkUserException {
        user.changePassword( oldPassword, newPassword, confirmPassword );
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
    
    public Map< String, String > getGlobalPreferences() {
        return user.getGlobalPreferences();
    }
    
    public Map< String, String > getProjectPreferences() {
        return user.getProjectPreferences();
    }
    
    public void setGlobalPreference( String prefName, String prefValue ) {
        user.setGlobalPreference( prefName, prefValue );
    }
    
    public void setProjectPreference( String prefName, String prefValue ) {
        user.setProjectPreference( prefName, prefValue );
    }
    
    public void unsetGlobalPreference( String prefName ) {
        user.unsetGlobalPreference( prefName );
    }
    
    public void unsetProjectPreference( String prefName ) {
        user.unsetProjectPreference( prefName );
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
    public DatasetBean createDataset( String uploadFile, String[ ] values )
            throws WdkUserException {
        DatasetBean bean = new DatasetBean( user.createDataset( uploadFile,
                values ) );
        return bean;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#deleteDataset(java.lang.String)
     */
    public void deleteDataset( int datasetId ) throws WdkUserException {
        user.deleteDataset( datasetId );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getDataset(java.lang.String)
     */
    public DatasetBean getDataset( int datasetId ) throws WdkUserException {
        return new DatasetBean( user.getDataset( datasetId ) );
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
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#createHistory(org.gusdb.wdk.model.Answer)
     */
    public HistoryBean createHistory( AnswerBean answer )
            throws WdkUserException, WdkModelException {
        History history = user.createHistory( answer.answer );
        return new HistoryBean( history );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#deleteHistory(int)
     */
    public void deleteHistory( int historyId ) throws WdkUserException,
            WdkModelException {
        user.deleteHistory( historyId );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories()
     */
    public HistoryBean[ ] getHistories() throws WdkUserException,
            WdkModelException {
        History[ ] histories = user.getHistories();
        HistoryBean[ ] beans = new HistoryBean[ histories.length ];
        for ( int i = 0; i < histories.length; i++ ) {
            beans[ i ] = new HistoryBean( histories[ i ] );
        }
        return beans;
    }
    
    public Map< String, List< HistoryBean >> getHistoriesByCategory()
            throws WdkUserException, WdkModelException {
        Map< String, List< History >> histories = user.getHistoriesByCategory();
        Map< String, List< HistoryBean >> category = new LinkedHashMap< String, List< HistoryBean >>();
        for ( String type : histories.keySet() ) {
            List< History > list = histories.get( type );
            List< HistoryBean > beans = new ArrayList< HistoryBean >();
            for ( History history : list ) {
                beans.add( new HistoryBean( history ) );
            }
            category.put( type, beans );
        }
        return category;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistories(java.lang.String)
     */
    public HistoryBean[ ] getHistories( String dataType )
            throws WdkUserException, WdkModelException {
        History[ ] histories = user.getHistories( dataType );
        HistoryBean[ ] beans = new HistoryBean[ histories.length ];
        for ( int i = 0; i < histories.length; i++ ) {
            beans[ i ] = new HistoryBean( histories[ i ] );
        }
        return beans;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#getHistory(int)
     */
    public HistoryBean getHistory( int historyId ) throws WdkUserException,
            WdkModelException {
        return new HistoryBean( user.getHistory( historyId ) );
    }
    
    public String validateExpression( String expression,
            Map< String, String > operatorMap ) throws WdkModelException {
        return this.user.validateExpression( expression, operatorMap );
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.user.User#combineHistory(java.lang.String)
     */
    public HistoryBean combineHistory( String expression )
            throws WdkUserException, WdkModelException {
        return new HistoryBean( user.combineHistory( expression ) );
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
    public void setItemsPerPage( int itemsPerPage ) throws WdkUserException {
        user.setItemsPerPage( itemsPerPage );
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
    public void addSortingAttribute( String questionFullName, String attrName,
            boolean ascending ) throws WdkUserException, WdkModelException {
        user.addSortingAttribute( questionFullName, attrName, ascending );
    }
    
    /**
     * @param questionFullName
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#getSortingAttributes(java.lang.String)
     */
    public Map< String, Boolean > getSortingAttributes( String questionFullName )
            throws WdkUserException, WdkModelException {
        return user.getSortingAttributes( questionFullName );
    }
    
    /**
     * @param questionFullName
     * @param attrName
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#addSummaryAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void addSummaryAttribute( String questionFullName, String attrName )
            throws WdkUserException, WdkModelException {
        user.addSummaryAttribute( questionFullName, attrName );
    }
    
    /**
     * @param questionFullName
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#getSummaryAttributes(java.lang.String)
     */
    public String[ ] getSummaryAttributes( String questionFullName )
            throws WdkUserException, WdkModelException {
        return user.getSummaryAttributes( questionFullName );
    }
    
    /**
     * @param questionFullName
     * @param attrName
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#removeSummaryAttribute(java.lang.String,
     *      java.lang.String)
     */
    public void removeSummaryAttribute( String questionFullName, String attrName )
            throws WdkUserException, WdkModelException {
        user.removeSummaryAttribute( questionFullName, attrName );
    }
    
    /**
     * @param questionFullName
     * @see org.gusdb.wdk.model.user.User#resetSummaryAttribute(java.lang.String)
     */
    public void resetSummaryAttribute( String questionFullName ) {
        user.resetSummaryAttribute( questionFullName );
    }
    
    /**
     * @param questionFullName
     * @param attrName
     * @param moveLeft
     * @throws WdkUserException
     * @throws WdkModelException
     * @see org.gusdb.wdk.model.user.User#arrangeSummaryAttribute(java.lang.String, java.lang.String, boolean)
     */
    public void arrangeSummaryAttribute( String questionFullName, String attrName, boolean moveLeft ) throws WdkUserException, WdkModelException {
        user.arrangeSummaryAttribute( questionFullName, attrName, moveLeft );
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.user.toString();
    }
    
    // ********************************* END ***********************************
}
