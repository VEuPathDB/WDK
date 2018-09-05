/* global wdk, wdkConfig */
import { ActionCreators } from 'wdk-client';
import { getContext } from './clientAdapter';

const { UserActionCreators } = ActionCreators;

// FIXME Review module
// Some redundant functions, some undefined functions called, etc.

wdk.namespace("window.wdk.user", function(ns, $) {
  "use strict";

  let { guestUser, wdkUser } = wdkConfig;

  ns.id = function() { return wdkUser.id; };
  ns.name = function() { return wdkUser.name; };
  ns.country = function() { return wdkUser.country; };
  ns.email = function() { return wdkUser.email; };
  ns.isGuest = function() { return guestUser; };
  ns.isUserLoggedIn = function() { return !ns.isGuest(); };

  ns.login = function(action, destination = window.location.href) {
    getContext().then(context => {
      context.dispatchAction(action
        ? UserActionCreators.showLoginWarning(action, destination)
        : UserActionCreators.showLoginForm(destination));
    });
  };

  ns.logout = function() {
    getContext().then(context => {
      context.dispatchAction(UserActionCreators.showLogoutWarning());
    });
  };

  // User preferences
  // ----------------

  // Preference store - can be localStorage OR cookie
  var hasLocalStorage = Boolean(window.localStorage);
  var sessionId = $.cookie('JSESSIONID');

  // Returns the value for key, defaultValue if it doesn't exist
  ns.getPreference = function getPreference(key, defaultValue) {
    if (hasLocalStorage) {
      var item;
      try { item = JSON.parse(localStorage.getItem(key)); }
      catch(e) { /* ignore error */ }
      if (item) {
        if (!item.session || item.session === sessionId) {
          return item.value;
        }
      }
      return defaultValue;
    }
    return $.cookie(key);
  };

  // Set the value of a preference.
  // Defaults to indefinite storage (1 year is using cookie fallback).
  //
  // - key: {String} Preference name
  // - value: {Any} Preference value
  // - session [optional]: {Boolean} Is preference session-only
  ns.setPreference = function setPreference(key, value, session) {
    if (hasLocalStorage) {
      localStorage.setItem(key, JSON.stringify({
        value: value,
        session: !!session && sessionId
      }));
    }
    else {
      $.cookie(key, value, session ? undefined : { expires: 365 });
    }
    return value;
  };

});
