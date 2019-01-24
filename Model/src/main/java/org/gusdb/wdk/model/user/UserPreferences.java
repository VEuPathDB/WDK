package org.gusdb.wdk.model.user;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.SummaryView;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordView;
import org.gusdb.wdk.model.record.attribute.AttributeField;

public class UserPreferences {

  // represents the maximum number of sorts we apply to an answer
  public static final int MAX_NUM_SORTING_COLUMNS = 3;

  public final static String PREF_ITEMS_PER_PAGE = "preference_global_items_per_page";

  public final static String SORTING_ATTRIBUTES_SUFFIX = "_sort";
  public final static String SUMMARY_ATTRIBUTES_SUFFIX = "_summary";

  public final static String SUMMARY_VIEW_PREFIX = "summary_view_";
  public final static String RECORD_VIEW_PREFIX = "record_view_";

  public final static String DEFAULT_SUMMARY_VIEW_PREF_SUFFIX = "";

  private final static int PREF_VALUE_LENGTH = 4000;

  private final User _user;
  private final WdkModel _wdkModel;

  /**
   * the preferences for the user: <prefName, prefValue>. It only contains the preferences for the current
   * project
   */
  private final Map<String, String> _globalPreferences;
  private final Map<String, String> _projectPreferences;

  public UserPreferences(User user) {
    _user = user;
    _wdkModel = user.getWdkModel();
    _globalPreferences = new LinkedHashMap<String, String>();
    _projectPreferences = new LinkedHashMap<String, String>();
  }

  public void setProjectPreference(String prefName, String prefValue) {
    if (prefValue == null)
      prefValue = prefName; // acting as a flag
    _projectPreferences.put(prefName, prefValue);
  }

  public String getProjectPreference(String key) {
    return _projectPreferences.get(key);
  }

  public void unsetProjectPreference(String prefName) {
    _projectPreferences.remove(prefName);
  }

  public void setProjectPreferences(Map<String, String> projectPreferences) {
    assignPrefs(projectPreferences, _projectPreferences);
  }

  public Map<String, String> getProjectPreferences() {
    return new LinkedHashMap<String, String>(_projectPreferences);
  }

  public void clearProjectPreferences() {
    _projectPreferences.clear();
  }

  public void setGlobalPreference(String prefName, String prefValue) {
    if (prefValue == null)
      prefValue = prefName; // acting as a flag
    _globalPreferences.put(prefName, prefValue);
  }

  public String getGlobalPreference(String key) {
    return _globalPreferences.get(key);
  }

  public void unsetGlobalPreference(String prefName) {
    _globalPreferences.remove(prefName);
  }

  public void setGlobalPreferences(Map<String, String> globalPreferences) {
    assignPrefs(globalPreferences, _globalPreferences);
  }

  public Map<String, String> getGlobalPreferences() {
    return new LinkedHashMap<String, String>(_globalPreferences);
  }

  public void clearGlobalPreferences() {
    _globalPreferences.clear();
  }

  public void clearPreferences() {
    _globalPreferences.clear();
    _projectPreferences.clear();
  }

  private static void assignPrefs(Map<String,String> from, Map<String,String> to) {
    to.clear();
    for (Entry<String,String> pref : from.entrySet()) {
      to.put(pref.getKey(), pref.getValue());
    }
  }

  //*******************************************************************************
  //*** Methods to persist preferences to the DB; TODO: make more efficient
  //*******************************************************************************

  private void saveProjectPreference(String key, String value) throws WdkModelException {
    setProjectPreference(key, value);
    save();
  }

  private void deleteAndSaveProjectPreference(String key) throws WdkModelException {
    unsetProjectPreference(key);
    save();
  }

  private void save() throws WdkModelException {
    _wdkModel.getUserFactory().savePreferences(_user);
  }

  //*******************************************************************************
  //*** Methods to support specific WDK preferences; prefs are saved automatically
  //*******************************************************************************

  public int getItemsPerPage() {
    String prefValue = getProjectPreference(PREF_ITEMS_PER_PAGE);
    int itemsPerPage = (prefValue == null) ? 20 : Integer.parseInt(prefValue);
    return itemsPerPage;
  }

  public void setItemsPerPage(int itemsPerPage) throws WdkModelException {
    if (itemsPerPage <= 0)
      itemsPerPage = 20;
    else if (itemsPerPage > 1000)
      itemsPerPage = 1000;
    saveProjectPreference(PREF_ITEMS_PER_PAGE, Integer.toString(itemsPerPage));
  }

  public Map<String, Boolean> getSortingAttributes(
      String questionFullName, String keySuffix) throws WdkModelException {
    Question question = _wdkModel.getQuestionOrFail(questionFullName);
    Map<String, AttributeField> attributes = question.getAttributeFieldMap();

    String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX + keySuffix;
    String sortingList = _projectPreferences.get(sortKey);

    // user doesn't have sorting preference, return the default from question.
    if (sortingList == null)
      return question.getSortingAttributeMap();

    // convert the list into a map.
    Map<String, Boolean> sortingAttributes = new LinkedHashMap<>();
    for (String clause : sortingList.split(",")) {
      String[] sort = clause.trim().split("\\s+", 2);

      // ignore the invalid sorting attribute
      String attrName = sort[0];
      if (!attributes.containsKey(attrName))
        continue;

      boolean order = (sort.length == 1 || sort[1].equalsIgnoreCase("ASC"));
      sortingAttributes.put(sort[0], order);
    }
    return sortingAttributes;
  }

  public String addSortingAttribute(String questionFullName, String attrName,
      boolean ascending, String keySuffix) throws WdkModelException {
    // make sure the attribute exists in the question
    Question question = _wdkModel.getQuestionOrFail(questionFullName);
    if (!question.getAttributeFieldMap().containsKey(attrName))
      throw new WdkModelException("Cannot sort by attribute '" + attrName +
          "' since it doesn't belong the question " + questionFullName);

    StringBuilder sort = new StringBuilder(attrName);
    sort.append(ascending ? " ASC" : " DESC");

    Map<String, Boolean> previousMap = getSortingAttributes(questionFullName, keySuffix);
    if (previousMap != null) {
      int count = 1;
      for (String name : previousMap.keySet()) {
        if (name.equals(attrName))
          continue;
        if (count >= Utilities.SORTING_LEVEL)
          break;
        sort.append(",").append(name).append(previousMap.get(name) ? " ASC" : " DESC");
      }
    }

    String sortKey = questionFullName + SORTING_ATTRIBUTES_SUFFIX + keySuffix;
    String sortValue = sort.toString();
    saveProjectPreference(sortKey, sortValue);
    return sortValue;
  }

  public void setSortingAttributes(String questionName, String sortings, String keySuffix) throws WdkModelException {
    String sortKey = questionName + SORTING_ATTRIBUTES_SUFFIX + keySuffix;
    saveProjectPreference(sortKey, sortings);
  }

  public String[] getSummaryAttributes(String questionFullName, String keySuffix) throws WdkModelException {
    Question question = _wdkModel.getQuestionOrFail(questionFullName);
    Map<String, AttributeField> attributes = question.getAttributeFieldMap();

    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX + keySuffix;
    String summaryValue = _projectPreferences.get(summaryKey);
    Set<String> summary = new LinkedHashSet<>();
    if (summaryValue != null) {
      for (String attrName : summaryValue.split(",")) {
        attrName = attrName.trim();
        // ignore invalid attribute names;
        if (attributes.containsKey(attrName) && !summary.contains(attrName)) {
          summary.add(attrName);
        }
      }
    }
    if (summary.isEmpty()) {
      return question.getSummaryAttributeFieldMap().keySet().toArray(new String[0]);
    }
    else {
      return summary.toArray(new String[0]);
    }
  }

  public void resetSummaryAttributes(String questionFullName, String keySuffix) throws WdkModelException {
    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX + keySuffix;
    deleteAndSaveProjectPreference(summaryKey);
  }

  public String setSummaryAttributes(String questionFullName, String[] summaryNames, String keySuffix) throws WdkModelException {
    // make sure all the attribute names exist
    Question question = (Question) _wdkModel.resolveReference(questionFullName);
    Map<String, AttributeField> attributes = question.getAttributeFieldMap();

    StringBuilder summary = new StringBuilder();
    for (String attrName : summaryNames) {
      // ignore invalid attribute names
      if (!attributes.keySet().contains(attrName))
        continue;

      // exit if we have too many attributes
      if (summary.length() + attrName.length() + 1 >= PREF_VALUE_LENGTH)
        break;

      if (summary.length() > 0)
        summary.append(",");
      summary.append(attrName);
    }

    String summaryKey = questionFullName + SUMMARY_ATTRIBUTES_SUFFIX + keySuffix;
    String summaryValue = summary.toString();
    saveProjectPreference(summaryKey, summaryValue);
    return summaryValue;
  }

  public SummaryView getCurrentSummaryView(Question question) {
    String key = SUMMARY_VIEW_PREFIX + question.getFullName(); //+ question.getRecordClassName();
    String viewName = _projectPreferences.get(key);
    SummaryView view;
    if (viewName == null) { // no summary view set, use the default one
      view = question.getDefaultSummaryView();
    }
    else {
      try {
        view = question.getSummaryView(viewName);
      }
      catch (WdkUserException e) {
        // stored user preference is no longer valid; choose default instead
        view = question.getDefaultSummaryView();
      }
    }
    return view;
  }

  public void setCurrentSummaryView(Question question, SummaryView summaryView) throws WdkModelException {
    String key = SUMMARY_VIEW_PREFIX + question.getFullName(); //+ question.getRecordClassName();
    if (summaryView == null) { // remove the current summary view
      deleteAndSaveProjectPreference(key);
    }
    else { // store the current summary view
      String viewName = summaryView.getName();
      saveProjectPreference(key, viewName);
    }
  }

  public RecordView getCurrentRecordView(String rcName) throws WdkUserException, WdkModelException {
    return getCurrentRecordView(_wdkModel.getRecordClass(rcName));
  }

  public RecordView getCurrentRecordView(RecordClass recordClass) throws WdkUserException {
    String key = RECORD_VIEW_PREFIX + recordClass.getFullName();
    String viewName = _projectPreferences.get(key);
    RecordView view;
    if (viewName == null) { // no record view set, use the default one
      view = recordClass.getDefaultRecordView();
    }
    else {
      view = recordClass.getRecordView(viewName);
    }
    return view;
  }

  public void setCurrentRecordView(RecordClass recordClass, RecordView recordView) throws WdkModelException {
    String key = RECORD_VIEW_PREFIX + recordClass.getFullName();
    if (recordView == null) { // remove the current record view
      deleteAndSaveProjectPreference(key);
    }
    else { // store the current record view
      String viewName = recordView.getName();
      saveProjectPreference(key, viewName);
    }
  }
}
