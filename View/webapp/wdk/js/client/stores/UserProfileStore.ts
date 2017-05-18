import WdkStore, { BaseState } from './WdkStore';
import {
  ProfileFormUpdateAction,
  ProfileFormSubmissionStatusAction
} from '../actioncreators/UserActionCreators';
import { User, UserPreferences } from "../utils/WdkUser";

type Action = ProfileFormUpdateAction | ProfileFormSubmissionStatusAction;

export type UserProfileFormData = User & {
  confirmEmail?: string;
  preferences?: UserPreferences;
};

export type State = BaseState & {
  userFormData?: UserProfileFormData;
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

    // Special case since this store is maintaining an unsaved, edited version
    // of the user and her preferences, not the 'gold copy' saved version.  Need
    // to override handling of user and preference load actions to update the
    // unsaved copies to be clones of the 'gold copy' versions.
    if (this.globalDataStore.hasChanged()) {
      let previousUser = this.getState().globalData.user;
      let nextUser = state.globalData.user;
      let previousPrefs = this.getState().globalData.preferences;
      let nextPrefs = state.globalData.preferences;
      if (previousUser != nextUser || previousPrefs != nextPrefs) {
        // either user or prefs changed in parent store with this action; update local copy
        return {
            ...state,
            userFormData: { ...nextUser, confirmEmail: nextUser.email, preferences: nextPrefs },
            formStatus: "new",
            errorMessage: undefined
        };
      }
    }

    return this.handleFormUpdate(state, action);
  }

  handleFormUpdate(state: State, action: Action): State {
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
