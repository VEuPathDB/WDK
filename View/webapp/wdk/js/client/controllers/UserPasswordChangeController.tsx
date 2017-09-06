import * as React from 'react';
import { get, omit } from 'lodash';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import ChangePasswordForm from '../components/ChangePasswordForm';
import { updateChangePasswordForm, savePassword } from '../actioncreators/UserActionCreators';
import UserPasswordChangeStore, { State as StoreState } from "../stores/UserPasswordChangeStore";

const ActionCreators = { updateChangePasswordForm, savePassword };

type State = Pick<StoreState, 'formStatus' | 'errorMessage' | 'passwordForm'>
           & Pick<StoreState['globalData'], 'user'>;

class UserPasswordChangeController extends AbstractViewController<State, UserPasswordChangeStore, typeof ActionCreators> {

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
