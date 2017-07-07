import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
//import UserRegistration from '../components/UserProfile';
import { updateProfileForm, submitProfileForm } from '../actioncreators/UserActionCreators';

class UserMessageController extends WdkViewController {

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
    return "User Message";
  }

  renderView(state, eventHandlers) {
    return ( <div>User message page</div> );
    //return ( <UserRegistration {...state} userEvents={eventHandlers}/> );
  }
}

export default wrappable(UserMessageController);
