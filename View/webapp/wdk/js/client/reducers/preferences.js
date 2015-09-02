import {
  PREFERENCES_LOADED,
  PREFERENCE_SET,
  PREFERENCE_REMOVED,
  PREFERENCE_REMOVED_ALL
} from '../constants/actionTypes';

export default function preferences(preferences = {}, action) {
  switch(action.type) {
    case PREFERENCES_LOADED: return load(preferences, action);
    case PREFERENCE_SET: return set(preferences, action);
    case PREFERENCE_REMOVED: return remove(preferences, action);
    case PREFERENCE_REMOVED_ALL: return removeAll(preferences, action);
    default: return preferences;
  }
}

function load(preferences, action) {
  return Object.assign({}, preferences, {
    preferences: action.preferences
  });
}

function set(preferences, action) {
  let { key, value } = action;
  return Object.assign({}, preferences, {
    [key]: value
  });
}

function remove(preferences, action) {
  let { key } = action;
  return Object.assign({}, preferences, {
    [key]: undefined
  });
}

function removeAll(preferences) {
  return {};
}
