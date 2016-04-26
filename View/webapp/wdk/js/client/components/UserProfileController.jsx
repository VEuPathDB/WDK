import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import UserProfile from './UserProfile';
import { loadCurrentUser, updateProfile, saveProfile } from '../actioncreators/UserActionCreator';

const APPLICATION_SPECIFIC_PROPERTIES = "applicationSpecificProperties";

let UserProfileController = React.createClass({

  componentWillMount() {

    // get store used in this view controller
    this.userStore = this.props.stores.UserStore;
    this.profileViewStore = this.props.stores.ProfileViewStore;

    // subscribe to changes in user store
    this.userStoreSubscription = this.userStore.addListener(() => {
      this.setState(this.userStore.getState());
    });
    this.profileViewStoreSubscription = this.profileViewStore.addListener(() => {
      this.setState(this.profileViewStore.getState());
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
    this.profileViewStore.remove();
  },

  userEvents: {
    onFormStateChange: function(newState) {
      this.props.dispatchAction(updateProfile(newState));
    },
    onSaveProfile: function(user) {
      this.props.dispatchAction(saveProfile(this.state.user, this.errorHandler));
    }
  },

  render() {
    let title = "User Account";
    if (this.state.user == null || this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }
    return ( <Doc title={title}><UserProfile {...this.state} userEvents={this.userEvents}/></Doc> );
  }
});

export default wrappable(UserProfileController);
