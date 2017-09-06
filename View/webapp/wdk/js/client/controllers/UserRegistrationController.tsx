import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import UserRegistration from '../components/UserRegistration';
import { updateProfileForm, submitRegistrationForm, conditionallyTransition } from '../actioncreators/UserActionCreators';
import UserRegistrationStore, { State } from "../stores/UserRegistrationStore";

const ActionCreators = { updateProfileForm, submitRegistrationForm, conditionallyTransition };

class UserRegistrationController extends AbstractViewController<State, UserRegistrationStore, typeof ActionCreators> {

  getStoreClass() {
    return UserRegistrationStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  getActionCreators() {
    return { updateProfileForm, submitRegistrationForm, conditionallyTransition };
  }

  isRenderDataLoaded() {
    return (this.state.userFormData != null &&
            this.state.userFormData.preferences != null &&
            this.state.globalData.config != null &&
            // show Loading if user is guest
            //   (will transition to Profile page in loadData() if non-guest)
            this.state.globalData.user != null &&
            this.state.globalData.user.isGuest);
  }

  getTitle() {
    return "Register";
  }

  renderView() {
    return ( <UserRegistration {...this.state} userEvents={this.eventHandlers}/> );
  }

  loadData() {
    this.eventHandlers.conditionallyTransition(user => !user.isGuest, '/user/profile');
  }
}

export default wrappable(UserRegistrationController);
