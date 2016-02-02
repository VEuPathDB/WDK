import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
  USER_LOADING: 'user/loading',
  USER_INITIALIZE_STORE: 'user/initialize',
  USER_PROFILE_UPDATE: 'user/profile-update',
  USER_PROPERTY_UPDATE: 'user/property-update',
  USER_PREFERENCE_UPDATE: 'user/preference-update',
  APP_ERROR: 'user/error'
};

export default class UserActionCreator extends ActionCreator {

  loadCurrentUser() {

    this._dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = this._service.getCurrentUser();
    let preferencePromise = this._service.getCurrentUserPreferences();

    Promise.all([ userPromise, preferencePromise ])
    .then(([ user, preferences ]) => {
      this._dispatch({
        type: actionTypes.USER_INITIALIZE_STORE,
        payload: { user, preferences }
      })
    }, this._errorHandler(actionTypes.APP_ERROR));
  }
}

UserActionCreator.actionTypes = actionTypes;
