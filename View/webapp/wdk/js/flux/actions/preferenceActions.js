import curry from 'lodash/function/curry';
import mapValues from 'lodash/object/mapValues';
import {
  SetPreference,
  RemovePreference,
  RemoveAllPreferences
} from '../ActionType';

let prefix = 'wdk_preference_';

function setPreference(key, value) {
  let storageKey = makeStorageKey(key);
  localStorage.setItem(storageKey, value);
  return SetPreference({ key, value });
}

function removePreference(key) {
  let storageKey = makeStorageKey(key);
  localStorage.removeItem(storageKey);
  return RemovePreference({ key });
}

function removeAllPreferences() {
  for (let key in localStorage) {
    if (key.startsWith(prefix))
      localStorage.removeItem(key);
  }
  return RemoveAllPreferences();
}

function makeStorageKey(key) {
  return prefix + key;
}

// TODO Put in utils and invert control so that the main function uses this.
// Eventually, action creators can be defined as a module with functions that
// return an action to be dispatched. We will be hiding the dispatcher from all
// domain specific code, including stores.
let makeActionCreators = curry(function makeActionCreators(module, context) {
  return mapValues(module, function(func) {
    return function actionCreator(...args) {
      context.dispatcher.dispatch(func(...args));
    }
  });
});

let createActions = makeActionCreators({
  setPreference,
  removePreference,
  removeAllPreferences
});

export default {
  createActions
};
