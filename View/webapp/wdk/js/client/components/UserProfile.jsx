import React from 'react';
import { PropTypes } from 'react';
import UserAccountForm from './UserAccountForm';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

export function interpretFormStatus(formStatus, errorMessage) {
  // configure properties for banner and submit button enabling based on status
  let messageClass = "wdk-UserProfile-banner ", message = "", disableSubmit = false;
  switch (formStatus) {
    case 'new':
      disableSubmit = true;
      break;
    case 'modified':
      message = "*** You have unsaved changes ***";
      messageClass += "wdk-UserProfile-modified";
      break;
    case 'pending':
      message = "Saving changes...";
      messageClass += "wdk-UserProfile-pending";
      disableSubmit = true;
      break;
    case 'success':
      message = "Your changes have been successfully saved.";
      messageClass += "wdk-UserProfile-success";
      disableSubmit = true; // same as 'new'
      break;
    case 'error':
      message = errorMessage;
      messageClass += "wdk-UserProfile-error";
  }
  return { messageClass, message, disableSubmit };
}

export function FormMessage({ message, messageClass }) {
  return ( message == '' ? <noscript/> :
    <div className={messageClass}><span>{message}</span></div> );
}

const containerStyle = {
  width: '0 2em'
};

/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
class UserProfile extends React.Component {

  constructor(props) {
    super(props);
    this.onEmailChange = this.onEmailChange.bind(this);
    this.onConfirmEmailChange = this.onConfirmEmailChange.bind(this);
    this.onEmailFieldChange = this.onEmailFieldChange.bind(this);
    this.onPropertyChange = this.onPropertyChange.bind(this);
    this.saveProfile = this.saveProfile.bind(this);
  }

  render() {
    let formConfig = interpretFormStatus(this.props.formStatus, this.props.errorMessage);
    return (
      <div style={containerStyle}>
        {this.props.userFormData.isGuest ?
          <div>You must first log on to read and alter your account information</div> :
          <div>
            <h1>My Account</h1>
            <FormMessage {...formConfig}/>
            <UserAccountForm user={this.props.userFormData}
                             disableSubmit={formConfig.disableSubmit}
                             onEmailChange={this.onEmailChange}
                             onConfirmEmailChange={this.onConfirmEmailChange}
                             onPropertyChange={this.onPropertyChange}
                             onPreferenceChange={this.onPreferenceChange}
                             onFormStateChange={this.props.userEvents.updateProfileForm}
                             saveProfile={this.saveProfile}
                             wdkConfig={this.props.globalData.config}/>
          </div>
        }
      </div>
    );
  }

  /**
   * Verifies that the email and the re-typed email match.  HTML5 validation doesn't handle this OOTB.
   * @param newState - new user state
   */
  validateEmailConfirmation(newState) {
    let userEmail = newState.email;
    let confirmUserEmail = newState.confirmEmail;
    if (userEmail != null  && confirmUserEmail != null) {
      let confirmUserEmailElement = document.getElementById("confirmUserEmail");
      userEmail !== confirmUserEmail ? confirmUserEmailElement.setCustomValidity("Both email entries must match.") : confirmUserEmailElement.setCustomValidity("");
    }
  }

  /**
   * Dynamically creates a change handler with the 
   * @param {string} field
   * @param {string} newValue
   */
  onEmailFieldChange(field, newValue) {
    // create function to do validation and call form action creator
    let updater = newState => {
        this.validateEmailConfirmation(newState);
        this.props.userEvents.updateProfileForm(newState);
    };
    // create change handler for email field requested
    let handler = getChangeHandler(field, updater, this.props.userFormData);
    // call it with new value
    handler(newValue);
  }

  /**
   * Triggered by onChange handler of email TextBox.  Provides extra validation
   * step comparing email and confirmEmail.  Calls event to update form.
   * @param newValue - new value of email field
   * @returns {*}
   */
  onEmailChange(newValue) {
    this.onEmailFieldChange('email', newValue);
  }

  /**
   * Triggered by onChange handler of confirmEmail TextBox.  Provides extra validation
   * step comparing email and confirmEmail.  Calls event to update form.
   * @param newValue - new value of email field
   * @returns {*}
   */
  onConfirmEmailChange(newValue) {
    this.onEmailFieldChange('confirmEmail', newValue);
  }

  /**
   * Triggered by onChange handler of TextBox of type text.  Returns user with state change incorporated.
   * @param field - name of user attribute being changed
   * @returns {*}
   */
  onPropertyChange(field) {
    let update = this.props.userEvents.updateProfileForm;
    let previousState = this.props.userFormData;
    return newValue => {
      let newProps = Object.assign({}, previousState.properties, { [field]: newValue });
      update(Object.assign({}, previousState, { properties: newProps }));
    };
  }

  /**
   * Triggered by onSubmit handler of the user profile/account form.  Verifies again that the email and re-typed version match. Then
   * checks the validity of all other inputs using HTML5 validity methods.  If all verifications pass, the re-typed email attribute is
   * removed from the user object (as it was only introduced as a check of user typing) and the user object is saved.
   * @param event
   */
  saveProfile(event) {
    event.preventDefault();
    this.validateEmailConfirmation(this.props.userFormData);
    let inputs = document.querySelectorAll("input[type=text],input[type=email]");
    let valid = true;
    for(let input of inputs) {
      if(!input.checkValidity()) {
        valid = false;
        break;
      }
    }
    if(valid) {
      this.props.userEvents.submitProfileForm(this.props.userFormData);
    }
  }

}

UserProfile.propTypes = {

  /** WDK config object */
  globalData: PropTypes.shape({ config: PropTypes.object.isRequired }),

  /** The user object to be modified */
  userFormData:  PropTypes.object.isRequired,

  /**
   *  Indicates the current status of form.  Display may change based on status.
   *  Acceptable values are: [ 'new', 'modified', 'pending', 'success', 'error' ]
   */
  formStatus: PropTypes.string,

  /**
   * Message to the user explaining an error outcome of the user's save attempt.
   */
  errorMessage: PropTypes.string,

  /** Hash holding the functions that trigger corresponding action creator actions */
  userEvents:  PropTypes.shape({

    /** Called with a parameter representing the new state when a form element changes */
    updateProfileForm:  PropTypes.func.isRequired,

    /** Called with a parameter representing the user data to be saved */
    submitProfileForm:  PropTypes.func.isRequired
  })
};

export default wrappable(UserProfile);
