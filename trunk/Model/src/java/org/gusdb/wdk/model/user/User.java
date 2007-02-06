/**
 * 
 */
package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.*;

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

    User(WdkModel model, int userId, String email, String signature)
            throws WdkUserException, WdkModelException {
        this.userId = userId;
        this.email = email;
        this.signature = signature;
        this.model = model;
        this.userFactory = model.getUserFactory();
        this.datasetFactory = model.getDatasetFactory();

        userRoles = new LinkedHashSet<String>();

        globalPreferences = new LinkedHashMap<String, String>();
        projectPreferences = new LinkedHashMap<String, String>();

        historyCount = 0;
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

    public History createHistory(Answer answer) throws WdkUserException,
            WdkModelException {
        return createHistory(answer, null, false);
    }

    private History createHistory(Answer answer, String booleanExpression,
            boolean deleted) throws WdkUserException, WdkModelException {
        return userFactory.createHistory(this, answer, booleanExpression,
                deleted);
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
                    History newHistory = createHistory(history.getAnswer(),
                            null, history.isDeleted());
                    newHistory.setCustomName(history.getBaseCustomName());
                    newHistory.update();
                    historyMap.put(history.getHistoryId(),
                            newHistory.getHistoryId());
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

                // can merge, needs to repack the param values
                History newHistory;
                if (history.isBoolean()) {
                    // merge boolean history
                    String expression = history.getBooleanExpression();
                    for (Integer compId : components) {
                        Integer newId = historyMap.get(compId);
                        expression = expression.replaceAll("\\b"
                                + compId.toString() + "\\b", newId.toString());
                    }
                    newHistory = combineHistory(expression, history.isDeleted());
                } else {
                    // merge histories with DatasetParam/HistoryParam
                    Answer answer = history.getAnswer();
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
                            int datasetId = Integer.parseInt(parts[1].trim());
                            Dataset dataset = user.getDataset(datasetId);
                            String[] data = dataset.getValues();

                            // now make new dataset for the new user
                            Dataset newDataset = this.createDataset(
                                    dataset.getUploadFile(), data);
                            String newValue = this.signature + ":"
                                    + newDataset.getDatasetId();
                            values.put(param.getName(), newValue);
                        }
                    }
                    answer = question.makeAnswer(values, startIndex, endIndex);
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
        Map<Integer, History> histories = userFactory.loadHistories(this);

        // update the history count
        historyCount = 0;
        for (History history : histories.values()) {
            if (!history.isDeleted()) historyCount++;
        }
        return histories;
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

    public int getHistoryCount() throws WdkUserException {
        return historyCount;
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

    public Dataset getDataset(int datasetId) throws WdkUserException {
        return datasetFactory.getDataset(this, datasetId);
    }

    public Dataset createDataset(String uploadFile, String[] values)
            throws WdkUserException {
        // summary will be the first three items of the values
        StringBuffer sb = new StringBuffer();
        int bound = Math.min(3, values.length);
        for (int i = 0; i < bound; i++) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(values[i]);
        }
        if (values.length > 3) sb.append(" ...");
        return datasetFactory.createDataset(this, uploadFile, sb.toString(),
                values);
    }

    public void deleteDataset(int datasetId) throws WdkUserException {
        datasetFactory.deleteDataset(this, datasetId);
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

    private History combineHistory(String expression, boolean deleted)
            throws WdkUserException, WdkModelException {
        BooleanExpression exp = new BooleanExpression(this);
        Map<String, String> operatorMap = getWdkModel().getBooleanOperators();
        BooleanQuestionNode root = exp.parseExpression(expression, operatorMap);

        Answer answer = root.makeAnswer(1, getItemsPerPage());
        return createHistory(answer, expression, false);
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
