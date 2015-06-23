import React from 'react';
import Store from '../core/store';
import {
  LoadPreferences,
  SetPreference,
  RemovePreference,
  RemoveAllPreferences
} from '../ActionType';

let $update = React.addons.update;

function createStore({ dispatcher }) {
  let state = { preferences: {} };
  return new Store(dispatcher, state, update);
}

function update(state, action) {
  switch(action.type) {
    case LoadPreferences: return load(state, action);
    case SetPreference: return set(state, action);
    case RemovePreference: return remove(state, action);
    case RemoveAllPreferences: return removeAll(state, action);
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
