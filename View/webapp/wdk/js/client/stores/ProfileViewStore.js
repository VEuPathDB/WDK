import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/UserActionCreator';

export default class ProfileViewStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null,
      isLoading: false,
      isEdit: false
    };
  }

  reduce(state, { type, payload }) {
    switch(type) {

      case actionTypes.USER_PROFILE_EDIT:
        return editProfile(state, payload);

      case actionTypes.USER_PROFILE_UPDATE:
        return updateProfile(state, payload);

      case actionTypes.USER_PROFILE_SAVE:
        return saveProfile(state, payload);

      default:
        return state;
    }
  }
}


function editProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: true });
}

function updateProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: true});
}

function saveProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: false});
}


