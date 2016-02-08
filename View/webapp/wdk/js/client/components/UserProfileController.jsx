import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import UserProfile from './UserProfile';

let UserProfileController = React.createClass({

  componentWillMount() {

    // get actions and store used in this view controller
    this.userActions = this.props.actionCreators.UserActionCreator;
    this.userStore = this.props.stores.UserStore;

    // subscribe to changes in user store
    this.userStoreSubscription = this.userStore.addListener(() => {
      this.setState(this.userStore.getState());
    });

    // get current user store state
    this.setState(this.userStore.getState());
  },

  componentDidMount() {
    // load user if not yet present
    if (this.state.user == null) {
      this.userActions.loadCurrentUser();
    }
  },

  componentWillUnmount() {
    this.userStoreSubscription.remove();
  },

  render() {
    let title = "User Profile";
    if (this.state.user == null || this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }
    return ( <Doc title={title}><UserProfile {...this.state}/></Doc> );
  }
});

export default wrappable(UserProfileController);
