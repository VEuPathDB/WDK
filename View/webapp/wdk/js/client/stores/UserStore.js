import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import UserActionCreator from '../actioncreators/UserActionCreator';

let action = UserActionCreator.actionTypes;

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
      case action.USER_LOADING:
        return userLoading(state, { isLoading: true });

      case action.USER_INITIALIZE_STORE:
        return initializeUser(state, payload);

      case action.USER_PROFILE_UPDATE:
        return updateProfile(state, payload);

      case action.USER_PROPERTY_UPDATE:
        return updateProperties(state, payload);

      case action.USER_PREFERENCE_UPDATE:
        return updatePreferences(state, payload);

      case action.APP_ERROR:
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
