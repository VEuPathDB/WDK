import {filterOutProps} from '../utils/componentUtils';
import {confirm} from '../utils/Platform';
import { broadcast } from '../utils/StaticDataUtils';
import {ActionCreator, DispatchAction} from "../ActionCreator";
import {User, UserPreferences, PreferenceScope, UserWithPrefs} from "../utils/WdkUser";
import {RecordInstance} from "../utils/WdkModel";
import * as AuthUtil from '../utils/AuthUtil';
import { State as PasswordStoreState } from '../stores/UserPasswordChangeStore';
import { State as ProfileStoreState, UserProfileFormData } from '../stores/UserProfileStore';

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
export type PreferencesUpdateAction = {
  type: 'user/preferences-update',
  payload: UserPreferences
}
type PrefAction = PreferenceUpdateAction|PreferencesUpdateAction;

// actions to manage the user profile/registration forms
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

export type ClearRegistrationFormAction = {
  type: 'user/clear-registration-form'
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

// actions to manage user password reset form
export type ResetPasswordUpdateEmailAction = {
  type: 'user/reset-password-email-update',
  payload: string
}
export type ResetPasswordSubmissionStatusAction = {
  type: 'user/reset-password-submission-status',
  payload : {
    success: boolean,
    message: string
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
    record: RecordInstance
  }
}
export type BasketStatusReceivedAction = {
  type: 'user/basket-status-received',
  payload: {
    record: RecordInstance;
    status: boolean;
  }
}
export type BasketStatusErrorAction = {
  type: 'user/basket-status-error',
  payload: {
    record: RecordInstance,
    error: Error
  }
}

// favorites actions
export type FavoritesStatusLoadingAction = {
  type: 'user/favorites-status-loading',
  payload: { record: RecordInstance }
}
export type FavoritesStatusReceivedAction = {
  type: 'user/favorites-status-received',
  payload: { record: RecordInstance, id: number }
}
export type FavoritesStatusErrorAction = {
  type: 'user/favorites-status-error',
  payload: { record: RecordInstance, error: Error }
}

/**
 * Merge supplied key-value pair with user preferences and update
 * on the server.
 */
export let updateUserPreference: ActionCreator<PreferenceUpdateAction> = (scope: PreferenceScope, key: string, value: string) => {
  return function run(dispatch, { wdkService }) {
    let updatePromise = wdkService.updateCurrentUserPreference(scope, key, value);
    return dispatch(sendPrefUpdateOnCompletion(updatePromise,
        'user/preference-update', { [scope]: { [key]: value } }) as Promise<PreferenceUpdateAction>);
  };
};

export let updateUserPreferences: ActionCreator<PreferencesUpdateAction> = (newPreferences: UserPreferences) => {
  return function run(dispatch, { wdkService }) {
    let updatePromise = wdkService.updateCurrentUserPreferences(newPreferences);
    return dispatch(sendPrefUpdateOnCompletion(updatePromise,
        'user/preferences-update', newPreferences) as Promise<PreferencesUpdateAction>);
  };
};

let sendPrefUpdateOnCompletion: ActionCreator<PreferenceUpdateAction|PreferencesUpdateAction> =
    (promise: Promise<void>, actionName: string, payload: UserPreferences) => {
  return function run(dispatch, { wdkService }) {
    let prefUpdater = function() {
      return dispatch(broadcast({
        type: actionName,
        payload: payload as UserPreferences
      }) as PreferenceUpdateAction|PreferencesUpdateAction);
    };
    return promise.then(
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
type SubmitProfileFormType = ActionCreator<UserUpdateAction|PreferencesUpdateAction|ProfileFormSubmissionStatusAction>;
export let submitProfileForm: SubmitProfileFormType = (user: UserProfileFormData) => {
  return function run(dispatch, { wdkService }) {
    dispatch(createProfileFormStatusAction('pending'));
    let trimmedUser = <UserProfileFormData>filterOutProps(user, ["isGuest", "id", "confirmEmail", "preferences"]);
    let userPromise = wdkService.updateCurrentUser(trimmedUser);
    let prefPromise = wdkService.updateCurrentUserPreferences(user.preferences as UserPreferences); // should never be null by this point
    return dispatch(Promise.all([userPromise, prefPromise])
      .then(() => {
        // success; update user first, then prefs, then status in ProfileViewStore
        dispatch(broadcast({
          type: 'user/user-update',
          // NOTE: this prop name should be the same as that used in StaticDataActionCreator for 'user'
          // NOTE2: not all user props were sent to update but all should remain EXCEPT 'confirmEmail' and 'preferences'
          payload: { user: filterOutProps(user, ["confirmEmail", "preferences"]) as User }
        }) as UserUpdateAction);
        dispatch(broadcast({
          type: 'user/preferences-update',
          payload: user.preferences as UserPreferences
        }) as PreferencesUpdateAction);
        return createProfileFormStatusAction('success');
      })
      .catch((error) => {
        console.error(error.response);
        return createProfileFormStatusAction('error', error.response);
      }));
  };
};

/** Register user */
type SubmitRegistrationFormType = ActionCreator<ProfileFormSubmissionStatusAction|ClearRegistrationFormAction>;
export let submitRegistrationForm: SubmitRegistrationFormType = (formData: UserProfileFormData) => {
  return function run(dispatch, { wdkService, transitioner }) {
    dispatch(createProfileFormStatusAction('pending'));
    let trimmedUser = <User>filterOutProps(formData, ["isGuest", "id", "preferences", "confirmEmail"]);
    let registrationData: UserWithPrefs = {
      user: trimmedUser,
      preferences: formData.preferences as UserPreferences
    }
    wdkService.createNewUser(registrationData)
      .then(responseData => {
        // success; clear the form in case user wants to register another user
        dispatch(broadcast({ type: 'user/clear-registration-form' }) as ClearRegistrationFormAction);
        // add success message to top of page (no longer needed with state transition)
        //dispatch(createProfileFormStatusAction('success'));
        // then transition to registration success message page
        transitioner.transitionToInternalPage('/user/message/registration-successful');
      })
      .catch((error) => {
        console.error(error.response);
        dispatch(createProfileFormStatusAction('error', error.response));
      });
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

export let updatePasswordResetEmail: ActionCreator<ResetPasswordUpdateEmailAction> = (emailText: string) => {
  return {
    type: 'user/reset-password-email-update',
    payload: emailText
  };
};

let createResetPasswordStatusAction: ActionCreator<ResetPasswordSubmissionStatusAction> = (message: string) => {
  return {
    type: 'user/reset-password-submission-status',
    payload: {
      success: (message ? false : true),
      message: message
    }
  }
};

export let submitPasswordReset: ActionCreator<ResetPasswordSubmissionStatusAction> = (email: string) => {
  return function run(dispatch, { wdkService, transitioner }) {
    dispatch(createResetPasswordStatusAction("Submitting..."));
    wdkService.resetUserPassword(email).then(
        () => {
          // clear form for next visitor to this page
          dispatch(createResetPasswordStatusAction(undefined));
          // transition to user message page
          transitioner.transitionToInternalPage('/user/message/password-reset-successful');
        },
        error => {
          dispatch(createResetPasswordStatusAction(error.response));
        }
    );
  };
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

//----------------------------------
// Basket action creators and helpers
// ----------------------------------

type BasketAction = BasketStatusLoadingAction | BasketStatusErrorAction | BasketStatusReceivedAction

/**
 * @param {Record} record
 */
export let loadBasketStatus: ActionCreator<BasketAction> = (record: RecordInstance) => {
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
export let updateBasketStatus: ActionCreator<BasketAction|{type:'__'}> = (user: User, record: RecordInstance, status: boolean) => {
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
let setBasketStatus: ActionCreator<BasketAction> = (record: RecordInstance, basketStatusPromise: Promise<boolean>) => {
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
export let loadFavoritesStatus: ActionCreator<FavoriteAction> = (record: RecordInstance) => {
  //if (user.isGuest) return favoritesAction(record, false);
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.getFavoriteId(record)
    ));
  };
};

export let removeFavorite: ActionCreator<FavoriteAction> = (record: RecordInstance, favoriteId: number) => {
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.deleteFavorite(favoriteId)
    ));
  };
};

export let addFavorite: ActionCreator<FavoriteAction|{type:'__'}> = (user: User, record: RecordInstance) => {
  if (user.isGuest) {
    return showLoginWarning('use favorites');
  }
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.addFavorite(record)
    ));
  };
};

/**
 * @param {Record} record
 * @param {Promise<Boolean>} statusPromise
 */
let setFavoritesStatus: ActionCreator<FavoriteAction> = (record: RecordInstance, statusPromise: Promise<number>) => {
  return function run(dispatch) {
    dispatch({
      type: 'user/favorites-status-loading',
      payload: { record }
    });
    return statusPromise.then(
      id => {
        dispatch({
          type: 'user/favorites-status-received',
          payload: {record, id}
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
