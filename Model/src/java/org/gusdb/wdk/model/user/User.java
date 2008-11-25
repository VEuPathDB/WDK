/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.AnswerFilterInstance;
import org.gusdb.wdk.model.AnswerValue;
import org.gusdb.wdk.model.AttributeField;
import org.gusdb.wdk.model.Question;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.json.JSONException;

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

    private Set<String> userRoles;
    private boolean guest = true;

    /**
     * the preferences for the user: <prefName, prefValue>. It only contains the
     * preferences for the current project
     */
    private Map<String, String> globalPreferences;
    private Map<String, String> projectPreferences;

    // cache the history count in memory
    private int stepCount;
    private int strategyCount;

    // keep track of user's open strategies; don't serialize
    private transient ArrayList<Integer> activeStrategies;

    User(WdkModel model, int userId, String email, String signature)
            throws WdkUserException {
        strategyCount = 0;
        this.userId = userId;
        this.email = email;
        this.signature = signature;

        userRoles = new LinkedHashSet<String>();

        globalPreferences = new LinkedHashMap<String, String>();
        projectPreferences = new LinkedHashMap<String, String>();

        stepCount = 0;

        setWdkModel(model);
    }

    /**
     * The setter is called when the session is restored (deserialized)
     * 
     * @param wdkModel
     * @throws WdkUserException
     */
    public void setWdkModel(WdkModel wdkModel) throws WdkUserException {
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

    public Step createStep(Question question, Map<String, String> paramValues,
            String filterName) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        AnswerFilterInstance filter = null;
        if (filterName != null) {
            RecordClass recordClass = question.getRecordClass();
            filter = recordClass.getFilter(filterName);
        }
        return createStep(question, paramValues, filter);
    }

    public Step createStep(Question question, Map<String, String> paramValues,
            AnswerFilterInstance filter) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        return stepFactory.createStep(this, question, paramValues, filter);
    }

    public Step createStep(Question question, Map<String, String> paramValues,
            AnswerFilterInstance filter, int pageStart, int pageEnd,
            boolean deleted) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        return stepFactory.createStep(this, question, paramValues, filter,
                pageStart, pageEnd, deleted);
    }

    public Strategy createStrategy(Step step, boolean saved)
            throws WdkUserException, WdkModelException, SQLException,
            JSONException {
        return createStrategy(step, null, saved);
    }

    public Strategy createStrategy(Step step, String name, boolean saved)
            throws WdkUserException, WdkModelException, SQLException,
            JSONException {
        return stepFactory.createStrategy(this, step, name, saved);
    }

    /**
     * this method is only called by UserFactory during the login process, it
     * merges the existing history of the current guest user into the logged-in
     * user.
     * 
     * @param user
     * @throws WdkUserException
     * @throws WdkModelException
     * @throws JSONException
     * @throws SQLException
     * @throws NoSuchAlgorithmException
     */
    void mergeUser(User user) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        // TEST
        logger.debug("Merging user #" + user.getUserId() + " into user #"
                + userId + "...");

        // first of all we import all the strategies
        Set<Integer> importedSteps = new LinkedHashSet<Integer>();
        Map<Integer, Integer> strategiesMap = new LinkedHashMap<Integer, Integer>();
        for (Strategy strategy : user.getStrategies()) {
            // the root step is considered as imported
            Step rootStep = strategy.getLatestStep();

            // import the strategy
            Strategy newStrategy = this.importStrategy(strategy);

            importedSteps.add(rootStep.getDisplayId());
            strategiesMap.put(strategy.getStrategyId(),
                    newStrategy.getStrategyId());
        }

        // update list of active strategies so ids are correct for logged in
        // user
        ArrayList<Integer> oldActiveStrategies = user.getActiveStrategies();
        for (Integer strategyId : oldActiveStrategies) {
            this.activeStrategies.add(strategiesMap.get(strategyId));
        }

        // then import the steps that do not belong to any strategies; that is,
        // only the root steps who are not imported yet.
        for (Step step : user.getSteps()) {
            if (stepFactory.isStepDepended(user, step.getDisplayId()))
                continue;

            stepFactory.importStep(this, step);
        }
    }

    public Map<Integer, Step> getStepsMap() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        Map<Integer, Step> invalidSteps = new LinkedHashMap<Integer, Step>();
        Map<Integer, Step> userAnswers = stepFactory.loadSteps(this,
                invalidSteps);

        return userAnswers;
    }

    public Map<Integer, Strategy> getStrategiesMap() throws WdkUserException,
            WdkModelException, JSONException, SQLException {
        Map<Integer, Strategy> invalidStrategies = new LinkedHashMap<Integer, Strategy>();
        Map<Integer, Strategy> strategies = stepFactory.loadStrategies(this,
                invalidStrategies);

        strategyCount = strategies.size();
        return strategies;
    }

    public Map<String, List<Step>> getStepsByCategory()
            throws WdkUserException, WdkModelException, SQLException,
            JSONException, NoSuchAlgorithmException {
        Map<Integer, Step> steps = getStepsMap();
        Map<String, List<Step>> category = new LinkedHashMap<String, List<Step>>();
        for (Step step : steps.values()) {
            // not include the histories marked as 'deleted'
            if (step.isDeleted()) continue;

            String type = step.getType();
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

    public Strategy[] getInvalidStrategies() throws WdkUserException,
            WdkModelException, JSONException, SQLException {
        try {
            Map<Integer, Strategy> strategies = new LinkedHashMap<Integer, Strategy>();
            stepFactory.loadStrategies(this, strategies);

            Strategy[] array = new Strategy[strategies.size()];
            strategies.values().toArray(array);
            return array;
        } catch (WdkUserException ex) {
            System.out.println(ex);
            throw ex;
        } catch (WdkModelException ex) {
            System.out.println(ex);
            throw ex;
        }
    }

    public Strategy[] getStrategies() throws WdkUserException,
            WdkModelException, JSONException, SQLException {
        Map<Integer, Strategy> map = getStrategiesMap();
        Strategy[] array = new Strategy[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Map<String, List<Strategy>> getStrategiesByCategory()
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Map<Integer, Strategy> strategies = getStrategiesMap();
        Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
        for (Strategy strategy : strategies.values()) {
            String type = strategy.getType();
            List<Strategy> list;
            if (category.containsKey(type)) {
                list = category.get(type);
            } else {
                list = new ArrayList<Strategy>();
                category.put(type, list);
            }
            list.add(strategy);
        }
        return category;
    }

    public Map<String, List<Strategy>> getUnsavedStrategiesByCategory()
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Map<Integer, Strategy> strategies = getStrategiesMap();
        Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
        for (Strategy strategy : strategies.values()) {
            if (!strategy.getIsSaved()) {
                String type = strategy.getType();
                List<Strategy> list;
                if (category.containsKey(type)) {
                    list = category.get(type);
                } else {
                    list = new ArrayList<Strategy>();
                    category.put(type, list);
                }
                list.add(strategy);
            }
        }
        return category;
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
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Map<Integer, Strategy> strategies = getStrategiesMap();
        Map<String, List<Strategy>> category = new LinkedHashMap<String, List<Strategy>>();
        for (Strategy strategy : strategies.values()) {
            if (strategy.getIsSaved()) {
                String type = strategy.getType();
                List<Strategy> list;
                if (category.containsKey(type)) {
                    list = category.get(type);
                } else {
                    list = new ArrayList<Strategy>();
                    category.put(type, list);
                }
                list.add(strategy);
            }
        }
        return category;
    }

    public Map<Integer, Step> getStepsMap(String dataType)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, JSONException, SQLException {
        Map<Integer, Step> steps = getStepsMap();
        Map<Integer, Step> selected = new LinkedHashMap<Integer, Step>();
        for (int stepDisplayId : steps.keySet()) {
            Step step = steps.get(stepDisplayId);
            if (dataType.equalsIgnoreCase(step.getType()))
                selected.put(stepDisplayId, step);
        }
        return selected;
    }

    public Step[] getSteps(String dataType) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            SQLException {
        Map<Integer, Step> map = getStepsMap(dataType);
        Step[] array = new Step[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Step[] getSteps() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Map<Integer, Step> map = getStepsMap();
        Step[] array = new Step[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Step[] getInvalidSteps() throws WdkUserException, WdkModelException,
            SQLException, JSONException {
        Map<Integer, Step> steps = new LinkedHashMap<Integer, Step>();
        stepFactory.loadSteps(this, steps);

        Step[] array = new Step[steps.size()];
        steps.values().toArray(array);
        return array;
    }

    public Map<Integer, Strategy> getStrategiesMap(String dataType)
            throws WdkUserException, WdkModelException, JSONException,
            SQLException, NoSuchAlgorithmException {
        Map<Integer, Strategy> strategies = getStrategiesMap();
        Map<Integer, Strategy> selected = new LinkedHashMap<Integer, Strategy>();
        for (int strategyId : strategies.keySet()) {
            Strategy strategy = strategies.get(strategyId);
            if (dataType.equalsIgnoreCase(strategy.getType()))
                selected.put(strategyId, strategy);
        }
        return selected;
    }

    public Strategy[] getStrategies(String dataType) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, JSONException,
            SQLException {
        Map<Integer, Strategy> map = getStrategiesMap(dataType);
        Strategy[] array = new Strategy[map.size()];
        map.values().toArray(array);
        return array;
    }

    public Step getStep(int displayId) throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        return stepFactory.loadStep(this, displayId);
    }

    public Strategy getStrategy(int userStrategyId) throws WdkUserException,
            WdkModelException, JSONException, SQLException {
        return stepFactory.loadStrategy(this, userStrategyId);
    }

    public void deleteSteps() throws WdkUserException, SQLException {
        deleteSteps(false);
    }

    public void deleteSteps(boolean allProjects) throws WdkUserException,
            SQLException {
        stepFactory.deleteSteps(this, allProjects);
        stepCount = 0;
    }

    public void deleteInvalidSteps() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        stepFactory.deleteInvalidSteps(this);
    }

    public void deleteInvalidStrategies() throws WdkUserException,
            WdkModelException, SQLException, JSONException {
        stepFactory.deleteInvalidStrategies(this);
    }

    public void deleteStep(int displayId) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        stepFactory.deleteStep(this, displayId);
        // decrement the history count
        stepCount--;
    }

    public void deleteStrategy(int strategyId) throws WdkUserException,
            WdkModelException, SQLException {
        stepFactory.deleteStrategy(this, strategyId);
        strategyCount--;
    }

    public void deleteStrategies() throws SQLException {
        deleteStrategies(false);
    }

    public void deleteStrategies(boolean allProjects) throws SQLException {
        stepFactory.deleteStrategies(this, allProjects);
        strategyCount = 0;
    }

    public int getStepCount() throws WdkUserException {
        return stepCount;
    }

    public int getStrategyCount() throws WdkUserException {
        return strategyCount;
    }

    public void setStrategyCount(int strategyCount) {
        this.strategyCount = strategyCount;
    }

    /**
     * @param stepCount
     *            The stepCount to set.
     */
    void setStepCount(int stepCount) {
        this.stepCount = stepCount;
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

    public Dataset getDataset(String datasetChecksum) throws WdkUserException,
            SQLException, WdkModelException {
        return datasetFactory.getDataset(this, datasetChecksum);
    }

    public Dataset getDataset(int userDatasetId) throws SQLException,
            WdkModelException {
        return datasetFactory.getDataset(this, userDatasetId);
    }

    public Dataset createDataset(String uploadFile, String[] values)
            throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException {
        return datasetFactory.getDataset(this, uploadFile, values);
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

    public void updateStep(Step step, String expression,
            boolean useBooleanFilter) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        // get a new hidden step, in order to get the new answer
        Step newStep = combineStep(expression, useBooleanFilter, true);
        step.setAnswer(newStep.getAnswer());
        stepFactory.deleteStep(this, newStep.getDisplayId());
        stepFactory.updateStep(this, step, true);
    }

    public Step combineStep(String expression) throws WdkUserException,
            WdkModelException, NoSuchAlgorithmException, SQLException,
            JSONException {
        return combineStep(expression, false, false);
    }

    public Step combineStep(String expression, boolean useBooleanFilter,
            boolean deleted) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException, SQLException, JSONException {
        logger.debug("Boolean expression: " + expression);
        BooleanExpression exp = new BooleanExpression(this);
        Step step = exp.parseExpression(expression, useBooleanFilter);
        AnswerValue answerValue = step.getAnswer().getAnswerValue();

        logger.debug("Boolean answer size: " + answerValue.getResultSize());

        // save summary list, if no summary list exists
        String summaryKey = answerValue.getQuestion().getFullName()
                + SUMMARY_ATTRIBUTES_SUFFIX;
        if (!projectPreferences.containsKey(summaryKey)) {
            Map<String, AttributeField> summary = answerValue.getSummaryAttributeFields();
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

    public void validateExpression(String expression) throws WdkModelException,
            NoSuchAlgorithmException, WdkUserException, SQLException,
            JSONException {
        // construct BooleanQuestionNode
        BooleanExpression be = new BooleanExpression(this);
        be.parseExpression(expression, false);
    }

    public Map<String, Boolean> getSortingAttributes(String questionFullName)
            throws WdkUserException, WdkModelException {
        String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX;
        String sortingChecksum = projectPreferences.get(sortKey);
        if (sortingChecksum == null) return null;

        QueryFactory queryFactory = wdkModel.getQueryFactory();
        Map<String, Boolean> sortingAttributes = queryFactory.getSortingAttributes(sortingChecksum);
        if (sortingAttributes != null) return sortingAttributes;

        // user doesn't have preference, use the default one of the question
        Question question = wdkModel.getQuestion(questionFullName);
        return question.getSortingAttributeMap();
    }

    public Map<String, Boolean> getSortingAttributesByChecksum(
            String sortingChecksum) throws WdkUserException {
        if (sortingChecksum == null) return null;
        QueryFactory queryFactory = wdkModel.getQueryFactory();
        return queryFactory.getSortingAttributes(sortingChecksum);
    }

    public String addSortingAttribute(String questionFullName, String attrName,
            boolean ascending) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException {
        Map<String, Boolean> sortingMap = new LinkedHashMap<String, Boolean>();
        sortingMap.put(attrName, ascending);
        Map<String, Boolean> previousMap = getSortingAttributes(questionFullName);
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
            throws WdkUserException, WdkModelException {
        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        String summaryChecksum = projectPreferences.get(summaryKey);
        if (summaryChecksum == null) return null;

        // get summary list
        QueryFactory queryFactory = wdkModel.getQueryFactory();
        String[] summary = queryFactory.getSummaryAttributes(summaryChecksum);
        if (summary != null) return summary;

        // user does't have preference, use the default of the question
        Question question = wdkModel.getQuestion(questionFullName);
        Map<String, AttributeField> attributes = question.getSummaryAttributeFieldMap();
        summary = new String[attributes.size()];
        attributes.keySet().toArray(summary);
        return summary;
    }

    public void resetSummaryAttributes(String questionFullName) {
        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        projectPreferences.remove(summaryKey);
    }

    public String setSummaryAttributes(String questionFullName,
            String[] summaryNames) throws WdkUserException, WdkModelException,
            NoSuchAlgorithmException {
        // make sure all the attribute names exist
        Question question = (Question) wdkModel.resolveReference(questionFullName);
        Map<String, AttributeField> attributes = question.getAttributeFieldMap();
        for (String summaryName : summaryNames) {
            if (!attributes.containsKey(summaryName))
                throw new WdkModelException("Invalid summary attribute ["
                        + summaryName + "] for question [" + questionFullName
                        + "]");
        }

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
            String summaryChecksum) throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException {
        String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX;
        projectPreferences.put(summaryKey, summaryChecksum);
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

    public Strategy importStrategy(String strategyKey)
            throws NoSuchAlgorithmException, WdkModelException,
            WdkUserException, SQLException, JSONException {
        String[] parts = strategyKey.split(":");
        String userSignature = parts[0];
        int displayId = Integer.parseInt(parts[1]);
        User user = userFactory.getUser(userSignature);
        Strategy oldStrategy = user.getStrategy(displayId);
        return importStrategy(oldStrategy);
    }

    public Strategy importStrategy(Strategy oldStrategy)
            throws WdkModelException, WdkUserException,
            NoSuchAlgorithmException, SQLException, JSONException {
        Strategy newStrategy = stepFactory.importStrategy(this, oldStrategy);
        newStrategy.setSavedName(oldStrategy.getSavedName());
        newStrategy.setIsSaved(oldStrategy.getIsSaved());
        newStrategy.update(true);
        return newStrategy;
    }

    public ArrayList<Integer> getActiveStrategies() {
        return activeStrategies;
    }

    public void setActiveStrategies(ArrayList<Integer> activeStrategies) {
        this.activeStrategies = activeStrategies;
    }

    public boolean checkNameExists(Strategy strategy, String name)
            throws SQLException {
        return stepFactory.checkNameExists(strategy, name);
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

}
