import {property} from 'lodash';
import {filterOutProps} from '../utils/componentUtils';
import {alert, confirm} from '../utils/Platform';
import { broadcast } from '../utils/StaticDataUtils';
import {ActionCreator} from "../ActionCreator";
import {User} from "../utils/WdkUser";
import {Record} from "../utils/WdkModel";
import * as AuthUtil from '../utils/AuthUtil';

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
  SHOW_LOGIN_MODAL: 'user/show-login-modal',
  LOGIN_DISMISSED: 'user/login-dismissed',
  LOGIN_ERROR: 'user/login-error',
  LOGIN_REDIRECT: 'user/login-redirect',
  LOGOUT_REDIRECT: 'user/logout-redirect',

  // basket actions
  BASKET_STATUS_LOADING: 'user/basket-status-loading',
  BASKET_STATUS_RECEIVED: 'user/basket-status-received',
  BASKET_STATUS_ERROR: 'user/basket-status-error',

  // favorites actions
  FAVORITES_STATUS_LOADING: 'user/favorites-status-loading',
  FAVORITES_STATUS_RECEIVED: 'user/favorites-status-received',
  FAVORITES_STATUS_ERROR: 'user/favorites-status-error'

};

/**
 * Merge supplied key-value pair with user preferences and update
 * on the server.
 */
export let updateUserPreference: ActionCreator = (key: string, value: string) => {
  return function run(dispatch, { wdkService }) {
    let prefUpdater = function() {
      return broadcast({
        type: actionTypes.USER_PREFERENCE_UPDATE,
        payload: { [key]: value }
      });
    };
    dispatch(wdkService.updateCurrentUserPreference({ [key]: value })
      .then(prefUpdater)
      .catch((error) => {
        console.error(error.response);
        // update stores anyway; not a huge deal if preference doesn't make it to server
        return prefUpdater();
      }));
  };
};

function createProfileFormStatusAction(status: string, errorMessage?: string) {
  return createFormStatusAction(actionTypes.USER_PROFILE_FORM_SUBMISSION_STATUS, status, errorMessage);
}

function createPasswordFormStatusAction(status: string, errorMessage?: string) {
  return createFormStatusAction(actionTypes.USER_PASSWORD_FORM_SUBMISSION_STATUS, status, errorMessage);
}

function createFormStatusAction(actionType: string, status: string, errorMessage?: string) {
  return {
    type: actionType,
    payload: {
      formStatus: status,
      errorMessage: errorMessage
    }
  }
}

/** Save user profile to DB */
export let submitProfileForm: ActionCreator = (user: User) => {
  return function run(dispatch, { wdkService }) {
    dispatch(createProfileFormStatusAction('pending'));
    let trimmedUser = <User>filterOutProps(user, ["isGuest", "id", "preferences", "confirmEmail"]);
    dispatch(wdkService.updateCurrentUser(trimmedUser)
      .then(() => {
        // success; update user first, then status in ProfileViewStore
        dispatch(broadcast({
          type: actionTypes.USER_UPDATE,
          // NOTE: this prop name should be the same as that used in StaticDataActionCreator for 'user'
          // NOTE2: not all user props were sent to update but all should remain EXCEPT 'confirmEmail'
          payload: { user: filterOutProps(user, ["confirmEmail"]) }
        }));
        return createProfileFormStatusAction('success');
      })
      .catch((error) => {
        console.error(error.response);
        return createProfileFormStatusAction('error', error.response);
      }));
  };
};

/** Update user profile present in the form (unsaved changes) */
export let updateProfileForm: ActionCreator = (user: User) => {
  return {
    type: actionTypes.USER_PROFILE_FORM_UPDATE,
    payload: {user}
  }
};

/** Save new password to DB */
export let savePassword: ActionCreator = (oldPassword: string, newPassword: string) => {
  return function run(dispatch, { wdkService }) {
    dispatch(createPasswordFormStatusAction('pending'));
    dispatch(wdkService.updateCurrentUserPassword(oldPassword, newPassword)
      .then(() => {
        return createPasswordFormStatusAction('success');
      })
      .catch((error) => {
        console.error(error.response);
        return createPasswordFormStatusAction('error', error.response);
      }));
  };
};

/** Update change password form data (unsaved changes) */
export let updateChangePasswordForm: ActionCreator = (formData: string) => {
  return {
    type: actionTypes.USER_PASSWORD_FORM_UPDATE,
    payload: formData
  }
};

// Session management action creators and helpers
// ----------------------------------------------

/**
 * Show a warning that user must be logged in for feature
 */
export let showLoginWarning: ActionCreator = (attemptedAction: string, destination?: string) => {
  return function(dispatch) {
    confirm(
      'Login Required',
      'To ' + attemptedAction + ', you must be logged in. Would you like to login now?'
    ).then(confirmed => {
      if (confirmed) dispatch(showLoginForm(destination));
    });
  }
};

/**
 * Show the login form based on config.
 */
export let showLoginForm: ActionCreator = (destination = window.location.href) => {
  return function(dispatch, {wdkService}) {
    wdkService.getConfig().then(config => {
      AuthUtil.login({
        webappUrl: config.webAppUrl,
        serviceUrl: wdkService.serviceUrl,
        method: config.authentication.method,
        oauthUrl: config.authentication.oauthUrl,
        oauthClientId: config.authentication.oauthClientId
      }, destination);
    });
  };
};

export let showLogoutWarning: ActionCreator = () => {
  return function(dispatch) {
    confirm(
      'Are you sure you want to logout?',
      'Note: You must log out of any other EuPathDB sites separately'
    ).then(confirmed => {
      if (confirmed) dispatch(logout());
    });
  }
};

let logout: ActionCreator = () => {
  return function run(dispatch, { wdkService }) {
    wdkService.getConfig().then(config => {
      AuthUtil.logout({
        webappUrl: config.webAppUrl,
        serviceUrl: wdkService.serviceUrl,
        method: config.authentication.method,
        oauthUrl: config.authentication.oauthUrl,
        oauthClientId: config.authentication.oauthClientId
      });
    });
  }
};


// Basket action creators and helpers
// ----------------------------------

/**
 * @param {Record} record
 */
export let loadBasketStatus: ActionCreator = (record: Record) => {
  //if (user.isGuest) return basketAction(record, false);
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.getBasketStatus(record)
    ));
  };
};

/**
 * @param {User} user
 * @param {Record} record
 * @param {Boolean} status
 */
export let updateBasketStatus: ActionCreator = (user: User, record: Record, status: boolean) => {
  if (user.isGuest) return showLoginWarning('use baskets');
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.updateBasketStatus(record, status)
    ));
  };
};

/**
 * @param {Record} record
 * @param {Promise<boolean>} basketStatusPromise
 */
let setBasketStatus: ActionCreator = (record: Record, basketStatusPromise: Promise<boolean>) => {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.BASKET_STATUS_LOADING,
      payload: { record }
    });
    return dispatch(basketStatusPromise.then(status => {
      return {
        type: actionTypes.BASKET_STATUS_RECEIVED,
        payload: { record, status }
      };
    }, error => {
      return {
        type: actionTypes.BASKET_STATUS_ERROR,
        payload: { record, error }
      };
    }));
  };
};


// Favorites action creators and helpers
// -------------------------------------

/** Create favorites action */
/**
 * @param {Record} record
 */
export let loadFavoritesStatus: ActionCreator = (record: Record) => {
  //if (user.isGuest) return favoritesAction(record, false);
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.getFavoritesStatus(record)
    ));
  };
};

/**
 * @param {User} user
 * @param {Record} record
 * @param {Boolean} status
 */
export let updateFavoritesStatus: ActionCreator = (user: User, record: Record, status: boolean) => {
  if (user.isGuest) return showLoginWarning('use favorites');
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.updateFavoritesStatus(record, status)
    ));
  };
};

/**
 * @param {Record} record
 * @param {Promise<Boolean>} statusPromise
 */
let setFavoritesStatus: ActionCreator = (record: Record, statusPromise: Promise<boolean>) => {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.FAVORITES_STATUS_LOADING,
      payload: { record }
    });
    dispatch(statusPromise.then(status => {
      return {
        type: actionTypes.FAVORITES_STATUS_RECEIVED,
        payload: { record, status }
      };
    }, error => {
      dispatch({
        type: actionTypes.FAVORITES_STATUS_ERROR,
        payload: { record, error }
      });
      throw error;
    }));
  };
};
