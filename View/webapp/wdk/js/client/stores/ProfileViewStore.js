import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/UserActionCreator';

export default class ProfileViewStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null,
      isLoading: false,
      isChanged: false,
      outcome: "",
      message: ""
    };
  }

  reduce(state, {type, payload}) {
    switch (type) {

      case actionTypes.USER_PROFILE_UPDATE:
        return updateProfile(state, payload);

      case actionTypes.USER_PROFILE_ERROR:
        return handleProfileError(state, payload);

      default:
        return state;
    }
  }
}

/**
 * Accepts every change to the user profile form and applies it to the state.
 * @param state
 * @param payload
 * @returns {({}&*)|({}&*&*&{isLoading: boolean, isEdit: boolean})|any|({}&*&*)}
 */
function updateProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading: false, isChanged: true, outcome: ""});
}

function handleProfileError(state, payload) {
  return Object.assign({}, state, payload, {isLoading: false, isChanged: true, outcome: "error"});
}




