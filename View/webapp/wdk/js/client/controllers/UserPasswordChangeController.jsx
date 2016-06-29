import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import ChangePasswordForm from '../components/ChangePasswordForm';
import { updateChangePasswordForm, savePassword } from '../actioncreators/UserActionCreators';

class UserPasswordChangeController extends WdkViewController {

  getStoreName() {
    return "UserPasswordChangeStore";
  }

  getActionCreators() {
    return { updateChangePasswordForm, savePassword };
  }

  isRenderDataLoaded(state) {
    return (state.user != null);
  }

  getTitle() {
    return "Change Password";
  }

  renderView(state, eventHandlers) {
    return ( <ChangePasswordForm {...state} userEvents={eventHandlers}/> );
  }
}

export default wrappable(UserPasswordChangeController);
