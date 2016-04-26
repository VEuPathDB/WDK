import React from 'react';
import { PropTypes } from 'react';
import UserAccountForm from './UserAccountForm';
import { wrappable, getChangeHandler } from '../utils/componentUtils';


/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
let UserProfile = React.createClass({

  render() {

    // Before the form is modified, a 'fake' user property, confirmEmail, is created to populate the retype email input element.
    this.props.isChanged ? () => {} : this.props.user.confirmEmail = this.props.user.email;

    // Only success and error outcomes are acted upon.  Any other outcome is assumed not to be associated with a save attempt.
    let saveAttempt = this.props.outcome === "error" || this.props.outcome === "success";

    // Banner L&F governed by outcome above.  Banner will not appear if no save attempt was made
    let messageClass = this.props.outcome === "success" ? "wdk-UserProfile-banner wdk-UserProfile-success" : "wdk-UserProfile-banner wdk-UserProfile-error";

    return (
      <div style={{ margin: "0 2em"}}>
        {this.props.user !== null && !this.props.user.isGuest ?
          <div>
            <h1>My Account</h1>
            {saveAttempt ? <p className={messageClass}>{this.props.message}</p> : ""}
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


  /**
   * Verifies that the email and the re-typed email match.  HTML5 validation doesn't handle this OOTB.
   * @param newState - new user state
   */
  validateEmailConfirmation(newState) {
    let userEmail = newState.email;
    let confirmUserEmail = newState.confirmEmail;
    if(userEmail != null  && confirmUserEmail != null) {
      let confirmUserEmailElement = document.getElementById("confirmUserEmail");
      userEmail !== confirmUserEmail ? confirmUserEmailElement.setCustomValidity("Both email entries must match.") : confirmUserEmailElement.setCustomValidity("");
    }
  },

  /**
   * Callback issued by the TextBox React component when either email input is modified.  Unlike the input to a text input, this modification needs an extra
   * validation step to insure that the email and it's re-typed version are identical.
   * @param newState
   */
  onEmailUpdate(newState) {
    this.validateEmailConfirmation(newState);
    this.props.userEvents.onFormStateChange(newState);
  },

  /**
   * Triggered by onChange handler of TextBox of type email.  Only different from handler of TextBox of type text because
   * of an extra validation step.  Returns user with state change incorporated.
   * @param field - name of user attribute being changed
   * @returns {*}
   */
  onEmailChange(field) {
    return getChangeHandler(field, this.onEmailUpdate, this.props.user)
  },

  /**
   * Triggered by onChange handler of TextBOx of type text.  Returns user with state change incorporated.
   * @param field - name of user attribute being changed
   * @returns {*}
   */
  onTextChange(field) {
    return getChangeHandler(field, this.props.userEvents.onFormStateChange, this.props.user);
  },

  /**
   * Triggered by onSubmit handler of the user profile/account form.  Verifies again that the email and re-typed version match. Then
   * checks the validity of all other inputs using HTML5 validity methods.  If all verifications pass, the re-typed email attribute is
   * removed from the user object (as it was only introduced as a check of user typing) and the user object is saved.
   * @param event
   */
  saveProfile(event) {
    event.preventDefault();
    this.validateEmailConfirmation(this.props.user);
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


UserProfile.propTypes = {

  /** The user object to be modified */
  user:  PropTypes.object.isRequired,

  /** Hash holding the functions that trigger corresponding action creator actions */
  userEvents:  PropTypes.shape({

    /** Called with a parameter representing the new state when a form element changes */
    onFormStateChange:  PropTypes.func.isRequired,

    /** Called with a parameter representing the user data to be saved */
    onSaveProfile:  PropTypes.func.isRequired
  }),

  /** Indicates that unsaved modifications currently exist */
  isChanged:  PropTypes.bool.isRequired,

  /**
   *  Indicates the outcome of the user's save attempt.  A banner with a green background appears for 'success' and
   *  a banner with a red background appears for 'error'.  Any other value will produce no banner.
   */
  outcome: PropTypes.string,

  /**
   * Message to the user explaining the outcome of the user's save attempt.  THe message will appear in the banner
   * at the top of the form only if the outcome is neither 'success' nor 'error'.
   */
  message: PropTypes.string
}


export default wrappable(UserProfile);