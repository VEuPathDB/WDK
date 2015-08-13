import {
  PREFERENCES_LOADED,
  PREFERENCE_SET,
  PREFERENCE_REMOVED,
  PREFERENCE_REMOVED_ALL
} from '../constants/actionTypes';

let prefix = 'wdk_preference_';

function loadPreferences() {
  let preferences = {};
  for (let storageKey in localStorage) {
    if (storageKey.startsWith(prefix)) {
      let key = storageKey.replace(prefix, '');
      try {
        preferences[key] = JSON.parse(localStorage.getItem(storageKey));
      }
      catch (error) {
        console.warn(
          'Could not load the "%s" preference from localStorage using the ' +
            'storage key "%s".',
          key,
          storageKey,
          error
        );
      }
    }
  }
  return { type: PREFERENCES_LOADED, preferences };
}

function setPreference(key, value) {
  let storageKey = makeStorageKey(key);
  localStorage.setItem(storageKey, JSON.stringify(value));
  return { type: PREFERENCE_SET, key, value };
}

function removePreference(key) {
  let storageKey = makeStorageKey(key);
  localStorage.removeItem(storageKey);
  return { type: PREFERENCE_REMOVED, key };
}

function removeAllPreferences() {
  for (let key in localStorage) {
    if (key.startsWith(prefix))
      localStorage.removeItem(key);
  }
  return { type: PREFERENCE_REMOVED_ALL };
}

function makeStorageKey(key) {
  return prefix + key;
}

export default {
  loadPreferences,
  setPreference,
  removePreference,
  removeAllPreferences
};
