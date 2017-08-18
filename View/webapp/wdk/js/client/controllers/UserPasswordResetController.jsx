import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import UserPasswordReset from '../components/UserPasswordReset';
import {
  updatePasswordResetEmail,
  submitPasswordReset,
  conditionallyTransition
} from '../actioncreators/UserActionCreators';

class UserPasswordResetController extends WdkViewController {

  getStoreName() {
    return "UserPasswordResetStore";
  }

  getActionCreators() {
    return { updatePasswordResetEmail, submitPasswordReset, conditionallyTransition };
  }

  getTitle() {
    return "Reset Password";
  }

  isRenderDataLoaded(state) {
    // show Loading if no user loaded yet, or if user is guest
    //   (will transition to Profile page in loadData() if non-guest)
    return (state.globalData.user != null && state.globalData.user.isGuest);
  }

  renderView(state, eventHandlers) {
    return ( <UserPasswordReset {...state} {...eventHandlers} /> );
  }

  loadData(actionCreators) {
    actionCreators.conditionallyTransition(user => !user.isGuest, '/user/profile');
  }
}

export default wrappable(UserPasswordResetController);
