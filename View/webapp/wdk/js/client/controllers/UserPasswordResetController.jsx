import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
//import UserRegistration from '../components/UserProfile';
import { updateProfileForm, submitProfileForm } from '../actioncreators/UserActionCreators';

class UserPasswordResetController extends WdkViewController {

  //getStoreName() {
  //  return "UserRegistrationStore";
  //}

  getActionCreators() {
    return { updateProfileForm, submitProfileForm };
  }

  isRenderDataLoaded() {
    return true;
    //return (this.state.userFormData != null && this.state.globalData.config != null);
  }

  getTitle() {
    return "Reset Password";
  }

  renderView(state, eventHandlers) {
    return ( <div>Password reset page</div> );
    //return ( <UserRegistration {...state} userEvents={eventHandlers}/> );
  }
}

export default wrappable(UserPasswordResetController);
