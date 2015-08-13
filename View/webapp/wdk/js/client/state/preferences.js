import React from 'react';
import {
  PREFERENCES_LOADED,
  PREFERENCE_SET,
  PREFERENCE_REMOVED,
  PREFERENCE_REMOVED_ALL
} from '../constants/actionTypes';

let $update = React.addons.update;

function update(preferences = {}, action) {
  switch(action.type) {
    case PREFERENCES_LOADED: return load(preferences, action);
    case PREFERENCE_SET: return set(preferences, action);
    case PREFERENCE_REMOVED: return remove(preferences, action);
    case PREFERENCE_REMOVED_ALL: return removeAll(preferences, action);
    default: return preferences;
  }
}

function load(preferences, action) {
  return $update(preferences, { $set: action.preferences } );
}

function set(preferences, action) {
  let { key, value } = action;
  return $update(preferences, { $merge: { [key]: value } } );
}

function remove(preferences, action) {
  let { key } = action;
  return $update(preferences, { $merge: { [key]: undefined } } );
}

function removeAll(preferences) {
  return $update(preferences, { $set: {} } );
}

export default {
  update
};
