import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import UserActionCreator from '../actioncreators/UserActionCreator';

let {
  USER_LOADING,
  USER_INITIALIZE_STORE,
  USER_PROFILE_UPDATE,
  USER_PROPERTY_UPDATE,
  USER_PREFERENCE_UPDATE,
  APP_ERROR
} = UserActionCreator.actionTypes;

export default class UserStore extends ReduceStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null,
      preferences: null,
      isLoading: false
    };
  }

  reduce(state, { type, payload }) {
    switch(type) {
      case USER_LOADING:
        return userLoading(state, { isLoading: true });

      case USER_INITIALIZE_STORE:
        return initializeUser(state, payload);

      case USER_PROFILE_UPDATE:
        return updateProfile(state, payload);

      case USER_PROPERTY_UPDATE:
        return updateProperties(state, payload);

      case USER_PREFERENCE_UPDATE:
        return updatePreferences(state, payload);

      case APP_ERROR:
        return userLoading(state, { isLoading: false });

      default:
        return state;
    }
  }
}

function userLoading(state, payload) {
  return Object.assign({}, state, { isLoading: payload.isLoading });
}

function initializeUser(state, payload) {
  return Object.assign({}, payload, { isLoading: false });
}
