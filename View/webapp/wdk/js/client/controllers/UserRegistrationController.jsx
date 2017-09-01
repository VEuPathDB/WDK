import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import UserRegistration from '../components/UserRegistration';
import { updateProfileForm, submitRegistrationForm, conditionallyTransition } from '../actioncreators/UserActionCreators';

class UserRegistrationController extends WdkViewController {

  getStoreName() {
    return "UserRegistrationStore";
  }

  getActionCreators() {
    return { updateProfileForm, submitRegistrationForm, conditionallyTransition };
  }

  isRenderDataLoaded(state) {
    return (state.userFormData != null &&
            state.userFormData.preferences != null &&
            state.globalData.config != null &&
            // show Loading if user is guest
            //   (will transition to Profile page in loadData() if non-guest)
            state.globalData.user != null &&
            state.globalData.user.isGuest);
  }

  getTitle() {
    return "Register";
  }

  renderView(state, eventHandlers) {
    return ( <UserRegistration {...state} userEvents={eventHandlers}/> );
  }

  loadData(actionCreators) {
    actionCreators.conditionallyTransition(user => !user.isGuest, '/user/profile');
  }
}

export default wrappable(UserRegistrationController);
