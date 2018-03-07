import * as React from 'react';
import { get, omit } from 'lodash';
import { wrappable } from 'Utils/ComponentUtils';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import ChangePasswordForm from 'Views/User/Password/ChangePasswordForm';
import { updateChangePasswordForm, savePassword } from 'Core/ActionCreators/UserActionCreators';
import UserPasswordChangeStore, { State as StoreState } from "Views/User/Password/UserPasswordChangeStore";

const ActionCreators = { updateChangePasswordForm, savePassword };

type State = Pick<StoreState, 'formStatus' | 'errorMessage' | 'passwordForm'>
           & Pick<StoreState['globalData'], 'user'>;

class UserPasswordChangeController extends AbstractPageController<State, UserPasswordChangeStore, typeof ActionCreators> {

  getStoreClass() {
    return UserPasswordChangeStore;
  }

  getStateFromStore() {
    return {
      ...omit(this.store.getState(), 'globalData'),
      user: get(this.store.getState(), 'globalData.user')
    } as State;
  }

  getActionCreators() {
    return ActionCreators;
  }

  isRenderDataLoaded() {
    return (this.state.user != null);
  }

  getTitle() {
    return "Change Password";
  }

  renderView() {
    return ( <ChangePasswordForm {...this.state} userEvents={this.eventHandlers}/> );
  }
}

export default wrappable(UserPasswordChangeController);
