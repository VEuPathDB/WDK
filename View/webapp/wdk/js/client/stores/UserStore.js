import WdkStore from './WdkStore';
import {filterRecords} from '../utils/recordUtils';
import { actionTypes } from '../actioncreators/UserActionCreator';

export default class UserStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null,
      preferences: null,
      baskets: {},
      isLoading: false,
      isEdit: false
    };
  }

  reduce(state, { type, payload }) {
    switch(type) {
      case actionTypes.USER_LOADING:
        return userLoading(state, { isLoading: true });

      case actionTypes.USER_INITIALIZE_STORE:
        return initializeUser(state, payload);

      case actionTypes.USER_PROPERTY_UPDATE:
        return updateProperties(state, payload);

      case actionTypes.USER_PREFERENCE_UPDATE:
        return updatePreferences(state, payload);

      case actionTypes.USER_PROFILE_SAVE:
        return saveProfile(state, payload);

      case actionTypes.APP_ERROR:
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
  return Object.assign({}, state, payload, { isLoading: false, isEdit: false });
}

function saveProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: false});
}
