import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import UserProfile from './UserProfile';
import { loadCurrentUser, editProfile, updateProfile, saveProfile } from '../actioncreators/UserActionCreator';

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
    onEditProfile: function(user) {
      this.props.dispatchAction(editProfile(user));
    },
    onFormStateChange: function(newState) {
      this.props.dispatchAction(updateProfile(newState));
    },
    onEmailPreferenceChange: function(newPreferences) {
      // Remove email preference properties
      let properties = this.state.user.properties;
      Object.keys(properties).forEach(function (key) {
        if(key.startsWith('preference_global_email_')) delete properties[key];
      });
      // Replace with new email preferences
      let newProperties = newPreferences.reduce((currentPreferences, newPreference) => Object.assign(currentPreferences, {[newPreference]: "on"}), properties);
      this.setState(Object.assign({}, this.state.user, {properties : newProperties}));
      this.props.dispatchAction(updateProfile(this.state.user));
    },
    onSaveProfile: function(user) {
      this.props.dispatchAction(saveProfile(this.state.user));
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
