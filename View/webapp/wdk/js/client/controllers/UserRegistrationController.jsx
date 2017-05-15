import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import UserRegistration from '../components/UserRegistration';
import { updateProfileForm, submitRegistrationForm } from '../actioncreators/UserActionCreators';

class UserRegistrationController extends WdkViewController {

  getActionCreators() {
    return { updateProfileForm, submitRegistrationForm };
  }

  isRenderDataLoaded() {
    return (this.state.globalData.config != null &&
            this.state.globalData.preferences != null);
  }

  getTitle() {
    return "Register";
  }

  renderView(state, eventHandlers) {
    return ( <UserRegistration {...state} userEvents={eventHandlers}/> );
  }
}

export default wrappable(UserRegistrationController);
