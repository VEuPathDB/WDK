import React from 'react';
import Store from '../core/store';
import {
  PREFERENCES_LOADED,
  PREFERENCE_SET,
  PREFERENCE_REMOVED,
  PREFERENCE_REMOVED_ALL
} from '../constants/actionTypes';

let $update = React.addons.update;

function createStore({ dispatcher }) {
  let state = { preferences: {} };
  return new Store(dispatcher, state, update);
}

function update(state, action) {
  switch(action.type) {
    case PREFERENCES_LOADED: return load(state, action);
    case PREFERENCE_SET: return set(state, action);
    case PREFERENCE_REMOVED: return remove(state, action);
    case PREFERENCE_REMOVED_ALL: return removeAll(state, action);
  }
}

function load(state, action) {
  return $update(state, {
    preferences: { $set: action.preferences }
  });
}

function set(state, action) {
  let { key, value } = action;
  return $update(state, {
    preferences: { $merge: { [key]: value } }
  });
}

function remove(state, action) {
  let { key } = action;
  return $update(state, {
    preferences: { $merge: { [key]: undefined } }
  });
}

function removeAll(state) {
  return $update(state, {
    preferences: { $set: {} }
  });
}

export default {
  createStore
};
