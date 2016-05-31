import {property} from 'lodash';
import {filterOutProps} from '../utils/componentUtils';
import {alert, confirm} from '../utils/Platform';

// Action types
export let actionTypes = {
  USER_LOADING: 'user/loading',
  USER_INITIALIZE_STORE: 'user/initialize',
  USER_PROFILE_UPDATE: 'user/profile-update',
  USER_PROFILE_ERROR: 'user/profile-error',
  USER_PROPERTY_UPDATE: 'user/property-update',
  USER_PREFERENCE_UPDATE: 'user/preference-update',
  USER_PROFILE_SAVE: 'user/profile-save',
  LOGIN_DISMISSED: 'user/login-dismissed',
  BASKET_STATUS_LOADING: 'user/basket-status-loading',
  BASKET_STATUS_RECEIVED: 'user/basket-status-received',
  BASKET_STATUS_ERROR: 'user/basket-status-error',
  FAVORITES_STATUS_LOADING: 'user/favorites-status-loading',
  FAVORITES_STATUS_RECEIVED: 'user/favorites-status-received',
  FAVORITES_STATUS_ERROR: 'user/favorites-status-error',
  APP_ERROR: 'user/error'
};

/** Load current user */
export function loadCurrentUser() {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = wdkService.getCurrentUser();
    let preferencePromise = wdkService.getCurrentUserPreferences();

    return Promise.all([ userPromise, preferencePromise ])
    .then(([ user, preferences ]) => {
      dispatch({
        type: actionTypes.USER_INITIALIZE_STORE,
        payload: { user, preferences }
      });
      return { user, preferences };
    }, error => {
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });
  };
}


/** Update user profile */
export function updateProfile(user) {
  return {
    type: actionTypes.USER_PROFILE_UPDATE,
    payload: {user}
  }
}

/** Save user profile */
export function saveProfile(user) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = wdkService.updateCurrentUser(filterOutProps(user,["isGuest","id","preferences"]));

    return userPromise.then(() => {
        dispatch({
          type: actionTypes.USER_PROFILE_SAVE,
          payload: { user, message : "Your profile has been successfully updated." }
        });
        return { user };
      }, error => {
        dispatch({
          type: actionTypes.USER_PROFILE_ERROR,
          payload: { message : error.response }
        });
      });
  };

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
          redirectUrl = redirectUrlBase + '?redirectUrl=' +
            encodeURIComponent(destination);
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
  }
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
export function loadBasketStatus(user, record) {
  if (user.isGuest) return basketAction(record, false);
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
  }
}

/**
 * @param {RecordDescriptor} record
 */
export function loadFavoritesStatus(user, record) {
  if (user.isGuest) return favoritesAction(record, false);
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

