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

      default:
        return state;
    }
  }
}

/**
 * The effect is to alter the profile page to show the form
 * @param state
 * @param payload
 * @returns {({}&*)|({}&*&*&{isLoading: boolean, isEdit: boolean})|any|({}&*&*)}
 */
function editProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: true });
}


/**
 * Accepts every change to the user profile form and applies it to the state.
 * @param state
 * @param payload
 * @returns {({}&*)|({}&*&*&{isLoading: boolean, isEdit: boolean})|any|({}&*&*)}
 */
function updateProfile(state, payload) {
  return Object.assign({}, state, payload, {isLoading:false, isEdit: true});
}



