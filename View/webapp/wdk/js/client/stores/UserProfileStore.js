import WdkStore from './WdkStore';
import { StaticDataProps } from '../utils/StaticDataUtils';
import { actionTypes } from '../actioncreators/UserActionCreators';

export default class UserProfileStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {
      globalData:{},       // loaded by WdkStore
      userFormData: null, // will be initialized when user is initialized
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }

  handleAction(state, {type, payload}) {

    // Special case since this store is maintaining an unsaved, edited version of
    // the user, not the 'gold copy' saved version.  Need to override handling of
    // user load action.
    if (this.globalDataStore.hasChanged()) {
      let previousUser = this.getState().globalData.user;
      let nextUser = state.globalData.user;
      if (previousUser != nextUser) {
        return Object.assign({}, state, {
          userFormData: Object.assign({}, nextUser, { confirmEmail: nextUser.email }),
          formStatus: "new",
          errorMessage: undefined
        });
      }
    }

    switch (type) {
      // form value has been updated; now different than 'saved' user
      case actionTypes.USER_PROFILE_FORM_UPDATE:
        return Object.assign({}, state, {
          userFormData: payload.user,
          formStatus: "modified"
        });
      case actionTypes.USER_PROFILE_FORM_SUBMISSION_STATUS:
        return Object.assign({}, state, {
          formStatus: payload.formStatus,
          errorMessage: payload.errorMessage
        });
      default:
        return state;
    }
  }
}
