import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import UserProfile from '../components/UserProfile';
import { updateProfileForm, submitProfileForm } from '../actioncreators/UserActionCreators';

class UserProfileController extends WdkViewController {

  getStoreName() {
    return "UserProfileStore";
  }

  getActionCreators() {
    return { updateProfileForm, submitProfileForm };
  }

  isRenderDataLoaded() {
    return (this.state.userFormData != null && this.state.globalData.config != null);
  }

  getTitle() {
    return "User Account";
  }

  renderView(state, eventHandlers) {
    return ( <UserProfile {...state} userEvents={eventHandlers}/> );
  }
}

export default wrappable(UserProfileController);
