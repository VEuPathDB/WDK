import WdkStore, { BaseState } from './WdkStore';
import {
  ProfileFormUpdateAction,
  ProfileFormSubmissionStatusAction
} from '../actioncreators/UserActionCreators';
import {User} from "../utils/WdkUser";

type Action = ProfileFormUpdateAction | ProfileFormSubmissionStatusAction;

export type State = BaseState & {
  userFormData?: User & {
    confirmEmail?: string;
  };
  formStatus: 'new' | 'modified' | 'pending' | 'success' | 'error';
  errorMessage?: string;
}

export default class UserProfileStore extends WdkStore<State> {

  // defines the structure of this store's data
  getInitialState(): State {
    return {
      ...super.getInitialState(),
      userFormData: undefined, // will be initialized when user is initialized
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }

  handleAction(state: State, action: Action): State {

    // Special case since this store is maintaining an unsaved, edited version of
    // the user, not the 'gold copy' saved version.  Need to override handling of
    // user load action.
    if (this.globalDataStore.hasChanged()) {
      let previousUser = this.getState().globalData.user;
      let nextUser = state.globalData.user;
      if (previousUser != nextUser) {
        return {
          ...state,
          userFormData: { ...nextUser, confirmEmail: nextUser.email },
          formStatus: "new",
          errorMessage: undefined
        };
      }
    }

    switch (action.type) {
      // form value has been updated; now different than 'saved' user
      case 'user/profile-form-update':
        return {
          ...state,
          userFormData: action.payload.user,
          formStatus: "modified"
        };
      case 'user/profile-form-submission-status':
        return {
          ...state,
          formStatus: action.payload.formStatus,
          errorMessage: action.payload.errorMessage
        };
      default:
        return state;
    }
  }
}
