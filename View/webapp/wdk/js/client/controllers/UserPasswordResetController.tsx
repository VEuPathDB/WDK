import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import UserPasswordReset from '../components/UserPasswordReset';
import {
  updatePasswordResetEmail,
  submitPasswordReset,
  conditionallyTransition
} from '../actioncreators/UserActionCreators';
import UserPasswordResetStore, { State } from "../stores/UserPasswordResetStore";

const ActionCreators = {
  updatePasswordResetEmail,
  submitPasswordReset,
  conditionallyTransition
}

class UserPasswordResetController extends AbstractViewController<State, UserPasswordResetStore, typeof ActionCreators> {

  getStoreClass() {
    return UserPasswordResetStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  getActionCreators() {
    return ActionCreators;
  }

  getTitle() {
    return "Reset Password";
  }

  isRenderDataLoaded() {
    // show Loading if no user loaded yet, or if user is guest
    //   (will transition to Profile page in loadData() if non-guest)
    return (this.state.globalData.user != null && this.state.globalData.user.isGuest);
  }

  renderView() {
    return ( <UserPasswordReset {...this.state} {...this.eventHandlers} /> );
  }

  loadData() {
    this.eventHandlers.conditionallyTransition(user => !user.isGuest, '/user/profile');
  }
}

export default wrappable(UserPasswordResetController);
