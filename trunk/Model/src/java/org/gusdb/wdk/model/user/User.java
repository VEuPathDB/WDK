/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.*;
import org.gusdb.wdk.model.DatasetParam.InputType;

/**
 * @author xingao
 * 
 */
public class User {

    public final static String PREF_ITEMS_PER_PAGE = "preference_global_items_per_page";

    private Logger logger = Logger.getLogger(User.class);

    private WdkModel model;
    private UserFactory userFactory;
    private DatasetFactory datasetFactory;
    private int userId;

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
     * the preferences for the user: <prefName, prefValue>. It only contains the
     * preferences for the current project
     */
    private Map<String, String> globalPreferences;
    private Map<String, String> projectPreferences;

    // private boolean cleared = false;
    //
    // public User(String email, WdkModel model) {
    // this.model = model;
    // this.email = email;
    //
    // globalPreferences = new LinkedHashMap<String, String>();
    // projectPreferences = new LinkedHashMap<String, String>();
    // histories = new ArrayList<History>();
    // }
    //
    // public void addAnswer(Answer answer) throws WdkUserException,
    // WdkModelException {
    // try {
    // getUserAnswerByAnswer(answer, false);
    // // answer exists, return
    // return;
    // } catch (WdkUserException ex) {
    // // ex.printStackTrace();
    // // System.err.println(ex);
    // }
    // insertAnswer(answer);
    // }
    //
    // public void addAnswerFuzzy(Answer answer) throws WdkUserException,
    // WdkModelException {
    // try {
    // getUserAnswerByAnswer(answer, true);
    // // answer exists, return
    // return;
    // } catch (WdkUserException ex) {
    // // ex.printStackTrace();
    // // System.err.println(ex);
    // }
    // insertAnswer(answer);
    // }
    //
    // private void insertAnswer(Answer answer) throws WdkUserException,
    // WdkModelException {
    // answerIndex++;
    // UserAnswer userAnswer = new UserAnswer(userID, answerIndex, answer);
    //
    // // initialize userAnswers map
    // if (userAnswers == null)
    // userAnswers = new LinkedHashMap<Integer, UserAnswer>();
    // userAnswers.put(answerIndex, userAnswer);
    //
    // // cache the history
    // saveHistory(userAnswer);
    // }
    //
    // public void deleteUserAnswer(int answerId) throws WdkUserException {
    // if (userAnswers == null)
    // throw new WdkUserException(
    // "The answer specified by the given ID doesn't exist!");
    // UserAnswer answer = userAnswers.remove(answerId);
    // if (answer == null)
    // throw new WdkUserException(
    // "The answer specified by the given ID doesn't exist!");
    // if (userAnswers.isEmpty()) userAnswers = null;
    //
    // // also delete the history record in the database
    // deleteHistory(answerId);
    // }
    //
    // private synchronized void deleteHistory(int historyID)
    // throws WdkUserException {
    // String historyTableName = model.getResultFactory().getHistoryTableName();
    // DataSource dataSource = model.getRDBMSPlatform().getDataSource();
    //
    // StringBuffer sb = new StringBuffer("DELETE FROM ");
    // sb.append(historyTableName);
    // sb.append(" WHERE ");
    // sb.append(ResultFactory.FIELD_USER_ID);
    // sb.append("='" + userID + "'");
    // sb.append(" AND ");
    // sb.append(ResultFactory.FIELD_HISTORY_ID);
    // sb.append("=");
    // sb.append(historyID);
    //
    // // execute the deletion
    // try {
    // SqlUtils.executeUpdate(dataSource, sb.toString());
    // } catch (SQLException ex) {
    // logger.error("Got an SQLException: " + ex.toString());
    // throw new WdkUserException(ex);
    // }
    // }
    //
    // public void clearUserAnswers() throws WdkUserException {
    // cleared = true;
    // if (userAnswers != null) userAnswers.clear();
    // userAnswers = null;
    //
    // // clear the history cache, too
    // String historyTableName = model.getResultFactory().getHistoryTableName();
    // DataSource dataSource = model.getRDBMSPlatform().getDataSource();
    //
    // StringBuffer sb = new StringBuffer("DELETE FROM ");
    // sb.append(historyTableName);
    // sb.append(" WHERE ");
    // sb.append(ResultFactory.FIELD_USER_ID);
    // sb.append("='");
    // sb.append(userID);
    // sb.append("'");
    // try {
    // SqlUtils.execute(dataSource, sb.toString());
    // } catch (SQLException ex) {
    // logger.error("Got an SQLException: " + ex.toString());
    // throw new WdkUserException(ex);
    // }
    // }
    //
    // public UserAnswer[] getUserAnswers() {
    // if (userAnswers == null || userAnswers.size() == 0)
    // return new UserAnswer[0];
    // UserAnswer[] answers = new UserAnswer[userAnswers.size()];
    // userAnswers.values().toArray(answers);
    // return answers;
    // }
    //
    // public Map getRecordAnswerMap() {
    // Map<String, Map<Integer, UserAnswer>> recAnsMapMap = new
    // LinkedHashMap<String, Map<Integer, UserAnswer>>();
    // if (userAnswers == null || userAnswers.size() == 0)
    // return recAnsMapMap;
    //
    // for (int ansID : userAnswers.keySet()) {
    // UserAnswer usrAns = userAnswers.get(new Integer(ansID));
    // String rec =
    // usrAns.getAnswer().getQuestion().getRecordClass().getFullName();
    // if (recAnsMapMap.get(rec) == null) {
    // recAnsMapMap.put(rec, new LinkedHashMap<Integer, UserAnswer>());
    // }
    // Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
    // recAnsMapMap1.put(new Integer(ansID), usrAns);
    // }
    //
    // // wants answers in sorted arrays
    // Map<String, UserAnswer[]> recAnsMap = new LinkedHashMap<String,
    // UserAnswer[]>();
    // for (Object r : recAnsMapMap.keySet()) {
    // String rec = (String) r;
    // Map<Integer, UserAnswer> recAnsMapMap1 = recAnsMapMap.get(rec);
    // List ansIDList = Arrays.asList(recAnsMapMap1.keySet().toArray());
    // Collections.sort(ansIDList);
    // Collections.reverse(ansIDList);
    // Object[] sortedAnsIDs = ansIDList.toArray();
    // Vector v = new Vector();
    // for (int i = 0; i < sortedAnsIDs.length; i++) {
    // v.add(recAnsMapMap1.get((Integer) sortedAnsIDs[i]));
    // }
    // UserAnswer[] sortedUsrAns = new UserAnswer[v.size()];
    // v.copyInto(sortedUsrAns);
    //
    // recAnsMap.put(rec, sortedUsrAns);
    // }
    //
    // return recAnsMap;
    // }
    //
    // public UserAnswer getUserAnswerByID(int answerID) throws WdkUserException
    // {
    // if (userAnswers == null || !userAnswers.containsKey(answerID))
    // throw new WdkUserException("The answer of ID " + answerID
    // + " does not exist!");
    // return userAnswers.get(answerID);
    // }
    //
    // public UserAnswer getUserAnswerByName(String name) throws
    // WdkUserException {
    // if (userAnswers != null) {
    // for (UserAnswer answer : userAnswers.values()) {
    // if (answer.getName().equalsIgnoreCase(name)) return answer;
    // }
    // }
    // throw new WdkUserException("The answer of name " + name
    // + " does not exist!");
    // }
    //
    // public UserAnswer getUserAnswerByAnswerFuzzy(Answer answer)
    // throws WdkUserException {
    // return getUserAnswerByAnswer(answer, true);
    // }
    //
    // public UserAnswer getUserAnswerByAnswer(Answer answer)
    // throws WdkUserException {
    // return getUserAnswerByAnswer(answer, false);
    // }
    //
    // private UserAnswer getUserAnswerByAnswer(Answer answer, boolean
    // ignorePage)
    // throws WdkUserException {
    // if (userAnswers != null) {
    // // check if the answer exists or not
    // for (UserAnswer uans : userAnswers.values()) {
    // Answer ans = uans.getAnswer();
    // // check question name
    // String qname = ans.getQuestion().getFullName();
    // if (!qname.equalsIgnoreCase(answer.getQuestion().getFullName()))
    // continue;
    //
    // // check paging number
    // if (!ignorePage
    // && (ans.getStartRecordInstanceI() != answer.getStartRecordInstanceI() ||
    // ans.getEndRecordInstanceI() != answer.getEndRecordInstanceI()))
    // continue;
    //
    // // check parameters
    // Map params = ans.getParams();
    // Map pchecks = answer.getParams();
    // Iterator it = params.keySet().iterator();
    // boolean equal = true;
    // while (it.hasNext()) {
    // String key = (String) it.next();
    // String value = params.get(key).toString();
    // // check on the input answer
    // if (pchecks.containsKey(key)) {
    // String vcheck = pchecks.get(key).toString();
    // if (!value.equalsIgnoreCase(vcheck)) {
    // equal = false;
    // break;
    // }
    // } else {
    // equal = false;
    // break;
    // }
    // }
    // // check if two answers are the same
    // if (equal) return uans;
    // }
    // }
    // throw new WdkUserException(
    // "The UserAnswer specified by the given answer doesn't exist!");
    // }
    //
    // public void renameUserAnswer(int answerID, String name)
    // throws WdkUserException {
    // // check if the answer exists
    // if (userAnswers == null || !userAnswers.containsKey(answerID))
    // throw new WdkUserException(
    // "The answer specified by the given ID doesn't exist!");
    //
    // // check if the answer name is unique
    // for (int ansID : userAnswers.keySet()) {
    // if (ansID != answerID) {
    // UserAnswer answer = userAnswers.get(ansID);
    // if (answer.getName().equalsIgnoreCase(name))
    // throw new WdkUserException(
    // "Duplicated name of the answer for this user");
    // }
    // }
    // // name is unique in user's session scope
    // UserAnswer answer = userAnswers.get(answerID);
    // answer.setName(name);
    // }
    //
    // public UserAnswer combineUserAnswers(int firstAnswerID, int
    // secondAnswerID,
    // String operation, int startIndex, int endIndex,
    // Map<String, String> operatorMap) throws WdkUserException,
    // WdkModelException {
    // // construct operand map
    // Map<String, Answer> operandMap = buildOperandMap();
    //
    // // construct the expression
    // StringBuffer sb = new StringBuffer();
    // sb.append(firstAnswerID);
    // sb.append(' ');
    // sb.append(operation);
    // sb.append(" ");
    // sb.append(secondAnswerID);
    //
    // // construct BooleanQuestionNode
    // BooleanExpression be = new BooleanExpression(model);
    // BooleanQuestionNode root = be.parseExpression(sb.toString(),
    // operandMap, operatorMap);
    //
    // // create a new UserAnswer
    // Answer answer = root.makeAnswer(startIndex, endIndex);
    // addAnswer(answer);
    // // set user answer as combined
    // UserAnswer userAnswer = getUserAnswerByAnswer(answer);
    // userAnswer.setCombinedAnswer(true);
    // userAnswer.setName(sb.toString());
    // return userAnswer;
    // }
    //
    // public UserAnswer combineUserAnswers(String expression, int startIndex,
    // int endIndex, Map<String, String> operatorMap)
    // throws WdkUserException, WdkModelException {
    // // construct operand map
    // Map<String, Answer> operandMap = buildOperandMap();
    //
    // // construct BooleanQuestionNode
    // BooleanExpression be = new BooleanExpression(model);
    // BooleanQuestionNode root = be.parseExpression(expression, operandMap,
    // operatorMap);
    //
    // // make answer
    // Answer answer = root.makeAnswer(startIndex, endIndex);
    // addAnswer(answer);
    // // set user answer as combined
    // UserAnswer userAnswer = getUserAnswerByAnswer(answer);
    // userAnswer.setCombinedAnswer(true);
    // userAnswer.setName(expression);
    // return userAnswer;
    // }
    //
    //
    // private Map<String, Answer> buildOperandMap() {
    // Map<String, Answer> operandMap = new LinkedHashMap<String, Answer>();
    // for (int answerID : userAnswers.keySet()) {
    // UserAnswer userAnswer = userAnswers.get(answerID);
    // operandMap.put(Integer.toString(answerID), userAnswer.getAnswer());
    // operandMap.put(userAnswer.getName(), userAnswer.getAnswer());
    // }
    // return operandMap;
    // }
    //
    // public String toString() {
    // String newline = System.getProperty("line.separator");
    // StringBuffer sb = new StringBuffer();
    //
    // sb.append("==================================");
    // sb.append(newline);
    // sb.append("UserID=" + userID);
    // int size = (userAnswers != null) ? userAnswers.size() : 0;
    // sb.append("\t#Answers=" + size);
    // sb.append(newline);
    // if (userAnswers != null) {
    // sb.append("----------------------------------");
    // sb.append(newline);
    // sb.append("ID\tType\t\t\tName");
    // sb.append(newline);
    // for (UserAnswer answer : userAnswers.values()) {
    // sb.append(answer.getAnswerID());
    // sb.append("\t" + answer.getType());
    // sb.append("\t" + answer.getName());
    // sb.append(newline);
    // }
    // }
    // sb.append(newline);
    // return sb.toString();
    // }
    //
    // private void saveHistory(UserAnswer userAnswer) throws WdkUserException,
    // WdkModelException {
    // int historyID = userAnswer.getAnswerID();
    // Integer tempID = userAnswer.getAnswer().getDatasetId();
    // StringBuffer sb = new StringBuffer("SELECT * FROM ");
    // try {
    // if (tempID == null) {
    // // read the record to make sure the id is initialized
    // userAnswer.getAnswer().getResultSize();
    // tempID = userAnswer.getAnswer().getDatasetId();
    // }
    // int datasetID = tempID.intValue();
    //
    // String historyTableName = model.getResultFactory().getHistoryTableName();
    // DataSource dataSource = model.getRDBMSPlatform().getDataSource();
    //
    // // check if the same history ID has been used; if so, replace the
    // // old
    // // one; otherwise, insert a new record
    // sb.append(historyTableName);
    // sb.append(" WHERE ");
    // sb.append(ResultFactory.FIELD_USER_ID);
    // sb.append("='");
    // sb.append(userID);
    // sb.append("' AND ");
    // sb.append(ResultFactory.FIELD_HISTORY_ID);
    // sb.append("=");
    // sb.append(historyID);
    //
    // ResultSet rs = SqlUtils.getResultSet(dataSource, sb.toString());
    // sb.delete(0, sb.length());
    // if (rs.next()) { // has existing history, replace old one
    // sb.append("UPDATE ");
    // sb.append(historyTableName);
    // sb.append(" SET ");
    // sb.append(ResultFactory.FIELD_DATASET_ID);
    // sb.append("=");
    // sb.append(datasetID);
    // sb.append(" WHERE ");
    // sb.append(ResultFactory.FIELD_USER_ID);
    // sb.append("='");
    // sb.append(userID);
    // sb.append("' AND ");
    // sb.append(ResultFactory.FIELD_HISTORY_ID);
    // sb.append("=");
    // sb.append(historyID);
    // } else { // no matched history, insert a new one
    // sb.append("INSERT INTO ");
    // sb.append(historyTableName);
    // sb.append(" (");
    // sb.append(ResultFactory.FIELD_USER_ID);
    // sb.append(", ");
    // sb.append(ResultFactory.FIELD_HISTORY_ID);
    // sb.append(", ");
    // sb.append(ResultFactory.FIELD_DATASET_ID);
    // sb.append(") VALUES ('");
    // sb.append(userID);
    // sb.append("', ");
    // sb.append(historyID);
    // sb.append(", ");
    // sb.append(datasetID);
    // sb.append(")");
    // }
    // SqlUtils.closeResultSet(rs);
    // // execute update/insert
    // SqlUtils.executeUpdate(dataSource, sb.toString());
    // } catch (SQLException ex) {
    // logger.error("Got an SQLException: " + ex.toString());
    // throw new WdkUserException(ex.getMessage() + ": " + sb);
    // }
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see java.lang.Object#finalize()
    // */
    // @Override
    // protected void finalize() throws Throwable {
    // // the user's history hasn't been cleared. in this place the user object
    // // removes the query histories
    // if (!cleared) clearUserAnswers();
    // super.finalize();
    // }
    //
    // // ******************************** END
    // ************************************

    User(WdkModel model, int userId, String email) throws WdkUserException,
            WdkModelException {
        this.userId = userId;
        this.email = email;
        this.model = model;
        this.userFactory = model.getUserFactory();
        this.datasetFactory = model.getDatasetFactory();

        userRoles = new LinkedHashSet<String>();

        globalPreferences = new HashMap<String, String>();
        projectPreferences = new HashMap<String, String>();
    }

    public WdkModel getWdkModel() {
        return this.model;
    }

    /**
     * @return Returns the userId.
     */
    public int getUserId() {
        return userId;
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
     * @return Returns the guest.
     * @throws WdkUserException
     */
    public boolean isGuest() throws WdkUserException {
        // update user's time stamp
        userFactory.updateUser(this);
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

    public History createHistory(Answer answer) throws WdkUserException,
            WdkModelException {
        return createHistory(answer, null);
    }

    private History createHistory(Answer answer, String booleanExpression)
            throws WdkUserException, WdkModelException {
        return userFactory.createHistory(this, answer, booleanExpression);
    }

    /**
     * this method is only called by UserFactory during the login process, it
     * merges the existing history of the current guest user into the logged-in
     * user.
     * 
     * @param user
     * @throws WdkUserException
     * @throws WdkModelException
     */
    void mergeUser(User user) throws WdkUserException, WdkModelException {
        // TEST
        logger.debug("Merging user #" + user.getUserId() + " into user #"
                + userId + "...");

        // merge the datasets
        Map<String, String> datasetMap = new LinkedHashMap<String, String>();
        Dataset[] dsets = user.getDatasets();
        for (Dataset dset : dsets) {
            String oldName = dset.getDatasetName();
            String newName = oldName;
            // check the name availability, in the new user domain
            if (!datasetFactory.checkAvailability(this, oldName))
                newName += " #" + dset.getDatasetId();
            dset.setUserId(userId);
            dset.setDatasetName(newName);
            datasetFactory.saveDatasetInfo(dset);
            datasetMap.put(oldName, newName);
        }

        // sort history by id, since the boolean history always has a bigger
        // history id than its compoment histories
        Map<Integer, History> hists = user.getHistoriesMap();
        List<Integer> histIds = new ArrayList<Integer>(hists.keySet());
        Collections.sort(histIds);
        Map<String, String> historyMap = new LinkedHashMap<String, String>();

        // recreate each history in the new user's domain, beware to update
        // the parameter information
        for (int histId : histIds) {
            History hist = hists.get(histId);

            // handle boolean history
            if (hist.isBoolean()) {
                // need to replace the history Ids in the expression
                String expression = hist.getBooleanExpression();
                for (String oldId : historyMap.keySet()) {
                    String newId = historyMap.get(oldId);
                    expression = expression.replaceAll("\\b" + oldId + "\\b",
                            newId);
                }
                History history = combineHistory(expression);
                historyMap.put(Integer.toString(histId),
                        Integer.toString(history.getHistoryId()));
                continue;
            }

            // handle non-boolean history
            Answer answer = hist.getAnswer();
            Question question = answer.getQuestion();
            QueryInstance qinstance = answer.getIdsQueryInstance();
            Param[] params = qinstance.getQuery().getParams();
            Map<String, Object> values = qinstance.getValuesMap();

            // check if the parameter contains DatasetParam
            boolean repack = false;
            for (Param param : params) {
                if (param instanceof DatasetParam) {
                    // get the type of iput data
                    DatasetParam dsParam = (DatasetParam) param;
                    String compound = values.get(dsParam.getName()).toString();
                    InputType inputType = dsParam.getInputType(compound);

                    // get the input value
                    String value = compound.substring(compound.indexOf(':') + 1);
                    if (inputType == InputType.Dataset) {
                        value = datasetMap.get(value);
                    } else if (inputType == InputType.History) {
                        value = historyMap.get(value);
                    }
                    compound = inputType.name() + ":" + value;
                    values.put(param.getName(), compound);
                    repack = true;
                }
            }
            // need to repack the query
            if (repack) {
                int startIndex = answer.getStartRecordInstanceI();
                int endIndex = answer.getEndRecordInstanceI();
                answer = question.makeAnswer(values, startIndex, endIndex);
            }
            History history = createHistory(answer);
            historyMap.put(Integer.toString(histId),
                    Integer.toString(history.getHistoryId()));
        }
    }

    /**
     * get an array of cached histories in the current project site; if the
     * cache is expired. it will be refreshed from the database. The result
     * array is sorted by last_run_time, the lastest at the first
     * 
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public Map<Integer, History> getHistoriesMap() throws WdkUserException,
            WdkModelException {
        return userFactory.loadHistories(this);
    }

    public History[] getHistories() throws WdkUserException, WdkModelException {
        Map<Integer, History> map = userFactory.loadHistories(this);
        History[] array = new History[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Map<String, List<History>> getHistoriesByCategory()
            throws WdkUserException, WdkModelException {
        Map<Integer, History> histories = userFactory.loadHistories(this);
        Map<String, List<History>> category = new LinkedHashMap<String, List<History>>();
        for (History history : histories.values()) {
            String type = history.getDataType();
            List<History> list;
            if (category.containsKey(type)) {
                list = category.get(type);
            } else {
                list = new ArrayList<History>();
                category.put(type, list);
            }
            list.add(history);
        }
        return category;
    }

    /**
     * * The result array is sorted by last_run_time, the lastest at the first
     * 
     * @param dataType
     * @return
     * @throws WdkModelException
     * @throws WdkUserException
     */
    public Map<Integer, History> getHistoriesMap(String dataType)
            throws WdkUserException, WdkModelException {
        Map<Integer, History> histories = userFactory.loadHistories(this);
        Map<Integer, History> selected = new LinkedHashMap<Integer, History>();
        for (int historyId : histories.keySet()) {
            History history = histories.get(historyId);
            if (dataType.equalsIgnoreCase(history.getDataType()))
                selected.put(historyId, history);
        }
        return selected;
    }

    public History[] getHistories(String dataType) throws WdkUserException,
            WdkModelException {
        Map<Integer, History> map = getHistoriesMap(dataType);
        History[] array = new History[map.size()];
        map.values().toArray(array);
        return array;
    }

    /**
     * if the history of the given id doesn't exist, a null is returned
     * 
     * @param historyId
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public History getHistory(int historyId) throws WdkUserException,
            WdkModelException {
        return userFactory.loadHistory(this, historyId);
    }

    public void deleteHistories() throws WdkUserException {
        userFactory.deleteHistories(this);
    }

    public void deleteHistory(int historyId) throws WdkUserException,
            WdkModelException {
        // check the dependencies of the history
        History history = getHistory(historyId);
        if (history.isDepended())
            throw new WdkUserException("The history #" + historyId + " cannot "
                    + "be deleted, since other histories depends on it");
        userFactory.deleteHistory(this, historyId);
    }

    public int getHistoryCount() throws WdkUserException {
        return userFactory.getHistoryCount(this);
    }

    public void setProjectPreference(String prefName, String prefValue) {
        if (prefValue == null) prefValue = prefName;
        projectPreferences.put(prefName, prefValue);
    }

    public void unsetProjectPreference(String prefName) {
        projectPreferences.remove(prefName);
    }

    public Map<String, String> getProjectPreferences() {
        return new HashMap<String, String>(projectPreferences);
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
        return new HashMap<String, String>(globalPreferences);
    }

    public void clearPreferences() {
        globalPreferences.clear();
        projectPreferences.clear();
    }

    public void changePassword(String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException {
        userFactory.changePassword(email, oldPassword, newPassword,
                confirmPassword);
        update();
    }

    public Map<String, Dataset> getDatasetsMap() throws WdkUserException {
        return datasetFactory.loadDatasets(this);
    }

    public Dataset[] getDatasets() throws WdkUserException {
        Map<String, Dataset> datasets = datasetFactory.loadDatasets(this);
        Dataset[] array = new Dataset[datasets.size()];
        datasets.values().toArray(array);
        return array;
    }

    public Map<String, Dataset> getDatasetsMap(String dataType)
            throws WdkUserException {
        Map<String, Dataset> datasets = datasetFactory.loadDatasets(this);
        Map<String, Dataset> selected = new LinkedHashMap<String, Dataset>();
        for (String datasetName : datasets.keySet()) {
            Dataset dataset = datasets.get(datasetName);
            if (dataType.equalsIgnoreCase(dataset.getDataType()))
                selected.put(datasetName, dataset);
        }
        return selected;
    }

    public Dataset[] getDatasets(String dataType) throws WdkUserException {
        Map<String, Dataset> datasets = getDatasetsMap(dataType);
        Dataset[] array = new Dataset[datasets.size()];
        datasets.values().toArray(array);
        return array;
    }

    public Dataset getDataset(String datasetName) throws WdkUserException {
        return datasetFactory.loadDataset(this, datasetName);
    }

    public Dataset createDataset(String datasetName, String dataType,
            String[][] values) throws WdkUserException {
        // the dataset name may be updated to keep the uniqueness constraint
        return datasetFactory.createDataset(this, datasetName, dataType,
                values, false);
    }

    public void deleteDataset(String datasetName) throws WdkUserException {
        datasetFactory.deleteDataset(this, datasetName);
    }

    public void deleteDatasets() throws WdkUserException {
        datasetFactory.deleteDatasets(this);
    }

    public void save() throws WdkUserException {
        userFactory.saveUser(this);
    }

    public void update() throws WdkUserException {
        // update user's time stamp
        userFactory.updateUser(this);
    }

    public int getItemsPerPage() {
        String prefValue = getGlobalPreference(User.PREF_ITEMS_PER_PAGE);
        int itemsPerPage = (prefValue == null) ? 20
                : Integer.parseInt(prefValue);
        return itemsPerPage;
    }

    public History combineHistory(String expression) throws WdkUserException,
            WdkModelException {
        BooleanExpression exp = new BooleanExpression(this);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        Answer answer = root.makeAnswer(0, getItemsPerPage());
        return createHistory(answer, expression);
    }

    public String validateExpression(String expression,
            Map<String, String> operatorMap) throws WdkModelException {
        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(this);
        try {
            be.parseExpression(expression, operatorMap);
        } catch (WdkUserException ue) {
            return ue.getMessage();
        }
        return null;
    }
}
