import * as React from 'react';
import { wrappable } from 'Utils/ComponentUtils';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import UserProfile from 'Views/User/Profile/UserProfile';
import { updateProfileForm, submitProfileForm } from 'Core/ActionCreators/UserActionCreators';
import UserProfileStore, { State } from "Views/User/Profile/UserProfileStore";

const ActionCreators = { updateProfileForm, submitProfileForm };

class UserProfileController extends AbstractPageController<State, UserProfileStore, typeof ActionCreators> {

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
