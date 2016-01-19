// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Loading from './Loading';
import UserProfile from './UserProfile';

let UserProfileController = React.createClass({

  componentWillMount() {

    this.store = this.props.stores.UserStore;
    this.actions = this.props.actionCreators.UserActionCreator;

    let state = this.store.getState();
    if (state.user == null) {
      this.actions.loadCurrentUser();
    }
    else {
      this.setState(this.store.getState());
    }

    this.storeSubscription = this.store.addListener(() => {
      this.setState(this.store.getState());
    });
  },

  componentWillUnmount() {
    this.storeSubscription.remove();
  },

  render() {
    if (this.state == null || this.state.isLoading) {
      return ( <Loading/> );
    }
    return ( <UserProfile {...this.state}/> );
  }
});

// Export the React Component class we just created.
export default wrappable(UserProfileController);
