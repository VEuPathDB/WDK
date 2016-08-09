import {property} from 'lodash';
import {filterOutProps} from '../utils/componentUtils';
import {alert, confirm} from '../utils/Platform';
import { broadcast } from '../utils/StaticDataUtils';

export let actionTypes = {

  // actions to update true user and preferences
  USER_UPDATE: "user/user-update",
  USER_PREFERENCE_UPDATE: 'user/preference-update',

  // actions to manage the user profile form
  USER_PROFILE_FORM_UPDATE: 'user/profile-form-update',
  USER_PROFILE_FORM_SUBMISSION_STATUS: 'user/profile-form-submission-status',

  // actions to manage user password form
  USER_PASSWORD_FORM_UPDATE: 'user/password-form-update',
  USER_PASSWORD_FORM_SUBMISSION_STATUS: 'user/password-form-submission-status',

  // actions related to login
  LOGIN_DISMISSED: 'user/login-dismissed',

  // basket actions
  BASKET_STATUS_LOADING: 'user/basket-status-loading',
  BASKET_STATUS_RECEIVED: 'user/basket-status-received',
  BASKET_STATUS_ERROR: 'user/basket-status-error',

  // favorites actions
  FAVORITES_STATUS_LOADING: 'user/favorites-status-loading',
  FAVORITES_STATUS_RECEIVED: 'user/favorites-status-received',
  FAVORITES_STATUS_ERROR: 'user/favorites-status-error'

};

export function updateUserPreference(key, value) {
  return function run(dispatch, { wdkService }) {
    wdkService.updateCurrentUserPreference({ [key]: value }).then(function() {
      dispatch(broadcast({
        type: actionTypes.USER_PREFERENCE_UPDATE,
        payload: { [key]: value }
      }));
    });
  };
}

function createProfileFormStatusAction(status, errorMessage) {
  return createFormStatusAction(actionTypes.USER_PROFILE_FORM_SUBMISSION_STATUS, status, errorMessage);
}

function createPasswordFormStatusAction(status, errorMessage) {
  return createFormStatusAction(actionTypes.USER_PASSWORD_FORM_SUBMISSION_STATUS, status, errorMessage);
}

function createFormStatusAction(actionType, status, errorMessage) {
  return {
    type: actionType,
    payload: {
      formStatus: status,
      errorMessage: errorMessage
    }
  }
}

/** Save user profile to DB */
export function submitProfileForm(user) {
  return function run(dispatch, { wdkService }) {
    dispatch(createProfileFormStatusAction('pending'));
    let trimmedUser = filterOutProps(user, ["isGuest", "id", "preferences", "confirmEmail"]);
    wdkService.updateCurrentUser(trimmedUser)
      .then(() => {
        // success; update user first, then status in ProfileViewStore
        dispatch(broadcast({
          type: actionTypes.USER_UPDATE,
          // NOTE: this prop name should be the same as that used in StaticDataActionCreator for 'user'
          // NOTE2: not all user props were sent to update but all should remain EXCEPT 'confirmEmail'
          payload: { user: filterOutProps(user, ["confirmEmail"]) }
        }));
        dispatch(createProfileFormStatusAction('success'));
      })
      .catch(error => {
        console.error(error.response);
        dispatch(createProfileFormStatusAction('error', error.response));
      });
  };
}

/** Update user profile present in the form (unsaved changes) */
export function updateProfileForm(user) {
  return {
    type: actionTypes.USER_PROFILE_FORM_UPDATE,
    payload: {user}
  }
}

/** Save new password to DB */
export function savePassword(oldPassword, newPassword) {
  return function run(dispatch, { wdkService }) {
    dispatch(createPasswordFormStatusAction('pending'));
    wdkService.updateCurrentUserPassword(oldPassword, newPassword)
      .then(() => {
        dispatch(createPasswordFormStatusAction('success'));
      })
      .catch(error => {
        console.error(error.response);
        dispatch(createPasswordFormStatusAction('error', error.response));
      });
  };
}

/** Update change password form data (unsaved changes) */
export function updateChangePasswordForm(formData) {
  return {
    type: actionTypes.USER_PASSWORD_FORM_UPDATE,
    payload: formData
  }
}

// Session management action creators and helpers
// ----------------------------------------------

/**
 * Show a warning that user must be logged in for feature
 */
export function showLoginWarning(attemptedAction, destination) {
  return confirm(
    'Login Required',
    'To ' + attemptedAction + ', you must be logged in. Would you like to login now?'
  ).then(confirmed => {
    return confirmed ? showLoginForm(destination) : { type: actionTypes.LOGIN_DISMISSED };
  });
}

/**
 * Show the login form based on config.
 */
export function showLoginForm(destination = window.location.href) {
  return (dispatch, {wdkService}) => {
    let showLoginFormImpl$ = wdkService.getConfig()
    .then(property('authentication.method'))
    .then(method => getShowLoginFormImpl(method, destination));
    return dispatch(showLoginFormImpl$);
  };
}

/** get showLoginForm implementation based on authentication method */
function getShowLoginFormImpl(method, destination) {
  return method === 'OAUTH2' ? showOauthLoginForm(destination) : showUserdbLoginForm(destination);
}

/** redirect to oauth server login page */
function showOauthLoginForm(destination) {
  return function (dispatch, { wdkService }) {
    return wdkService.getConfig().then(config => {
      let { webAppUrl, authentication: { oauthClientId, oauthUrl } } = config;
      return wdkService.getOauthStateToken().then(response => {
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

        window.location = finalOauthUrl;
      }).catch(error => {
        alert("Unable to fetch your WDK state token.", "Please check your internet connection.");
        throw error;
      });
    });
  }
}

/** dispatch action to show modal dialog */
function showUserdbLoginForm(destination) {
  return new Promise(function(resolve) {
    window.jQuery('#wdk-dialog-login-form')
    .dialog('open')
    .find("input[name='redirectUrl']").val(destination)
    .one('dialogclose', () => resolve({ type: actionTypes.LOGIN_DISMISSED }));
  });
}


// Basket action creators and helpers
// ----------------------------------

/** Create basket action */
function basketAction(record, status) {
  return {
    type: actionTypes.BASKET_STATUS_RECEIVED,
    payload: { record, status }
  };
}

/**
 * @typedef RecordDescriptor
 * @type {Object}
 * @property {string} recordClassName
 * @property {Object.<string, string>} id
 */

/**
 * @param {RecordDescriptor} record
 */
export function loadBasketStatus(record) {
  //if (user.isGuest) return basketAction(record, false);
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.getBasketStatus(record)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Boolean} status
 */
export function updateBasketStatus(user, record, status) {
  if (user.isGuest) return showLoginWarning('use baskets');
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.updateBasketStatus(record, status)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Promise<Boolean>} basketStatusPromise
 */
function setBasketStatus(record, basketStatusPromise) {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.BASKET_STATUS_LOADING,
      payload: { record }
    });
    return basketStatusPromise.then(status => {
      return dispatch(basketAction(record, status));
    }, error => {
      return dispatch({
        type: actionTypes.BASKET_STATUS_ERROR,
        payload: { record, error }
      });
    });
  };
}


// Favorites action creators and helpers
// -------------------------------------

/** Create favorites action */
function favoritesAction(record, status) {
  return {
    type: actionTypes.FAVORITES_STATUS_RECEIVED,
    payload: { record, status }
  };
}

/**
 * @param {RecordDescriptor} record
 */
export function loadFavoritesStatus(record) {
  //if (user.isGuest) return favoritesAction(record, false);
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.getFavoritesStatus(record)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Boolean} status
 */
export function updateFavoritesStatus(user, record, status) {
  if (user.isGuest) return showLoginWarning('use favorites');
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.updateFavoritesStatus(record, status)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Promise<Boolean>} statusPromise
 */
function setFavoritesStatus(record, statusPromise) {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.FAVORITES_STATUS_LOADING,
      payload: { record }
    });
    return statusPromise.then(status => {
      return dispatch({
        type: actionTypes.FAVORITES_STATUS_RECEIVED,
        payload: { record, status }
      });
    }, error => {
      dispatch({
        type: actionTypes.FAVORITES_STATUS_ERROR,
        payload: { record, error }
      });
      throw error;
    });
  };
}
