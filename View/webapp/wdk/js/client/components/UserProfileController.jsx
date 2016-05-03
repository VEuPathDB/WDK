import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import UserProfile from './UserProfile';
import { loadCurrentUser, updateProfile, saveProfile } from '../actioncreators/UserActionCreator';

const APPLICATION_SPECIFIC_PROPERTIES = "applicationSpecificProperties";

let UserProfileController = React.createClass({

  /**
   * On mount, sets up listeners to the store holding user DB data and to the store holding as yet unsaved modifications.
   * Sets up initial user data and binds the functions used trigger action creator actions.
   */
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

  /**
   * Calls an action creator to start the loading of the user if the user is not already loaded.
   */
  componentDidMount() {
    // load user if not yet present
    if (this.state.user == null) {
      this.props.dispatchAction(loadCurrentUser());
    }
  },

  /** On unmount, stop listening to the stores. */
  componentWillUnmount() {
    this.userStoreSubscription.remove();
    this.profileViewStore.remove();
  },

  /** Hash that holds the functions that trigger appropriate action creator actions */
  userEvents: {

    /**
     * Called when a form entry has changed
     * @param newState - the new state of the user with the form entry change(s) included.
     */
    onFormStateChange: function(newState) {
      this.props.dispatchAction(updateProfile(newState));
    },

    /**
     * Called when a form submit button (or a return) is pressed.
     * @param user - the user data to be saved.
     */
    onSaveProfile: function(user) {
      this.props.dispatchAction(saveProfile(this.state.user, this.errorHandler));
    }
  },

  /**
   * Top level render of the User Profile/Account.  Shows loading gif while user data is retrieved and
   * renders the UserProfile component once the user data is available.
   * @returns {XML}
   */
  render() {
    let title = "User Account";
    if (this.state.user == null || this.state.isLoading) {
      return ( <Doc title={title}><Loading/></Doc> );
    }
    return ( <Doc title={title}><UserProfile {...this.state} userEvents={this.userEvents}/></Doc> );
  }
});


export default wrappable(UserProfileController);
