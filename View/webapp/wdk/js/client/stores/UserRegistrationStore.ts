import UserProfileStore, { State } from './UserProfileStore';
import { User } from '../utils/WdkUser';
import { BaseState } from './WdkStore';

export default class UserRegistrationStore extends UserProfileStore {

  // defines the structure of this store's data
  getInitialState(): State {
    return {
      ...super.getInitialState(),
      userFormData: {
        id: 0,
        email: '',
        isGuest: true,
        properties: { }
      },
      formStatus: "new",  // Values: [ 'new', 'modified', 'pending', 'success', 'error' ]
      errorMessage: undefined
    };
  }
}
