import WdkStore from './WdkStore';
import { StaticDataProps } from '../utils/StaticDataUtils';
import { actionTypes } from '../actioncreators/UserActionCreator';

export default class UserProfileStore extends WdkStore {

  getRequiredStaticDataProps() {
    return [ StaticDataProps.CONFIG, StaticDataProps.USER ];
  }

  // defines the structure of this store's data
  getInitialState() {
    return {
      config: null,       // loaded by WdkStore
      userFormData: null, // will be initialized when user is initialized
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }

  // Special case since this store is maintaining an unsaved, edited version of
  // the user, not the 'gold copy' saved version.  Need to override handling of
  // user load action.
  handleStaticDataItemAction(state, itemName, payload) {
    if (itemName === StaticDataProps.USER) {
      // reset the form when user changes and populate with the newly loaded user
      return Object.assign({}, state, {
        userFormData: Object.assign({}, payload.user, { confirmEmail: payload.user.email }),
        formStatus: "new",
        errorMessage: undefined
      });
    }
    else {
      // use default method for config
      return super.handleStaticDataItemAction(state, itemName, payload);
    }
  }

  handleAction(state, {type, payload}) {
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
