import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import UserPasswordReset from '../components/UserPasswordReset';
import { updatePasswordResetEmail, submitPasswordReset } from '../actioncreators/UserActionCreators';

class UserPasswordResetController extends WdkViewController {

  getStoreName() {
    return "UserPasswordResetStore";
  }

  getActionCreators() {
    return { updatePasswordResetEmail, submitPasswordReset };
  }

  getTitle() {
    return "Reset Password";
  }

  renderView(state, eventHandlers) {
    return ( <UserPasswordReset {...state} {...eventHandlers} /> );
  }
}

export default wrappable(UserPasswordResetController);
