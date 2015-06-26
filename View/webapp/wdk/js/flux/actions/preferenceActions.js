import curry from 'lodash/function/curry';
import mapValues from 'lodash/object/mapValues';
import {
  LoadPreferences,
  SetPreference,
  RemovePreference,
  RemoveAllPreferences
} from '../ActionType';

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
  return LoadPreferences({ preferences });
}

function setPreference(key, value) {
  let storageKey = makeStorageKey(key);
  localStorage.setItem(storageKey, JSON.stringify(value));
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
    };
  });
});

let createActions = makeActionCreators({
  loadPreferences,
  setPreference,
  removePreference,
  removeAllPreferences
});

export default {
  createActions
};
