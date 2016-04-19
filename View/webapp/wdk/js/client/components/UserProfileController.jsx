import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import UserProfile from './UserProfile';
import { loadCurrentUser, editProfile, updateProfile } from '../actioncreators/UserActionCreator';

let UserProfileController = React.createClass({

  componentWillMount() {

    // get store used in this view controller
    this.userStore = this.props.stores.UserStore;

    // subscribe to changes in user store
    this.userStoreSubscription = this.userStore.addListener(() => {
      this.setState(this.userStore.getState());
    });

    // get current user store state
    this.setState(this.userStore.getState());
    for (let key in this.userEvents) {
      this.userEvents[key] = this.userEvents[key].bind(this);
    }
  },

  componentDidMount() {
    // load user if not yet present
    if (this.state.user == null) {
      this.props.dispatchAction(loadCurrentUser());
    }
  },

  componentWillUnmount() {
    this.userStoreSubscription.remove();
  },

  userEvents: {
    onEditProfile: function(user) {
      this.props.dispatchAction(editProfile(user));
    },
    onFormStateChange: function(newState) {
      this.setState({user: newState});
      Object.keys(newState).forEach(key => {
        console.log(key + " => " + newState[key]);
      });
    },
    onUpdateProfile: function(user) {
      this.props.dispatchAction(updateProfile(user));
    },
    onCancelEdit: function(user) {
      this.props.dispatchAction(loadCurrentUser());
    }
  },

  render() {
    let title = "User Profile";
    if (this.state.user == null || this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }
    return ( <Doc title={title}><UserProfile {...this.state} userEvents={this.userEvents}/></Doc> );
  }
});

export default wrappable(UserProfileController);
