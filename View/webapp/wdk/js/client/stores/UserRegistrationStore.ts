import UserProfileStore, { State, Action } from './UserProfileStore';
import { ClearRegistrationFormAction } from '../actioncreators/UserActionCreators';
import { User } from '../utils/WdkUser';

type RegistrationAction = Action | ClearRegistrationFormAction;

export default class UserRegistrationStore extends UserProfileStore {

  // defines the structure of this store's data
  getInitialState(): State {
    return {
      ...super.getInitialState(),
      userFormData: {
        id: 0,
        email: '',
        isGuest: true,
        properties: { },
        confirmEmail: '',
        preferences: { }
      },
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }


  handleAction(state: State, action: RegistrationAction): State {
    switch(action.type) {
      case 'user/clear-registration-form':
          return this.getInitialState();
      default:
          return super.handleFormUpdate(state, action);
    }
  }
}
