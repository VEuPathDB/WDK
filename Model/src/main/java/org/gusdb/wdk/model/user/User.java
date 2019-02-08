package org.gusdb.wdk.model.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.FunctionWithException;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * Represents a WDK user.
 * 
 * @see UnregisteredUser for subclass representing guest user
 * @see RegisteredUser for subclass representing registered user
 * 
 * @author rdoherty
 */
public abstract class User {

  private static final Logger LOG = Logger.getLogger(User.class);

  protected WdkModel _wdkModel;

  protected long _userId;
  protected String _email;
  protected String _signature;
  protected String _stableId;

  // Holds key/value pairs associated with the user's profile (come from account db)
  protected Map<String,String> _properties = new HashMap<>();

  // Holds key/value pairs associated with the user for this project (come from user db)
  protected UserPreferences _preferences;

  // Holds current session data for this user.  TODO: factor out
  protected UserSession _session;

  /**
   * Temporarily provide display name value until Struts Actions are purged.
   * After that, client will determine what to display
   * TODO: remove once struts is purged
   * 
   * @return display name for this user
   */
  public abstract String getDisplayName();

  /**
   * Tells whether this user is a guest
   * 
   * @return true if guest, else false
   */
  public abstract boolean isGuest();

  protected User(WdkModel wdkModel, long userId, String email, String signature, String stableId) {
    _wdkModel = wdkModel;
    _userId = userId;
    _email = email;
    _signature = signature;
    _stableId = stableId;
    _preferences = new UserPreferences(this);
    _session = new UserSession(this);
  }

  public long getUserId() {
    return _userId;
  }

  public String getEmail() {
    return _email;
  }

  public String getSignature() {
    return _signature;
  }

  public String getStableId() {
    return _stableId;
  }

  public void setEmail(String email) {
    _email = email;
  }

  /**
   * Sets the value of the profile property given by the UserProfileProperty enum
   * @param key
   * @param value
   */
  public void setProfileProperty(String key, String value) {
    _properties.put(key, value);
  }

  public void setProfileProperties(Map<String, String> properties) {
    _properties = properties;
  }

  /**
   * Return the entire user profile property map
   * @return
   */
  public Map<String, String> getProfileProperties() {
    return _properties;
  }

  /**
   * Removes all existing user profile properties
   */
  public void clearProfileProperties() {
    _properties.clear();
  }

  public void setPreferences(UserPreferences preferences) {
    _preferences = preferences;
  }

  public UserPreferences getPreferences() {
    return _preferences;
  }

  public UserSession getSession() {
    return _session;
  }

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  @Override
  public String toString() {
    return "User #" + getUserId() + " - " + getEmail();
  }

  @Override
  public int hashCode() {
    return (int)getUserId();
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof User)) {
      return false;
    }
    User other = (User)obj;
    return (
        getUserId() == other.getUserId() &&
        getEmail().equals(other.getEmail()) &&
        getSignature().equals(other.getSignature()) &&
        getStableId().equals(other.getStableId())
    );
  }

/**
 * This is a deprecated "trait" interface containing default methods previously
 * supplied by UserBean.  It is necessary since we now expose the User class
 * to JSPs but its getters are limited in scope (callers in Java land should be
 * calling the appropriate factory methods to get a user's data e.g. favorites).
 * 
 * Thus this is (hopefully) a temporary interface and can be removed when the
 * strategy-loading branch is merged back and we say good-bye to JSPs/beans.
 */

  public String getFirstName() {
    return getProfileProperties().get("firstName");
  }

  public String getMiddleName() {
    return getProfileProperties().get("middleName");
  }

  public String getLastName() {
    return getProfileProperties().get("lastName");
  }

  public String getOrganization() {
    return getProfileProperties().get("organization");
  }

  public String getFrontAction() {
    return getSession().getFrontAction();
  }

  public Long getFrontStrategy() {
    return getSession().getFrontStrategy();
  }

  public Long getFrontStep() {
    return getSession().getFrontStep();
  }

  public Map<String, String> getGlobalPreferences() {
    return getPreferences().getGlobalPreferences();
  }

  public Map<String, String> getProjectPreferences() {
    return getPreferences().getProjectPreferences();
  }

  public int getStrategyCount() throws WdkModelException {
    return getWdkModel().getStepFactory().getStrategyCount(getUserId());
  }

  public int getPublicCount() throws WdkModelException {
    int count = getWdkModel().getStepFactory().getPublicStrategyCount();
    LOG.debug("Found number of public strats: " + count);
    return count;
  }

  public int getItemsPerPage() {
    return getPreferences().getItemsPerPage();
  }

  public Map<String, List<Strategy>> getSavedStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getSavedStrategiesByCategory(this);
    logFoundStrategies(strategies, "saved");
    return convertMap(strategies);
  }

  public Map<String, List<Strategy>> getUnsavedStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getUnsavedStrategiesByCategory(this);
    logFoundStrategies(strategies, "unsaved");
    return convertMap(strategies);
  }

  public void logFoundStrategies(Map<String, List<Strategy>> strategies, String condition) {
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loaded map of " + strategies.size() + " " + condition + " strategy categories:");
      int total = 0;
      for (Entry<String, List<Strategy>> entry : strategies.entrySet()) {
        LOG.debug("   " + entry.getKey() + ": " + entry.getValue().size() + " strategies.");
        total += entry.getValue().size();
      }
      LOG.debug("   Total: " + total);
    }
  }

  public Map<String, List<Strategy>> getRecentStrategiesByCategory() throws WdkModelException {
    Map<String, List<Strategy>> strategies = StepUtilities.getRecentStrategiesByCategory(this);
    return convertMap(strategies);
  }

  public Map<String, List<Strategy>> getActiveStrategiesByCategory() {
    Map<String, List<Strategy>> strategies = StepUtilities.getActiveStrategiesByCategory(this);
    return convertMap(strategies);
  }

  /**
   * @return { category/(type name)->{ activity->strategyBean } }
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public Map<String, Map<String, List<Strategy>>> getStrategiesByCategoryActivity()
      throws WdkModelException, WdkUserException {
    Map<String, List<Strategy>> activeStrats = getActiveStrategiesByCategory();
    Map<String, List<Strategy>> savedStrats = getSavedStrategiesByCategory();
    Map<String, List<Strategy>> recentStrats = getRecentStrategiesByCategory();
    Map<String, Map<String, List<Strategy>>> categories = new LinkedHashMap<>();
    WdkModel wdkModel = getWdkModel();

    for (String rcName : activeStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClassByName(rcName).orElse(null);
      if (recordClass == null)
        continue;
      String category = recordClass.getDisplayName();
      List<Strategy> strategies = activeStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<Strategy>> activities = new LinkedHashMap<>();
      activities.put("Opened", strategies);
      categories.put(category, activities);
    }

    for (String rcName : savedStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClassByName(rcName).orElse(null);
      if (recordClass == null)
        continue;
      String category = recordClass.getDisplayName();
      List<Strategy> strategies = savedStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<Strategy>> activities = categories.get(category);
      if (activities == null) {
        activities = new LinkedHashMap<>();
        categories.put(category, activities);
      }
      activities.put("Saved", strategies);
    }

    for (String rcName : recentStrats.keySet()) {
      RecordClass recordClass = wdkModel.getRecordClassByName(rcName).orElse(null);
      if (recordClass == null)
        continue;
      String category = recordClass.getDisplayName();
      List<Strategy> strategies = recentStrats.get(rcName);
      if (strategies.size() == 0)
        continue;

      Map<String, List<Strategy>> activities = categories.get(category);
      if (activities == null) {
        activities = new LinkedHashMap<>();
        categories.put(category, activities);
      }
      activities.put("Recent", strategies);
    }
    return categories;
  }

  public String getViewStrategyId() {
    return getSession().getViewStrategyKey();
  }

  public long getViewStepId() {
    return getSession().getViewStepId();
  }

  public Integer getViewPagerOffset() {
    return getSession().getViewPagerOffset();
  }

  public Strategy[] getActiveStrategies() {
    List<Strategy> strategies = new ArrayList<>();
    for (Strategy strategy : getSession().getActiveStrategies()) {
      strategies.add(strategy);
    }
    Strategy[] array = new Strategy[strategies.size()];
    strategies.toArray(array);
    return array;
  }

  public long[] getActiveStrategyIds() {
    return getSession().getActiveStrategyIds();
  }

  public Map<RecordClass, Integer> getBasketCounts() throws WdkModelException {
    Map<RecordClass, Integer> counts = getWdkModel().getBasketFactory().getBasketCounts(this);
    Map<RecordClass, Integer> beans = new LinkedHashMap<>();
    for (RecordClass recordClass : counts.keySet()) {
      int count = counts.get(recordClass);
      beans.put(recordClass, count);
    }
    return beans;
  }

  public int getBasketCount() throws WdkModelException {
    Map<RecordClass, Integer> baskets = getWdkModel().getBasketFactory().getBasketCounts(this);
    int total = 0;
    for (int count : baskets.values()) {
      total += count;
    }
    return total;
  }

  @Deprecated
  public Map<RecordClass, List<Favorite>> getFavorites() throws WdkModelException {
    Map<RecordClass, List<Favorite>> favorites = getWdkModel().getFavoriteFactory().getFavorites(this);
    Map<RecordClass, List<Favorite>> beans = new LinkedHashMap<>();
    for (RecordClass recordClass : favorites.keySet()) {
      List<Favorite> beanList = new ArrayList<>();
      List<Favorite> list = favorites.get(recordClass);
      for (Favorite favorite : list) {
        beanList.add(favorite);
      }
      beans.put(recordClass, beanList);
    }
    return beans;
  }

  @Deprecated
  public String[] getFavoriteGroups() throws WdkModelException {
    return getWdkModel().getFavoriteFactory().getGroups(this);
  }

  static <T> Map<String, T> exposeAsMap(FunctionWithException<String,T> getter) {
    return new HashMap<String, T>() {
      @Override
      public T get(Object objectName) {
        try {
          return getter.apply((String)objectName);
        }
        catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  static Map<String, List<Strategy>> convertMap(Map<String, List<Strategy>> strategies) {
    Map<String, List<Strategy>> category = new LinkedHashMap<>();
    for (String type : strategies.keySet()) {
      List<Strategy> list = strategies.get(type);
      List<Strategy> beans = new ArrayList<>();
      for (Strategy strategy : list) {
        beans.add(strategy);
      }
      category.put(type, beans);
    }
    return category;
  }
}
