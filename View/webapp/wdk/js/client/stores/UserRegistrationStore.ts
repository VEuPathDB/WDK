import UserProfileStore, { State, Action, UserProfileFormData } from './UserProfileStore';
import { ClearRegistrationFormAction } from '../actioncreators/UserActionCreators';
import { User, UserPreferences } from '../utils/WdkUser';

type RegistrationAction = Action | ClearRegistrationFormAction;

let emptyUserFormData: UserProfileFormData = {
  id: 0,
  email: '',
  isGuest: true,
  properties: { },
  confirmEmail: '',
  preferences: {
    global: {},
    project: {}
  } as UserPreferences
};

export default class UserRegistrationStore extends UserProfileStore {

  // defines the structure of this store's data
  getInitialState(): State {
    return {
      ...super.getInitialState(),
      userFormData: emptyUserFormData,
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }

  handleAction(state: State, action: RegistrationAction): State {
    switch(action.type) {
      case 'user/clear-registration-form':
          return Object.assign({}, state, { userFormData: emptyUserFormData });
      default:
          return super.handleFormUpdate(state, action);
    }
  }
}
