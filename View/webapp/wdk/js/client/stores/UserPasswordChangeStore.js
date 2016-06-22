import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/UserActionCreator';
import { StaticDataProps } from '../utils/StaticDataUtils';

export default class UserPasswordChangeStore extends WdkStore {

  getRequiredStaticDataProps() {
    return [ StaticDataProps.USER ];
  }

  // defines the structure of this store's data
  getInitialState() {
    return {
      user: null, // loaded by parent
      passwordForm: getEmptyForm(),
      formStatus: 'new', // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }

  handleAction(state, {type, payload}) {
    switch (type) {
      case actionTypes.USER_PASSWORD_FORM_UPDATE:
        return Object.assign({}, state, {
          passwordForm: payload,
          formStatus: 'modified'
        });
      case actionTypes.USER_PASSWORD_FORM_SUBMISSION_STATUS:
        return Object.assign({}, state, {
          // in all status update cases, we should clear passwords
          passwordForm: getEmptyForm(),
          formStatus: payload.formStatus,
          errorMessage: payload.errorMessage,
        });
      default:
        return state;
    }
  }
}

function getEmptyForm() {
  return {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  };
}
