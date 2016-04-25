import React from 'react';
import TextBox from './TextBox';
import TextArea from './TextArea';
import CheckboxList from './CheckboxList';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

/**
 * React component for the user profile page/form
 * @type {*|Function}
 */
let UserProfile = React.createClass({

  render() {

    /**
     * Provides hardcode relationships between user email preferences and the display labels in the order the data
     * should be displayed.
     * @type {*[]}
     */
    //TODO this will go in ApiCommonWebsite
    let emailPreferenceData = [{value:'preference_global_email_amoebadb', display:'AmoebaDB'},
                               {value:'preference_global_email_cryptodb', display:'CryptoDB'},
                               {value:'preference_global_email_apidb', display:'EuPathDB'},
                               {value:'preference_global_email_fungidb', display:'FungiDB'},
                               {value:'preference_global_email_giardiadb', display:'GiardiaDB'},
                               {value:'preference_global_email_microsporidiadb', display:'MicrosporidiaDB'},
                               {value:'preference_global_email_piroplasmadb', display:'PiroplasmaDB'},
                               {value:'preference_global_email_plasmodb', display:'PlasmoDB'},
                               {value:'preference_global_email_schistodb', display:'SchistoDB'},
                               {value:'preference_global_email_toxodb', display:'ToxoDB'},
                               {value:'preference_global_email_trichdb', display:'TrichDB'},
                               {value:'preference_global_email_tritrypdb', display:'TriTrypDB'}];
    let properties = toNamedMap(Object.keys(this.props.user.applicationSpecificProperties), this.props.user.applicationSpecificProperties);
    let emailPreferenceSelections = properties.filter(property => property.name.startsWith('preference_global_email_')).map(property => property.name);
    this.props.isChanged ? () => {} : this.props.user.confirmEmail = this.props.user.email;
    let messageClass = this.props.outcome === "error" ? "wdk-UserProfile-banner wdk-UserProfile-error" :
      this.props.outcome === "success" ? "wdk-UserProfile-banner wdk-UserProfile-success" : "wdk-UserProfile-banner";

    return (
      <div style={{ margin: "0 2em"}}>
        {this.props.user !== null && !this.props.user.isGuest ?
          <div>
            <h1>My Account</h1>
            {this.props.outcome.length > 0 ? <p className={messageClass}>{this.props.message}</p> : ""}
            {userForm(this.props.user, emailPreferenceData, emailPreferenceSelections, this.onEmailChange,
                      this.props.userEvents.onFormStateChange, this.props.userEvents.onEmailPreferenceChange,
                      this.props.isChanged, this.saveProfile)}
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

  onEmailChange(newState) {
    this.validateEmailConfirmation();
    this.props.userEvents.onFormStateChange(newState);
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

function displayEmailPreferences(emailPreferenceSelections, emailPreferenceData) {
  let filteredEmailPreferenceData = emailPreferenceData.filter(item => emailPreferenceSelections.indexOf(item.value ) > -1);
  return (
    <ul className="wdk-UserProfile-propertyData">
      { filteredEmailPreferenceData.map(item => ( <li key={item.value}>{item.display}</li> )) }
    </ul>
  )
}

function toNamedMap(keys, object) {
  return keys.map(key => ({ name: key, value: object[key] }));
}

function userForm(user, emailPreferenceData, emailPreferenceSelections, onEmailChange, onFormStateChange, onEmailPreferenceChange, isChanged, saveProfile) {
  return(
    <form className="wdk-UserProfile-profileForm" name="userProfileForm" onSubmit={saveProfile} >
      <p><i className="fa fa-asterisk"></i> = required</p>
      <fieldset>
        <legend>Identification</legend>
        <div>
          <label htmlFor="userEmail"><i className="fa fa-asterisk"></i>Email:</label>
          <TextBox type='email' id='userEmail'
                   value={user.email} onChange={getChangeHandler('email', onEmailChange, user)}
                   maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <label htmlFor="confirmUserEmail"><i className="fa fa-asterisk"></i>Retype Email:</label>
          <TextBox type='email' id='confirmUserEmail'
                   value={user.confirmEmail} onChange={getChangeHandler('confirmEmail', onEmailChange, user)}
                   maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <label htmlFor="firstName"><i className="fa fa-asterisk"></i>First Name:</label>
          <TextBox id="firstName" value={user.firstName} onChange={getChangeHandler('firstName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <label htmlFor="middleName">Middle Name:</label>
          <TextBox id="middleName" value={user.middleName} onChange={getChangeHandler('middleName', onFormStateChange, user)} maxLength='50' size='25'/>
        </div>
        <div>
          <label htmlFor="lastName"><i className="fa fa-asterisk"></i>Last Name:</label>
          <TextBox id="lastName" value={user.lastName} onChange={getChangeHandler('lastName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <label htmlFor="title">Title:</label>
          <TextBox id="title" value={user.title} onChange={getChangeHandler('title', onFormStateChange, user)} maxLength='50' size='25' />
        </div>
        <div>
          <label htmlFor="department">Department:</label>
          <TextBox id="department" value={user.department} onChange={getChangeHandler('department', onFormStateChange, user)} maxLength='50' size='25' />
        </div>
        <div>
          <label htmlFor="organization"><i className="fa fa-asterisk"></i>Organization:</label>
          <TextBox id="organization" value={user.organization} onChange={getChangeHandler('organization', onFormStateChange, user)} maxLength='255' size='100' required />
        </div>
      </fieldset>
      <br />
      <fieldset>
        <legend>Password</legend>
        <div>
          <a href="#">Change your password</a>
        </div>
      </fieldset>
      <br />
      <fieldset>
        <legend>Contact Info</legend>
        <div>
          <label htmlFor="streetAddress">Street Address:</label>
          <TextArea id="streetAddress" value={user.address} onChange={getChangeHandler('address', onFormStateChange, user)} maxLength='500' cols='100' rows="3"/>
        </div>
        <div>
          <label htmlFor="city">City:</label>
          <TextBox id="city" value={user.city} onChange={getChangeHandler('city', onFormStateChange, user)} maxLength='255' size='100'/>
        </div>
        <div>
          <label htmlFor="state">State/Province:</label>
          <TextBox id="state" value={user.state} onChange={getChangeHandler('state', onFormStateChange, user)} maxLength='255' size='100'/>
        </div>
        <div>
          <label htmlFor="country">Country:</label>
          <TextBox id="country" value={user.country} onChange={getChangeHandler('country', onFormStateChange, user)} maxLength='255' size='100'/>
        </div>
        <div>
          <label htmlFor="zipCode">Postal Code:</label>
          <TextBox id="zipCode" value={user.zipCode} onChange={getChangeHandler('zipCode', onFormStateChange, user)} maxLength='20' size='10'/>
        </div>
        <div>
          <label htmlFor="phoneNumber">Phone Number:</label>
          <TextBox id="phoneNumber" value={user.phoneNumber} onChange={getChangeHandler('phoneNumber', onFormStateChange, user)} maxLength='50' size='25'/>
        </div>
      </fieldset>
      <br />
      <fieldset>
        <legend>Preferences</legend>
        <p>Send me email alerts about:</p>
        <CheckboxList name="emailAlerts" items={emailPreferenceData} value={emailPreferenceSelections} onChange={onEmailPreferenceChange} />
      </fieldset>
      <div>
        <input type="submit" value="Save" disabled={isChanged ? "" : "disabled"} />
      </div>
    </form>
  );
}

export default wrappable(UserProfile);