/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Answer;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.UserAnswer;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.implementation.SqlUtils;

/**
 * @author xingao
 * 
 */
public class User {

    private Logger logger = Logger.getLogger(User.class);

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

    private Set<String> userRoles;
    private boolean guest = true;

    /**
     * The interval for caching user's history and preference. If the period
     * since last refresh is bigger than it, the history and/or preference will
     * be fetched from the database again.
     */
    private int refreshInterval;
    /**
     * ticks since the last time of refreshing history cache
     */
    private long lastRefreshHistory;
    /**
     * ticks since the last time of refreshing preference cache/
     */
    private long lastRefreshPreference;

    private UserFactory factory;

    /**
     * the histories for the user: <historyId, history>. It only contains the
     * histories for the current project
     */
    private Map<Integer, History> histories;
    /**
     * the preferences for the user: <prefName, prefValue>. It only contains the
     * preferences for the current project
     */
    private Map<String, String> preferences;

    // *************************************************************************
    // Copied from the riginal code - to be updated soon
    // *************************************************************************
    private String userID;
    private Map<Integer, UserAnswer> userAnswers;
    private int answerIndex;
    /**
     * the WdkModel is used when growing BooleanQuestionNode
     */
    WdkModel model;
    private boolean cleared = false;

    public User(String userID, WdkModel model) {
        this.model = model;
        this.userID = userID;
        this.answerIndex = 0;
        // don't create the userAnswers map by default, since there may be many
        // users at the same time, and it would consume too many memory; so I
        // only create it when it's used.
    }

    public String getUserID() {
        //return this.userID;
        return email;
    }

    public void addAnswer(Answer answer) throws WdkUserException {
        try {
            getUserAnswerByAnswer(answer, false);
            // answer exists, return
            return;
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            // System.err.println(ex);
        }
        insertAnswer(answer);
    }

    public void addAnswerFuzzy(Answer answer) throws WdkUserException {
        try {
            getUserAnswerByAnswer(answer, true);
            // answer exists, return
            return;
        } catch (WdkUserException ex) {
            // TODO Auto-generated catch block
            // ex.printStackTrace();
            // System.err.println(ex);
        }
        insertAnswer(answer);
    }

    private void insertAnswer(Answer answer) throws WdkUserException {
        answerIndex++;
        UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);

        // initialize userAnswers map
        if (userAnswers == null)
            userAnswers = new LinkedHashMap<Integer, UserAnswer>();
        userAnswers.put(answerIndex, userAnswer);

        // cache the history
        try {
            saveHistory(userAnswer);
        } catch (WdkModelException ex) {
            throw new WdkUserException(ex);
        }
    }

    public void deleteUserAnswer(int answerId) throws WdkUserException {
        if (userAnswers == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        UserAnswer answer = userAnswers.remove(answerId);
        if (answer == null)
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");
        if (userAnswers.isEmpty()) userAnswers = null;

        // also delete the history record in the database
        deleteHistory(answerId);
    }

    private synchronized void deleteHistory(int historyID)
            throws WdkUserException {
        String historyTableName = model.getResultFactory().getHistoryTableName();
        DataSource dataSource = model.getRDBMSPlatform().getDataSource();

        StringBuffer sb = new StringBuffer("DELETE FROM ");
        sb.append(historyTableName);
        sb.append(" WHERE ");
        sb.append(ResultFactory.FIELD_USER_ID);
        sb.append("='" + userID + "'");
        sb.append(" AND ");
        sb.append(ResultFactory.FIELD_HISTORY_ID);
        sb.append("=");
        sb.append(historyID);

        // execute the deletion
        try {
            SqlUtils.executeUpdate(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.error("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex);
        }
    }

    public void clearUserAnswers() throws WdkUserException {
        cleared = true;
        if (userAnswers != null) userAnswers.clear();
        userAnswers = null;

        // clear the history cache, too
        String historyTableName = model.getResultFactory().getHistoryTableName();
        DataSource dataSource = model.getRDBMSPlatform().getDataSource();

        StringBuffer sb = new StringBuffer("DELETE FROM ");
        sb.append(historyTableName);
        sb.append(" WHERE ");
        sb.append(ResultFactory.FIELD_USER_ID);
        sb.append("='");
        sb.append(userID);
        sb.append("'");
        try {
            SqlUtils.execute(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.error("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex);
        }
    }

    public UserAnswer[] getUserAnswers() {
        if (userAnswers == null || userAnswers.size() == 0)
            return new UserAnswer[0];
        UserAnswer[] answers = new UserAnswer[userAnswers.size()];
        userAnswers.values().toArray(answers);
        return answers;
    }

    public Map getRecordAnswerMap() {
        Map<String, Map<Integer, UserAnswer>> recAnsMapMap = new LinkedHashMap<String, Map<Integer, UserAnswer>>();
        if (userAnswers == null || userAnswers.size() == 0)
            return recAnsMapMap;

        for (int ansID : userAnswers.keySet()) {
            UserAnswer usrAns = userAnswers.get(new Integer(ansID));
            String rec = usrAns.getAnswer().getQuestion().getRecordClass().getFullName();
            if (recAnsMapMap.get(rec) == null) {
                recAnsMapMap.put(rec, new LinkedHashMap<Integer, UserAnswer>());
            }
            Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
            recAnsMapMap1.put(new Integer(ansID), usrAns);
        }

        // wants answers in sorted arrays
        Map<String, UserAnswer[]> recAnsMap = new LinkedHashMap<String, UserAnswer[]>();
        for (Object r : recAnsMapMap.keySet()) {
            String rec = (String) r;
            Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
            List ansIDList = Arrays.asList(recAnsMapMap1.keySet().toArray());
            Collections.sort(ansIDList);
            Collections.reverse(ansIDList);
            Object[] sortedAnsIDs = ansIDList.toArray();
            Vector v = new Vector();
            for (int i = 0; i < sortedAnsIDs.length; i++) {
                v.add(recAnsMapMap1.get((Integer) sortedAnsIDs[i]));
            }
            UserAnswer[] sortedUsrAns = new UserAnswer[v.size()];
            v.copyInto(sortedUsrAns);

            recAnsMap.put(rec, sortedUsrAns);
        }

        return recAnsMap;
    }

    public UserAnswer getUserAnswerByID(int answerID) throws WdkUserException {
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            throw new WdkUserException("The answer of ID " + answerID
                    + " does not exist!");
        return userAnswers.get(answerID);
    }

    public UserAnswer getUserAnswerByName(String name) throws WdkUserException {
        if (userAnswers != null) {
            for (UserAnswer answer : userAnswers.values()) {
                if (answer.getName().equalsIgnoreCase(name)) return answer;
            }
        }
        throw new WdkUserException("The answer of name " + name
                + " does not exist!");
    }

    public UserAnswer getUserAnswerByAnswerFuzzy(Answer answer)
            throws WdkUserException {
        return getUserAnswerByAnswer(answer, true);
    }

    public UserAnswer getUserAnswerByAnswer(Answer answer)
            throws WdkUserException {
        return getUserAnswerByAnswer(answer, false);
    }

    private UserAnswer getUserAnswerByAnswer(Answer answer, boolean ignorePage)
            throws WdkUserException {
        if (userAnswers != null) {
            // check if the answer exists or not
            for (UserAnswer uans : userAnswers.values()) {
                Answer ans = uans.getAnswer();
                // check question name
                String qname = ans.getQuestion().getFullName();
                if (!qname.equalsIgnoreCase(answer.getQuestion().getFullName()))
                    continue;

                // check paging number
                if (!ignorePage
                        && (ans.getStartRecordInstanceI() != answer.getStartRecordInstanceI() || ans.getEndRecordInstanceI() != answer.getEndRecordInstanceI()))
                    continue;

                // check parameters
                Map params = ans.getParams();
                Map pchecks = answer.getParams();
                Iterator it = params.keySet().iterator();
                boolean equal = true;
                while (it.hasNext()) {
                    String key = (String) it.next();
                    String value = params.get(key).toString();
                    // check on the input answer
                    if (pchecks.containsKey(key)) {
                        String vcheck = pchecks.get(key).toString();
                        if (!value.equalsIgnoreCase(vcheck)) {
                            equal = false;
                            break;
                        }
                    } else {
                        equal = false;
                        break;
                    }
                }
                // check if two answers are the same
                if (equal) return uans;
            }
        }
        throw new WdkUserException(
                "The UserAnswer specified by the given answer doesn't exist!");
    }

    public void renameUserAnswer(int answerID, String name)
            throws WdkUserException {
        // check if the answer exists
        if (userAnswers == null || !userAnswers.containsKey(answerID))
            throw new WdkUserException(
                    "The answer specified by the given ID doesn't exist!");

        // check if the answer name is unique
        for (int ansID : userAnswers.keySet()) {
            if (ansID != answerID) {
                UserAnswer answer = userAnswers.get(ansID);
                if (answer.getName().equalsIgnoreCase(name))
                    throw new WdkUserException(
                            "Duplicated name of the answer for this user");
            }
        }
        // name is unique in user's session scope
        UserAnswer answer = userAnswers.get(answerID);
        answer.setName(name);
    }

    public UserAnswer combineUserAnswers(int firstAnswerID, int secondAnswerID,
            String operation, int startIndex, int endIndex,
            Map<String, String> operatorMap) throws WdkModelException,
            WdkUserException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct the expression
        StringBuffer sb = new StringBuffer();
        sb.append(firstAnswerID);
        sb.append(' ');
        sb.append(operation);
        sb.append(" ");
        sb.append(secondAnswerID);

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        BooleanQuestionNode root = be.parseExpression(sb.toString(),
                operandMap, operatorMap);

        // create a new UserAnswer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        // set user answer as combined
        UserAnswer userAnswer = getUserAnswerByAnswer(answer);
        userAnswer.setCombinedAnswer(true);
        userAnswer.setName(sb.toString());
        return userAnswer;
    }

    public UserAnswer combineUserAnswers(String expression, int startIndex,
            int endIndex, Map<String, String> operatorMap)
            throws WdkUserException, WdkModelException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        BooleanQuestionNode root = be.parseExpression(expression, operandMap,
                operatorMap);

        // make answer
        Answer answer = root.makeAnswer(startIndex, endIndex);
        addAnswer(answer);
        // set user answer as combined
        UserAnswer userAnswer = getUserAnswerByAnswer(answer);
        userAnswer.setCombinedAnswer(true);
        userAnswer.setName(expression);
        return userAnswer;
    }

    public String validateExpression(String expression, int startIndex,
            int endIndex, Map<String, String> operatorMap)
            throws WdkModelException {
        // construct operand map
        Map<String, Answer> operandMap = buildOperandMap();

        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(model);
        try {
            be.parseExpression(expression, operandMap, operatorMap);
        } catch (WdkUserException ue) {
            return ue.getMessage();
        }
        return null;
    }

    private Map<String, Answer> buildOperandMap() {
        Map<String, Answer> operandMap = new LinkedHashMap<String, Answer>();
        for (int answerID : userAnswers.keySet()) {
            UserAnswer userAnswer = userAnswers.get(answerID);
            operandMap.put(Integer.toString(answerID), userAnswer.getAnswer());
            operandMap.put(userAnswer.getName(), userAnswer.getAnswer());
        }
        return operandMap;
    }

    public String toString() {
        String newline = System.getProperty("line.separator");
        StringBuffer sb = new StringBuffer();

        sb.append("==================================");
        sb.append(newline);
        sb.append("UserID=" + userID);
        int size = (userAnswers != null) ? userAnswers.size() : 0;
        sb.append("\t#Answers=" + size);
        sb.append(newline);
        if (userAnswers != null) {
            sb.append("----------------------------------");
            sb.append(newline);
            sb.append("ID\tType\t\t\tName");
            sb.append(newline);
            for (UserAnswer answer : userAnswers.values()) {
                sb.append(answer.getAnswerID());
                sb.append("\t" + answer.getType());
                sb.append("\t" + answer.getName());
                sb.append(newline);
            }
        }
        sb.append(newline);
        return sb.toString();
    }

    private void saveHistory(UserAnswer userAnswer) throws WdkModelException,
            WdkUserException {
        int historyID = userAnswer.getAnswerID();
        Integer tempID = userAnswer.getAnswer().getDatasetId();
        StringBuffer sb = new StringBuffer("SELECT * FROM ");
        try {
            if (tempID == null) {
                // read the record to make sure the id is initialized
                userAnswer.getAnswer().getResultSize();
                tempID = userAnswer.getAnswer().getDatasetId();
            }
            int datasetID = tempID.intValue();

            String historyTableName = model.getResultFactory().getHistoryTableName();
            DataSource dataSource = model.getRDBMSPlatform().getDataSource();

            // check if the same history ID has been used; if so, replace the
            // old
            // one; otherwise, insert a new record
            sb.append(historyTableName);
            sb.append(" WHERE ");
            sb.append(ResultFactory.FIELD_USER_ID);
            sb.append("='");
            sb.append(userID);
            sb.append("' AND ");
            sb.append(ResultFactory.FIELD_HISTORY_ID);
            sb.append("=");
            sb.append(historyID);

            ResultSet rs = SqlUtils.getResultSet(dataSource, sb.toString());
            sb.delete(0, sb.length());
            if (rs.next()) { // has existing history, replace old one
                sb.append("UPDATE ");
                sb.append(historyTableName);
                sb.append(" SET ");
                sb.append(ResultFactory.FIELD_DATASET_ID);
                sb.append("=");
                sb.append(datasetID);
                sb.append(" WHERE ");
                sb.append(ResultFactory.FIELD_USER_ID);
                sb.append("='");
                sb.append(userID);
                sb.append("' AND ");
                sb.append(ResultFactory.FIELD_HISTORY_ID);
                sb.append("=");
                sb.append(historyID);
            } else { // no matched history, insert a new one
                sb.append("INSERT INTO ");
                sb.append(historyTableName);
                sb.append(" (");
                sb.append(ResultFactory.FIELD_USER_ID);
                sb.append(", ");
                sb.append(ResultFactory.FIELD_HISTORY_ID);
                sb.append(", ");
                sb.append(ResultFactory.FIELD_DATASET_ID);
                sb.append(") VALUES ('");
                sb.append(userID);
                sb.append("', ");
                sb.append(historyID);
                sb.append(", ");
                sb.append(datasetID);
                sb.append(")");
            }
            SqlUtils.closeResultSet(rs);
            // execute update/insert
            SqlUtils.executeUpdate(dataSource, sb.toString());
        } catch (SQLException ex) {
            logger.error("Got an SQLException: " + ex.toString());
            throw new WdkUserException(ex.getMessage() + ": " + sb);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#finalize()
     */
    @Override
    protected void finalize() throws Throwable {
        // the user's history hasn't been cleared. in this place the user object
        // removes the query histories
        if (!cleared) clearUserAnswers();
        super.finalize();
    }

    // ******************************** END ************************************

    User(String email, UserFactory factory) {
        this.email = email;
        this.factory = factory;
        userRoles = new LinkedHashSet<String>();

        refreshInterval = -1;
        
        this.model = factory.getWdkModel();
    }

    /**
     * @return Returns the email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * @return Returns the address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param address
     *            The address to set.
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
     *            The city to set.
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
     *            The country to set.
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
     *            The department to set.
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
     *            The firstName to set.
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
     *            The lastName to set.
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
     *            The middleName to set.
     */
    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    /**
     * @return Returns the organization.
     */
    public String getOrganization() {
        return organization;
    }

    /**
     * @param organization
     *            The organization to set.
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
     *            The phoneNumber to set.
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
     *            The state to set.
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
     *            The title to set.
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
     *            The zipCode to set.
     */
    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    /**
     * @return Returns the refreshInterval.
     */
    public int getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @return Returns the guest.
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
     *            The userRole to set.
     */
    public void addUserRole(String userRole) {
        this.userRoles.add(userRole);
    }

    public void removeUserRole(String userRole) {
        userRoles.remove(userRole);
    }

    /**
     * @param guest
     *            The guest to set.
     */
    void setGuest(boolean guest) {
        this.guest = guest;
    }

    /**
     * @param refreshInterval
     *            The refreshInterval to set.
     */
    void setRefreshInterval(int refreshInterval) {
        this.refreshInterval = refreshInterval;
    }

    /**
     * create and save a history from the answer
     * 
     * @param answer
     * @return
     */
    public History createHistory(Answer answer) {
        // TODO
        return null;
    }

    /**
     * this method is used by the factory when loading histories from database
     * 
     * @param history
     */
    void addHistory(History history) {
    // TODO
    }

    public void mergeHistory(User user) {
    // TODO
    }

    /**
     * get an array of cached histories in the current project site; if the
     * cache is expired. it will be refreshed from the database
     * 
     * @return
     */
    public History[] getHistories() {
        // TODO
        return null;
    }

    public Map<Integer, History> getHistoryMap() {
        // TODO
        return null;
    }

    /**
     * return a list
     * 
     * @param namePattern
     * @return
     */
    public History[] queryHistories(String namePattern) {
        List<History> hits = new ArrayList<History>();
        // TODO
        return null;
    }

    public void saveHistory(History history) {
    // TODO
    }

    public void clearHistories() {
    // TODO
    }

    public void deleteHistory(History history) {
    // TODO
    }

    public void savePreference(String prefName, String prefValue) {
    // TODO
    }

    public Map<String, String> getPreferenceMap() {
        // TODO
        return null;
    }

    public Map<String, String> queryPreferences(String namePattern) {
        // TODO
        return null;
    }

    public void changePassword(String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException, WdkModelException {
        factory.changePassword(email, oldPassword, newPassword, confirmPassword);
    }
}
