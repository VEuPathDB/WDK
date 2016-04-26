import React from 'react';
import UserAccountForm from './UserAccountForm';
import { wrappable, getChangeHandler } from '../utils/componentUtils';


const APPLICATION_SPECIFIC_PROPERTIES = "applicationSpecificProperties";

/**
 * React component for the user profile page/form
 * @type {*|Function}
 */
let UserProfile = React.createClass({

  render() {
    this.props.isChanged ? () => {} : this.props.user.confirmEmail = this.props.user.email;
    let messageClass = this.props.outcome === "error" ? "wdk-UserProfile-banner wdk-UserProfile-error" :
      this.props.outcome === "success" ? "wdk-UserProfile-banner wdk-UserProfile-success" : "wdk-UserProfile-banner";

    return (
      <div style={{ margin: "0 2em"}}>
        {this.props.user !== null && !this.props.user.isGuest ?
          <div>
            <h1>My Account</h1>
            {this.props.outcome.length > 0 ? <p className={messageClass}>{this.props.message}</p> : ""}
            <UserAccountForm user={this.props.user}
                             onTextChange={this.onTextChange}
                             onEmailChange={this.onEmailChange}
                             onFormStateChange={this.props.userEvents.onFormStateChange}
                             isChanged={this.props.isChanged}
                             saveProfile={this.saveProfile} />
          </div>
        : <div>You must first log on to read and alter your account information</div>
        }
      </div>
    );
  },

  validateEmailConfirmation() {
    let userEmail = document.getElementById("userEmail");
    let confirmUserEmail = document.getElementById("confirmUserEmail");
    if(userEmail != null  && confirmUserEmail != null) {
      userEmail.value !== confirmUserEmail.value ? confirmUserEmail.setCustomValidity("Both email entries must match.") : confirmUserEmail.setCustomValidity("");
    }
  },

  onEmailUpdate(newState) {
    this.validateEmailConfirmation();
    this.props.userEvents.onFormStateChange(newState);
  },

  onEmailChange(field) {
    return getChangeHandler(field, this.onEmailUpdate, this.props.user)
  },
  
  onTextChange(field) {
    return getChangeHandler(field, this.props.userEvents.onFormStateChange, this.props.user);
  },

  saveProfile(event) {
    event.preventDefault();
    this.validateEmailConfirmation();
    let inputs = document.querySelectorAll("input[type=text],input[type=email]");
    let valid = true;
    for(let input of inputs) {
      if(!input.checkValidity()) {
        valid = false;
        break;
      }
    }
    if(valid) {
      delete this.props.user.confirmEmail;
      this.props.userEvents.onSaveProfile(this.props.user);
    }
  }

});

export default wrappable(UserProfile);