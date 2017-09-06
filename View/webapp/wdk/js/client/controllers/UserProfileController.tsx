import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import UserProfile from '../components/UserProfile';
import { updateProfileForm, submitProfileForm } from '../actioncreators/UserActionCreators';
import UserProfileStore, { State } from "../stores/UserProfileStore";

const ActionCreators = { updateProfileForm, submitProfileForm };

class UserProfileController extends AbstractViewController<State, UserProfileStore, typeof ActionCreators> {

  getStoreClass() {
    return UserProfileStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  getActionCreators() {
    return { updateProfileForm, submitProfileForm };
  }

  isRenderDataLoaded() {
    return (this.state.userFormData != null &&
            this.state.userFormData.preferences != null &&
            this.state.globalData.config != null);
  }

  getTitle() {
    return "User Account";
  }

  renderView() {
    return ( <UserProfile {...this.state} userEvents={this.eventHandlers}/> );
  }
}

export default wrappable(UserProfileController);
