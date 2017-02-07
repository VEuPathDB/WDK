import {filterOutProps} from '../utils/componentUtils';
import {confirm} from '../utils/Platform';
import { broadcast } from '../utils/StaticDataUtils';
import {ActionCreator} from "../ActionCreator";
import {User, UserPreferences} from "../utils/WdkUser";
import {Record} from "../utils/WdkModel";
import * as AuthUtil from '../utils/AuthUtil';
import { State as PasswordStoreState } from '../stores/UserPasswordChangeStore';
import { State as ProfileStoreState } from '../stores/UserProfileStore';

// actions to update true user and preferences
export type UserUpdateAction = {
  type: "user/user-update",
  payload: {
    user: User;
  }
}
export type PreferenceUpdateAction = {
  type: 'user/preference-update',
  payload: UserPreferences
}

// actions to manage the user profile form
export type ProfileFormUpdateAction = {
  type: 'user/profile-form-update',
  payload: {
    user: User
  }
}
export type ProfileFormSubmissionStatusAction = {
  type: 'user/profile-form-submission-status',
  payload: {
    formStatus: ProfileStoreState['formStatus'];
    errorMessage: string | undefined;
  }
}

// actions to manage user password form
export type PasswordFormUpdateAction = {
  type: 'user/password-form-update',
  payload: PasswordStoreState['passwordForm'];
}
export type PasswordFormSubmissionStatusAction = {
  type: 'user/password-form-submission-status',
  payload: {
    formStatus: PasswordStoreState['formStatus'];
    errorMessage: string | undefined;
  }
}

// actions related to login
export type ShowLoginModalAction = {
  type: 'user/show-login-modal',
}
export type LoginDismissedAction = {
  type: 'user/login-dismissed',
}
export type LoginErrorAction = {
  type: 'user/login-error',
}

export type LoginRedirectAction = {
  type: 'user/login-redirect',
}
export type LogoutRedirectAction = {
  type: 'user/logout-redirect',
}

// basket actions
export type BasketStatusLoadingAction = {
  type: 'user/basket-status-loading',
  payload: {
    record: Record
  }
}
export type BasketStatusReceivedAction = {
  type: 'user/basket-status-received',
  payload: {
    record: Record;
    status: boolean;
  }
}
export type BasketStatusErrorAction = {
  type: 'user/basket-status-error',
  payload: {
    record: Record,
    error: Error
  }
}

// favorites actions
export type FavoritesStatusLoadingAction = {
  type: 'user/favorites-status-loading',
  payload: { record: Record }
}
export type FavoritesStatusReceivedAction = {
  type: 'user/favorites-status-received',
  payload: { record: Record, status: boolean }
}
export type FavoritesStatusErrorAction = {
  type: 'user/favorites-status-error',
  payload: { record: Record, error: Error }
}

/**
 * Merge supplied key-value pair with user preferences and update
 * on the server.
 */
export let updateUserPreference: ActionCreator<PreferenceUpdateAction> = (key: string, value: string) => {
  return function run(dispatch, { wdkService }) {
    let prefUpdater = function() {
      dispatch(broadcast({
        type: 'user/preference-update',
        payload: { [key]: value } as UserPreferences
      }) as PreferenceUpdateAction);
    };
    wdkService.updateCurrentUserPreference({ [key]: value })
      .then(
        () => {
          prefUpdater();
        },
        (error) => {
          console.error(error.response);
          // update stores anyway; not a huge deal if preference doesn't make it to server
          prefUpdater();
        }
      );
  };
};

function createProfileFormStatusAction(status: string, errorMessage?: string) {
  return createFormStatusAction('user/profile-form-submission-status', status, errorMessage) as ProfileFormSubmissionStatusAction;
}

function createPasswordFormStatusAction(status: string, errorMessage?: string) {
  return createFormStatusAction('user/password-form-submission-status', status, errorMessage) as PasswordFormSubmissionStatusAction;
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
export let submitProfileForm: ActionCreator<UserUpdateAction|ProfileFormSubmissionStatusAction> = (user: User) => {
  return function run(dispatch, { wdkService }) {
    dispatch(createProfileFormStatusAction('pending'));
    let trimmedUser = <User>filterOutProps(user, ["isGuest", "id", "preferences", "confirmEmail"]);
    return dispatch(wdkService.updateCurrentUser(trimmedUser)
      .then(() => {
        // success; update user first, then status in ProfileViewStore
        dispatch(broadcast({
          type: 'user/user-update',
          // NOTE: this prop name should be the same as that used in StaticDataActionCreator for 'user'
          // NOTE2: not all user props were sent to update but all should remain EXCEPT 'confirmEmail'
          payload: { user: filterOutProps(user, ["confirmEmail"]) as User}
        }) as UserUpdateAction);
        return createProfileFormStatusAction('success');
      })
      .catch((error) => {
        console.error(error.response);
        return createProfileFormStatusAction('error', error.response);
      }));
  };
};

/** Update user profile present in the form (unsaved changes) */
export let updateProfileForm: ActionCreator<ProfileFormUpdateAction> = (user: User) => {
  return {
    type: 'user/profile-form-update',
    payload: {user}
  }
};

/** Save new password to DB */
export let savePassword: ActionCreator<PasswordFormSubmissionStatusAction> = (oldPassword: string, newPassword: string) => {
  return function run(dispatch, { wdkService }) {
    dispatch(createPasswordFormStatusAction('pending'));
    wdkService.updateCurrentUserPassword(oldPassword, newPassword)
      .then(() => {
        dispatch(createPasswordFormStatusAction('success'));
      })
      .catch((error) => {
        console.error(error.response);
        dispatch(createPasswordFormStatusAction('error', error.response));
      });
  };
};

/** Update change password form data (unsaved changes) */
export let updateChangePasswordForm: ActionCreator<PasswordFormUpdateAction> = (formData: PasswordStoreState['passwordForm']) => {
  return {
    type: 'user/password-form-update',
    payload: formData
  }
};

// Session management action creators and helpers
// ----------------------------------------------

/**
 * Show a warning that user must be logged in for feature
 */
export let showLoginWarning: ActionCreator<{type: '__'}> = (attemptedAction: string, destination?: string) => {
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
export let showLoginForm: ActionCreator<{type:'__'}> = (destination = window.location.href) => {
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

export let showLogoutWarning: ActionCreator<{type:'__'}> = () => {
  return function(dispatch) {
    confirm(
      'Are you sure you want to logout?',
      'Note: You must log out of other EuPathDB sites separately'
    ).then(confirmed => {
      if (confirmed) dispatch(logout());
    });
  }
};

let logout: ActionCreator<{type:'__'}> = () => {
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

type BasketAction = BasketStatusLoadingAction | BasketStatusErrorAction | BasketStatusReceivedAction

/**
 * @param {Record} record
 */
export let loadBasketStatus: ActionCreator<BasketAction> = (record: Record) => {
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
export let updateBasketStatus: ActionCreator<BasketAction|{type:'__'}> = (user: User, record: Record, status: boolean) => {
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
let setBasketStatus: ActionCreator<BasketAction> = (record: Record, basketStatusPromise: Promise<boolean>) => {
  return function run(dispatch) {
    dispatch({
      type: 'user/basket-status-loading',
      payload: { record }
    });
    return basketStatusPromise.then(
      status => {
        dispatch({
          type: 'user/basket-status-received',
          payload: {record, status}
        })
      },
      error => {
        dispatch({
          type: 'user/basket-status-error',
          payload: { record, error }
        })
      }
    );
  };
};


// Favorites action creators and helpers
// -------------------------------------

type FavoriteAction = FavoritesStatusErrorAction | FavoritesStatusLoadingAction | FavoritesStatusReceivedAction

/** Create favorites action */
/**
 * @param {Record} record
 */
export let loadFavoritesStatus: ActionCreator<FavoriteAction> = (record: Record) => {
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
export let updateFavoritesStatus: ActionCreator<FavoriteAction|{type:'__'}> = (user: User, record: Record, status: boolean) => {
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
let setFavoritesStatus: ActionCreator<FavoriteAction> = (record: Record, statusPromise: Promise<boolean>) => {
  return function run(dispatch) {
    dispatch({
      type: 'user/favorites-status-loading',
      payload: { record }
    });
    return statusPromise.then(
      status => {
        dispatch({
          type: 'user/favorites-status-received',
          payload: {record, status}
        })
      },
      error => {
        dispatch({
          type: 'user/favorites-status-error',
          payload: { record, error }
        })
      }
    );
  };
};
