/* global wdk, wdkConfig */
import { createElement } from 'react';
import { render } from 'react-dom';
import { alert, confirm } from './client/utils/Platform';
import LoginForm from './client/components/LoginForm';

// FIXME Review module
// Some redundant functions, some undefined functions called, etc.

wdk.namespace("window.wdk.user", function(ns, $) {
  "use strict";

  ns.id = function() { return wdkConfig.wdkUser.id; };
  ns.name = function() { return wdkConfig.wdkUser.name; };
  ns.country = function() { return wdkConfig.wdkUser.country; };
  ns.email = function() { return wdkConfig.wdkUser.email; };
  ns.isGuest = function() { return wdkConfig.guestUser; };
  ns.isUserLoggedIn = function() { return !ns.isGuest(); };

  ns.login = function(action, destination = window.location.href) {
    if (action) {
      let message = `To ${action}, you must be logged in. Would you like to login now?`;
      confirm('Login required', message).then(confirmed => {
        if (confirmed) {
          renderLoginForm(destination, true);
        }
      });
    }
    else {
      renderLoginForm(destination, true);
    }
  };

  let loginContainer;
  function renderLoginForm(destination, open) {
    loginContainer = loginContainer || document.body.appendChild(
      document.createElement('div'));
    wdk.context.wdkService.getConfig().then(config => {
      let { authentication, webAppUrl } = config;
      if (authentication.method === 'OAUTH2') {
        let { oauthClientId, oauthUrl } = authentication;
        return wdk.context.wdkService.getOauthStateToken().then(response => {
          // build URL to OAuth service and redirect
          let redirectUrlBase = webAppUrl + '/processLogin.do';

          let googleSpecific = (oauthUrl.indexOf("google") != -1);
          let redirectUrl, authEndpoint;
          if (googleSpecific) {
            // hacks to conform to google OAuth2 API
            redirectUrl = redirectUrlBase;
            authEndpoint = "auth";
          }
          else {
            redirectUrl = redirectUrlBase + '?redirectUrl=' + encodeURIComponent(destination);
            authEndpoint = "authorize";
          }

          let finalOauthUrl = oauthUrl + "/" + authEndpoint + "?" +
            "response_type=code&" +
            "scope=" + encodeURIComponent("openid email") + "&" +
            "state=" + encodeURIComponent(response.oauthStateToken) + "&" +
            "client_id=" + oauthClientId + "&" +
            "redirect_uri=" + encodeURIComponent(redirectUrl);

          window.location.assign(finalOauthUrl);
        }).catch(error => {
          alert("Unable to fetch your WDK state token.", "Please check your internet connection.");
          throw error;
        });
      }
      else {
        let component = render(createElement('div', {}, createElement(LoginForm, {
          onCancel: () => {
            renderLoginForm(null, false);
          },
          onSubmit: () => {
          },
          open: open,
          action: webAppUrl + '/processLogin.do',
          redirectUrl: destination,
          passwordResetUrl: webAppUrl + '/showResetPassword.do',
          registerUrl: webAppUrl + '/showRegister.do'
        })), loginContainer);
      }
    });
  }

  ns.logout = function() {
    return confirm(
      'Are you sure you want to logout?',
      'Note: You must log out of any other EuPathDB sites separately'
    ).then(confirmed => {
      if (confirmed) {
        wdk.context.wdkService.getConfig().then(config => {
          let logoutUrl = config.webAppUrl + '/processLogout.do';
          if (config.authentication.method === 'OAUTH2') {
            let { oauthUrl } = config.authentication;
            let googleSpecific = (oauthUrl.indexOf("google") != -1);
            // don't log user out of google, only the eupath oauth server
            let nextPage = (googleSpecific ? logoutUrl :
            oauthUrl + "/logout?redirect_uri=" + encodeURIComponent(logoutUrl));
            window.location.assign(nextPage);
          }
          else {
            window.location.assign(logoutUrl);
          }
        })
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
      catch(e) {}
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
