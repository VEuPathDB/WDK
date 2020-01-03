package org.gusdb.wdk.model.user;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.wdk.model.WdkModel;

/**
 * Represents a WDK user.
 * 
 * @see UnregisteredUser for subclass representing guest user
 * @see RegisteredUser for subclass representing registered user
 * 
 * @author rdoherty
 */
public abstract class User {

  protected WdkModel _wdkModel;

  protected long _userId;
  protected String _email;
  protected String _signature;
  protected String _stableId;

  // Holds key/value pairs associated with the user's profile (come from account db)
  protected Map<String,String> _properties = new HashMap<>();

  // Holds key/value pairs associated with the user for this project (come from user db)
  protected UserPreferences _preferences;

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

  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  @Override
  public String toString() {
    return "User #" + getUserId() + " - " + getEmail();
  }

  @Override
  public int hashCode() {
    return (getUserId() + getEmail() + getSignature() + getStableId()).hashCode();
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

}
