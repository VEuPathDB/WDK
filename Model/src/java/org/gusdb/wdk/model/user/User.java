/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.Collections;
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

    User(WdkModel model, int userId, String email) throws WdkUserException,
            WdkModelException {
        this.userId = userId;
        this.email = email;
        this.model = model;
        this.userFactory = model.getUserFactory();
        this.datasetFactory = model.getDatasetFactory();

        userRoles = new LinkedHashSet<String>();

        globalPreferences = new LinkedHashMap<String, String>();
        projectPreferences = new LinkedHashMap<String, String>();
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
        userFactory.deleteHistories(this, false);
    }

    public void deleteHistories(boolean allProjects) throws WdkUserException {
        userFactory.deleteHistories(this, allProjects);
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
        } else {
            // delete the history from the database
            userFactory.deleteHistory(this, historyId);
        }
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

    public void setItemsPerPage(int itemsPerPage) throws WdkUserException {
        if (itemsPerPage <= 0) itemsPerPage = 20;
        else if (itemsPerPage > 100) itemsPerPage = 100;
        setGlobalPreference(User.PREF_ITEMS_PER_PAGE,
                Integer.toString(itemsPerPage));
        save();
    }

    public History combineHistory(String expression) throws WdkUserException,
            WdkModelException {
        BooleanExpression exp = new BooleanExpression(this);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        Answer answer = root.makeAnswer(1, getItemsPerPage());
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
