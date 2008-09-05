/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.BooleanExpression;
import org.gusdb.wdk.model.BooleanQuestionNode;
import org.gusdb.wdk.model.DatasetParam;
import org.gusdb.wdk.model.HistoryParam;
import org.gusdb.wdk.model.Param;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;

/**
 * @author xingao
 * 
 */
public class User /* implements Serializable */{

    /**
     * 
     */
    private static final long serialVersionUID = 6276406938881110742L;

    public final static String PREF_ITEMS_PER_PAGE = "preference_global_items_per_page";
    public final static String PREF_REMOTE_KEY = "preference_remote_key";

    public final static String SORTING_ATTRIBUTES_SUFFIX = "_sort";
    public final static String SUMMARY_ATTRIBUTES_SUFFIX = "_summary";

    public static final int SORTING_LEVEL = 3;

    private Logger logger = Logger.getLogger(User.class);

    private/* transient */WdkModel model;
    private/* transient */UserFactory userFactory;
    private/* transient */DatasetFactory datasetFactory;
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

    private Set<String> userRoles;
    private boolean guest = true;

    /**
     * the preferences for the user: <prefName, prefValue>. It only contains the
     * preferences for the current project
     */
    private Map<String, String> globalPreferences;
    private Map<String, String> projectPreferences;

    // cache the history count in memory
    int historyCount;
    int strategyCount;

    public User() {
        userRoles = new LinkedHashSet<String>();

        globalPreferences = new LinkedHashMap<String, String>();
        projectPreferences = new LinkedHashMap<String, String>();

        historyCount = 0;
	strategyCount = 0;
    }

    User(WdkModel model, int userId, String email, String signature)
            throws WdkUserException {
        this();
        this.userId = userId;
        this.email = email;
        this.signature = signature;

        setWdkModel(model);
    }

    /**
     * The setter is called when the session is restored (deserialized)
     * 
     * @param wdkModel
     * @throws WdkUserException
     */
    public void setWdkModel(WdkModel wdkModel) throws WdkUserException {
        this.model = wdkModel;
        this.userFactory = model.getUserFactory();
        this.datasetFactory = model.getDatasetFactory();
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

    public History createHistory(AnswerValue answer) throws WdkUserException,
            WdkModelException {
        return createHistory(answer, null, false);
    }

    private History createHistory(AnswerValue answer, String booleanExpression,
            boolean deleted) throws WdkUserException, WdkModelException {
        return userFactory.createHistory(this, answer, booleanExpression,
                deleted);
    }

    public Step createStep(AnswerValue result)
	throws WdkUserException, WdkModelException {
	return createStep(result, null, false);
    }
    
    public Step createStep(AnswerValue result, String booleanExpression,
				       boolean deleted)
	throws WdkUserException, WdkModelException {
	return userFactory.createStep(this, result, booleanExpression, deleted);
    }

    public Strategy createStrategy(Step answer, boolean saved) 
	throws WdkUserException, WdkModelException {
        Date now = new Date();
	String name = answer.getAnswer().getAnswerValue().getQuestion().getRecordClass().getType() + " Strategy for ";
	name += getFirstName();
	if (!isGuest()) {
	    name += " " + getLastName();
	}
	name += now;
	return createStrategy(answer, name, saved);
    }

    public Strategy createStrategy(Step answer, String name, boolean saved)
	throws WdkUserException, WdkModelException {
	return userFactory.createStrategy(this, answer, name, saved);
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

        // merge histories
        // a history can be merged only if its all components have been merged
        Map<Integer, Integer> historyMap = new LinkedHashMap<Integer, Integer>();
        Map<Integer, History> histories = user.getHistoriesMap();
        while (!histories.isEmpty()) {
            // for each round, only merge the histories that have no components,
            // or all of its components have been merged
            Map<Integer, History> pendings = new LinkedHashMap<Integer, History>();
            for (History history : histories.values()) {
                Set<Integer> components = history.getComponentHistories();

                if (components.isEmpty()) {
                    // no components, can merge
                    History newHistory = createHistory(history.getAnswerValue(),
                            null, history.isDeleted());
                    newHistory.setCustomName(history.getBaseCustomName());
                    newHistory.update();
                    historyMap.put(history.getHistoryId(),
                            newHistory.getHistoryId());

                    logger.info("Merging history #" + history.getHistoryId()
                            + " -> #" + newHistory.getHistoryId());

                    continue;
                }

                // histories with components, the components need ed to be
                // merged first
                boolean canMerge = true;
                for (Integer compId : components) {
                    if (!historyMap.containsKey(compId)) {
                        // still have components not merged
                        canMerge = false;
                        break;
                    }
                }
                if (!canMerge) {
                    pendings.put(history.getHistoryId(), history);
                    continue;
                }

                StringBuffer sbLog = new StringBuffer();
                sbLog.append("History #" + history.getHistoryId()
                        + " has components: ");
                for (int compId : components) {
                    sbLog.append(compId + ", ");
                }
                logger.info(sbLog);

                // can merge, needs to repack the param values
                History newHistory;
                if (history.isBoolean()) {
                    // merge boolean history
                    String expression = history.getBooleanExpression();
                    for (Integer compId : components) {
                        Integer newId = historyMap.get(compId);
                        expression = expression.replaceAll("\\b"
                                + compId.toString() + "\\b", "WDK"
                                + newId.toString() + "WDK");
                    }
                    expression = expression.replaceAll("WDK", "");
                    newHistory = combineHistory(expression, history.isDeleted());
                } else {
                    // merge histories with DatasetParam/HistoryParam
                    AnswerValue answer = history.getAnswerValue();
                    Question question = answer.getQuestion();
                    int startIndex = answer.getStartRecordInstanceI();
                    int endIndex = answer.getEndRecordInstanceI();
                    Param[] params = question.getParams();
                    Map<String, Object> values = answer.getParams();
                    for (Param param : params) {
                        if (param instanceof HistoryParam) {
                            String compound = values.get(param.getName()).toString();
                            // two parts: user_signature, history_id
                            String parts[] = compound.split(":");
                            int histId = Integer.parseInt(parts[1].trim());
                            Integer newId = historyMap.get(histId);
                            // replace the signature with current user's
                            String newValue = this.signature + ":" + newId;
                            values.put(param.getName(), newValue);
                        } else if (param instanceof DatasetParam) {
                            // merge dataset, by creating new datasets with the
                            // previous values
                            String compound = values.get(param.getName()).toString();
                            // two parts: user_signature, dataset_id
                            String parts[] = compound.split(":");
                            String datasetChecksum = parts[1].trim();

                            // now make new dataset for the new user
                            String newValue = this.signature + ":"
                                    + datasetChecksum;
                            values.put(param.getName(), newValue);
                        }
                    }
                    answer = question.makeAnswerValue(values, startIndex, endIndex);
                    newHistory = createHistory(answer, null,
                            history.isDeleted());
                }
                newHistory.setCustomName(history.getBaseCustomName());
                newHistory.setDeleted(history.isDeleted());
                newHistory.update();
                historyMap.put(history.getHistoryId(),
                        newHistory.getHistoryId());
            }
            histories = pendings;
        }
        // TEST
        StringBuffer sb = new StringBuffer("The history Mapping: ");
        for (int histId : historyMap.keySet()) {
            sb.append("(" + histId + "-" + historyMap.get(histId) + ") ");
        }
        logger.info(sb.toString().trim());
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
        Map<Integer, History> invalidHistories = new LinkedHashMap<Integer, History>();
        Map<Integer, History> histories = userFactory.loadHistories(this,
                invalidHistories);

        // update the history count
        historyCount = 0;
        for (History history : histories.values()) {
            if (!history.isDeleted()) historyCount++;
        }
        return histories;
    }

    public Map<Integer, Step> getStepsMap()
	throws WdkUserException, WdkModelException {
	Map<Integer, Step> invalidSteps = new LinkedHashMap<Integer, Step>();
	Map<Integer, Step> userAnswers = userFactory.loadSteps(this, invalidSteps);

	// update the user answer count?  we need to have one first

	return userAnswers;
    }

    public Map<Integer, Strategy> getStrategiesMap()
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> invalidStrategies = new LinkedHashMap<Integer, Strategy>();
	Map<Integer, Strategy> strategies = userFactory.loadStrategies(this, invalidStrategies);
	
	strategyCount = 0;
	for (Strategy strategy : strategies.values()) {
	    strategyCount++;
	}
	return strategies;
    }

    public History[] getInvalidHistories() throws WdkUserException,
            WdkModelException {
        Map<Integer, History> histories = new LinkedHashMap<Integer, History>();
        userFactory.loadHistories(this, histories);

        History[] array = new History[histories.size()];
        histories.values().toArray(array);
        return array;
    }

    public History[] getHistories() throws WdkUserException, WdkModelException {
        Map<Integer, History> map = getHistoriesMap();
        History[] array = new History[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Map<String, List<History>> getHistoriesByCategory()
            throws WdkUserException, WdkModelException {
        Map<Integer, History> histories = getHistoriesMap();
        Map<String, List<History>> category = new LinkedHashMap<String, List<History>>();
        for (History history : histories.values()) {
            // not include the histories marked as 'deleted'
            if (history.isDeleted()) continue;

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

    public Strategy[] getInvalidStrategies()
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> strategies = new LinkedHashMap<Integer, Strategy>();
	userFactory.loadStrategies(this, strategies);

	Strategy[] array = new Strategy[strategies.size()];
	strategies.values().toArray(array);
	return array;
    }

    public Strategy[] getStrategies()
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> map = getStrategiesMap();
	Strategy[] array = new Strategy[map.size()];
	map.values().toArray(array);
	return array;
    }

    public Map<String, List<Strategy>> getStrategiesByCategory()
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> strategies = getStrategiesMap();
	Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
	for (Strategy strategy : strategies.values()) {
	    String type = strategy.getDataType();
	    List<Strategy> list;
	    if (category.containsKey(type)) {
		list = category.get(type);
	    }
	    else {
		list = new ArrayList<Strategy>();
		category.put(type, list);
	    }
	    list.add(strategy);
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
        Map<Integer, History> histories = getHistoriesMap();
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

    public Map<Integer, Step> getStepsMap(String dataType)
	throws WdkUserException, WdkModelException {
	Map<Integer, Step> userAnswers = getStepsMap();
	Map<Integer, Step> selected = new LinkedHashMap<Integer, Step>();
	for (int userAnswerId : userAnswers.keySet()) {
	    Step userAnswer = userAnswers.get(userAnswerId);
	    if (dataType.equalsIgnoreCase(userAnswer.getDataType()))
		selected.put(userAnswerId, userAnswer);
	}
	return selected;
    }

    public Step[] getSteps(String dataType)
	throws WdkUserException, WdkModelException {
	Map<Integer, Step> map = getStepsMap(dataType);
	Step[] array = new Step[map.size()];
	map.values().toArray(array);
	return array;
    }

    public Map<Integer, Strategy> getStrategiesMap(String dataType)
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> strategies = getStrategiesMap();
	Map<Integer, Strategy> selected = new LinkedHashMap<Integer, Strategy>();
	for (int strategyId : strategies.keySet()) {
	    Strategy strategy = strategies.get(strategyId);
	    if (dataType.equalsIgnoreCase(strategy.getDataType()))
		selected.put(strategyId, strategy);
	}
	return selected;
    }

    public Strategy[] getStrategies(String dataType)
	throws WdkUserException, WdkModelException {
	Map<Integer, Strategy> map = getStrategiesMap(dataType);
	Strategy[] array = new Strategy[map.size()];
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

    public Step getStep(int userAnswerId)
	throws WdkUserException, WdkModelException {
	return userFactory.loadStep(this, userAnswerId);
    }

    public Strategy getStrategy(int userStrategyId)
	throws WdkUserException, WdkModelException {
	return userFactory.loadStrategy(this, userStrategyId);
    }

    public void deleteHistories() throws WdkUserException {
        userFactory.deleteHistories(this, false);
    }

    public void deleteHistories(boolean allProjects) throws WdkUserException {
        userFactory.deleteHistories(this, allProjects);
    }

    public void deleteSteps()
	throws WdkUserException {
	userFactory.deleteSteps(this, false);
    }

    public void deleteSteps(boolean allProjects)
	throws WdkUserException {
	userFactory.deleteSteps(this, allProjects);
    }

    public void deleteInvalidHistories() throws WdkUserException,
            WdkModelException {
        userFactory.deleteInvalidHistories(this);
    }

    public void deleteInvalidSteps()
	throws WdkUserException, WdkModelException {
	userFactory.deleteInvalidSteps(this);
    }

    public void deleteHistory(int historyId) throws WdkUserException,
            WdkModelException {
        // check the dependencies of the history
        History history = getHistory(historyId);
        if (history.isDepended()) {
            // the history is depended by other nodes, mark it as delete, but
            // don't really delete it from the database
            history.setDeleted(true);
            history.update(false);

            // TEST
            logger.info("History #" + historyId + " of user " + email
                    + " is depended by other histories. Marked as deleted.");
        } else {
            // delete the history from the database
            userFactory.deleteHistory(this, historyId);
        }
        // decrement the history count
        historyCount--;
    }

    public void deleteStep(int userAnswerId)
	throws WdkUserException, WdkModelException {
	Step userAnswer = getStep(userAnswerId);
	// Need to check if we can actually delete from DB or not
	// For now, this will just hide the Step
	userAnswer.setDeleted(true);
	userAnswer.update(false);

	// need a user answer count?
    }

    public void deleteStrategy(int strategyId)
	throws WdkUserException, WdkModelException {
	userFactory.deleteStrategy(this, strategyId);
	strategyCount--;
    }

    public int getHistoryCount() throws WdkUserException {
        return historyCount;
    }

    public int getStrategyCount() throws WdkUserException {
        return strategyCount;
    }

    public void setStrategyCount(int strategyCount) {
	this.strategyCount = strategyCount;
    }

    /**
     * @param historyCount
     *            The historyCount to set.
     */
    void setHistoryCount(int historyCount) {
        this.historyCount = historyCount;
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

    public void changePassword(String oldPassword, String newPassword,
            String confirmPassword) throws WdkUserException {
        userFactory.changePassword(email, oldPassword, newPassword,
                confirmPassword);
    }

    DatasetFactory getDatasetFactory() {
        return datasetFactory;
    }

    public Dataset getDataset(String datasetChecksum) throws WdkUserException {
        Dataset dataset = datasetFactory.getDataset(this, datasetChecksum);
        if (dataset == null)
            throw new WdkUserException("Dataset of the checksum "
                    + datasetChecksum + " cannot be found");
        logger.info("dataset #" + dataset.getDatasetId()
                + " is uploaded from: " + dataset.getUploadFile());
        return dataset;
    }

    public Dataset getDataset(int datasetId) throws WdkUserException {
        Dataset dataset = datasetFactory.getDataset(this, datasetId);
        if (dataset == null)
            throw new WdkUserException("Dataset #" + datasetId
                    + " cannot be found");
        logger.info("dataset #" + datasetId + " is uploaded from: "
                + dataset.getUploadFile());
        return dataset;
    }

    public Dataset createDataset(String uploadFile, String[] values)
            throws WdkUserException, WdkModelException {
        return datasetFactory.makeDataset(this, uploadFile, values);
    }

    public void save() throws WdkUserException {
        userFactory.saveUser(this);
    }

    public int getItemsPerPage() {
        String prefValue = getGlobalPreference(User.PREF_ITEMS_PER_PAGE);
        int itemsPerPage = (prefValue == null) ? 20
                : Integer.parseInt(prefValue);
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) throws WdkUserException {
        if (itemsPerPage <= 0) itemsPerPage = 20;
        else if (itemsPerPage > 100) itemsPerPage = 100;
        setGlobalPreference(User.PREF_ITEMS_PER_PAGE,
                Integer.toString(itemsPerPage));
        save();
    }

    public History combineHistory(String expression) throws WdkUserException,
            WdkModelException {
        return combineHistory(expression, false);
    }

    public Step combineStep(String expression)
	throws WdkUserException, WdkModelException {
	return combineStep(expression, false);
    }

    public void updateHistory(History history, String expression)
            throws WdkUserException, WdkModelException {
        AnswerValue answer = parseExpression(expression);
        history.setAnswerValue(answer);
        history.setBooleanExpression(expression);
        userFactory.updateHistory(history, expression);
    }


    private History combineHistory(String expression, boolean deleted)
            throws WdkUserException, WdkModelException {
        AnswerValue answer = parseExpression(expression);
        return createHistory(answer, expression, false);
    }

    private Step combineStep(String expression, boolean deleted)
	throws WdkUserException, WdkModelException {
	AnswerValue answer = parseExpression(expression);
	return createStep(answer, expression, false);
    }

    private AnswerValue parseExpression(String expression) throws WdkUserException,
            WdkModelException {
        logger.debug("Boolean expression: " + expression);
        BooleanExpression exp = new BooleanExpression(this);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        AnswerValue answer = root.makeAnswerValue(1, getItemsPerPage());

        logger.debug("Boolean answer: " + answer.getResultSize());

        // save summary list, if not summary list exists
        String summaryKey = answer.getQuestion().getFullName()
                + SUMMARY_ATTRIBUTES_SUFFIX;
        if (!projectPreferences.containsKey(summaryKey)) {
            Map<String, AttributeField> summary = answer.getSummaryAttributes();
            StringBuffer sb = new StringBuffer();
            for (String attrName : summary.keySet()) {
                if (sb.length() != 0) sb.append(",");
                sb.append(attrName);
            }
            projectPreferences.put(summaryKey, sb.toString());
            save();
        }
        return answer;
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

    public Map<String, Boolean> getSortingAttributes(String questionFullName)
            throws WdkUserException, WdkModelException {
        String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX;
        String sortingChecksum = projectPreferences.get(sortKey);
        Map<String, Boolean> sortingAttributes = getSortingAttributesByChecksum(sortingChecksum);
        if (sortingAttributes != null) return sortingAttributes;

        if (questionFullName.equals(BooleanQuestionNode.BOOLEAN_QUESTION_NAME)) {
            // boolean question has no sorting attributes by default
            return new LinkedHashMap<String, Boolean>();
        } else { // ordinary question
            Question question = model.getQuestion(questionFullName);
            return question.getDefaultSortingAttributes();
        }
    }

    public Map<String, Boolean> getSortingAttributesByChecksum(
            String sortingChecksum) throws WdkUserException {
        if (sortingChecksum == null) return null;
        QueryFactory queryFactory = model.getQueryFactory();
        return queryFactory.getSortingAttributes(sortingChecksum);
    }

    public String addSortingAttribute(String questionFullName, String attrName,
            boolean ascending) throws WdkUserException, WdkModelException {
        Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>();
        sortingMap.put(attrName, ascending);
        Map<String, Boolean> previousMap = getSortingAttributes(questionFullName);
        for (String aName : previousMap.keySet()) {
            if (!sortingMap.containsKey(aName))
                sortingMap.put(aName, previousMap.get(aName));
        }

        // save and get sorting checksum
        QueryFactory queryFactory = model.getQueryFactory();
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
            throws WdkUserException, WdkModelException {
        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        String summaryChecksum = projectPreferences.get(summaryKey);
        String[] summary = getSummaryAttributesByChecksum(summaryChecksum);
        if (summary != null) return summary;

        if (questionFullName.equals(BooleanQuestionNode.BOOLEAN_QUESTION_NAME)) {
            summary = new String[0];
        } else { // ordinary question
            Question question = model.getQuestion(questionFullName);
            Map<String, AttributeField> attributes = question.getSummaryAttributes();
            summary = new String[attributes.size()];
            attributes.keySet().toArray(summary);
        }
        return summary;
    }

    public String[] getSummaryAttributesByChecksum(String summaryChecksum)
            throws WdkUserException {
        if (summaryChecksum == null) return null;
        // get summary list
        QueryFactory queryFactory = model.getQueryFactory();
        return queryFactory.getSummaryAttributes(summaryChecksum);
    }

    public String addSummaryAttribute(String questionFullName, String attrName)
            throws WdkUserException, WdkModelException {
        Set<String> summaryAttributes = new LinkedHashSet<String>();
        String[] summary = getSummaryAttributes(questionFullName);
        for (String attributeName : summary) {
            summaryAttributes.add(attributeName);
        }
        summaryAttributes.add(attrName);

        // save the summary attribute list
        String[] attributes = new String[summaryAttributes.size()];
        summaryAttributes.toArray(attributes);

        return applySummaryChecksum(questionFullName, attributes);
    }

    public String removeSummaryAttribute(String questionFullName,
            String attrName) throws WdkUserException, WdkModelException {
        Set<String> summaryAttributes = new LinkedHashSet<String>();
        String[] summary = getSummaryAttributes(questionFullName);
        for (String attributeName : summary) {
            if (!attributeName.equals(attrName))
                summaryAttributes.add(attributeName);
        }

        // save the summary attribute list
        String[] attributes = new String[summaryAttributes.size()];
        summaryAttributes.toArray(attributes);

        return applySummaryChecksum(questionFullName, attributes);
    }

    public void resetSummaryAttribute(String questionFullName) {
        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        projectPreferences.remove(summaryKey);
    }

    public String arrangeSummaryAttribute(String questionFullName,
            String attrName, boolean moveLeft) throws WdkUserException,
            WdkModelException {
        String[] summary = getSummaryAttributes(questionFullName);

        // TEST
        StringBuffer theSb = new StringBuffer();
        for (String name : summary) {
            theSb.append(name + ", ");
        }
        logger.info("Summary before: " + theSb.toString());

        for (int i = 0; i < summary.length; i++) {
            if (attrName.equals(summary[i])) {
                if (moveLeft && i > 0) {
                    summary[i] = summary[i - 1];
                    summary[i - 1] = attrName;
                } else if (!moveLeft && i < summary.length - 1) {
                    summary[i] = summary[i + 1];
                    summary[i + 1] = attrName;
                }
                break;
            }
        }

        // TEST
        theSb = new StringBuffer();
        for (String name : summary) {
            theSb.append(name + ", ");
        }
        logger.info("Summary after: " + theSb.toString());

        return applySummaryChecksum(questionFullName, summary);
    }

    /**
     * The method replace the previous checksum with the given one.
     * 
     * @param summaryChecksum
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public String applySummaryChecksum(String questionFullName,
            String[] attributes) throws WdkModelException, WdkUserException {
        QueryFactory queryFactory = model.getQueryFactory();
        String summaryChecksum = queryFactory.makeSummaryChecksum(attributes);

        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        projectPreferences.put(summaryKey, summaryChecksum);
        return summaryChecksum;
    }

    public String createRemoteKey() throws WdkUserException {
        // user can remote key only if he/she is logged in
        if (isGuest())
            throw new WdkUserException("Guest user cannot create remote key.");

        // the key is a combination of user id and current time
        Date now = new Date();

        String key = Long.toString(now.getTime()) + "->"
                + Integer.toString(userId);
        try {
            key = userFactory.encrypt(key);
        } catch (NoSuchAlgorithmException ex) {
            throw new WdkUserException(ex);
        }
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
}
