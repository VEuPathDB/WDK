import { filterOutProps } from '../utils/componentUtils';

// Action types
export let actionTypes = {
  USER_LOADING: 'user/loading',
  USER_INITIALIZE_STORE: 'user/initialize',
  USER_PROFILE_UPDATE: 'user/profile-update',
  USER_PROFILE_ERROR: 'user/profile-error',
  USER_PROPERTY_UPDATE: 'user/property-update',
  USER_PREFERENCE_UPDATE: 'user/preference-update',
  USER_PROFILE_SAVE: 'user/profile-save',
  APP_ERROR: 'user/error'
};

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


export function updateProfile(user) {
  return {
    type: actionTypes.USER_PROFILE_UPDATE,
    payload: {user}
  }
}

export function saveProfile(user) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = wdkService.updateCurrentUser(filterOutProps(user,["isGuest","id","preferences","baskets"]));

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

