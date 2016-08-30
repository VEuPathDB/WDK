/* global wdk, wdkConfig */
import * as AuthUtil from './client/utils/AuthUtil';
import { confirm } from './client/utils/Platform';

// FIXME Review module
// Some redundant functions, some undefined functions called, etc.

wdk.namespace("window.wdk.user", function(ns, $) {
  "use strict";

  let { auth, guestUser, webappUrl, wdkUser } = wdkConfig;

  let authUtilConfig = {
    webappUrl,
    serviceUrl: webappUrl + '/service',
    method: auth.method,
    oauthUrl: auth.oauthUrl,
    oauthClientId: auth.oauthClientId
  };

  ns.id = function() { return wdkUser.id; };
  ns.name = function() { return wdkUser.name; };
  ns.country = function() { return wdkUser.country; };
  ns.email = function() { return wdkUser.email; };
  ns.isGuest = function() { return guestUser; };
  ns.isUserLoggedIn = function() { return !ns.isGuest(); };

  ns.login = function(action, destination = window.location.href) {
    if (action) {
      let message = `To ${action}, you must be logged in. Would you like to login now?`;
      confirm('Login required', message).then(confirmed => {
        if (confirmed) {
          AuthUtil.login(authUtilConfig, destination);
        }
      });
    }
    else {
      AuthUtil.login(authUtilConfig, destination);
    }
  };

  ns.logout = function() {
    return confirm(
      'Are you sure you want to logout?',
      'Note: You must log out of any other EuPathDB sites separately'
    ).then(confirmed => {
      if (confirmed) {
        AuthUtil.logout(authUtilConfig);
      }
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
