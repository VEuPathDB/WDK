import React from 'react';
import TextBox from './TextBox';
import CheckboxList from './CheckboxList';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

/**
 * React component for the user profile page/form
 * @type {*|Function}
 */
let UserProfile = React.createClass({

  render() {

    /**
     * Provides hardcoded relationships between user profile data designations and display labels in the order the data
     * should be displayed.
     * @type {*[]}
     */
    let userKeyData = [{key:'email', label:'Email'},
                       {key:'firstName', label:"First Name"},
                       {key:'lastName', label:"Last Name"},
                       {key:'organization', label:'Organization'}];

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

    return (
      <div style={{ margin: "0 2em"}}>
        {this.props.user !== null && !this.props.user.isGuest ?
          this.props.isEdit ?
            <div>
              {userForm(this.props.user, emailPreferenceData, emailPreferenceSelections,
                        this.props.userEvents.onFormStateChange, this.props.userEvents.onEmailPreferenceChange,
                        this.saveProfile, this.cancelEdit)}
            </div>
            :
            <div>
              <div>
                <h1>Your Profile<i className="fa fa-pencil edit" onClick={this.editProfile}></i></h1>
                {userProfile(userKeyData, this.props.user)}
                <h2>Properties</h2>
                {tableOf(properties, true, "Name", "Value")}
              </div>
            </div>
        : <div>You must first log on to read and alter your profile</div>
        }
      </div>
    );
  },

  editProfile() {
    this.props.user.confirmEmail = this.props.user.email;
    this.props.userEvents.onEditProfile(this.props.user);
  },

  saveProfile(event) {
    console.log("Reached save");
    delete this.props.user.confirmEmail;
    event.preventDefault();
    this.props.userEvents.onSaveProfile(this.props.user);
  },
  
  cancelEdit() {
    this.props.userEvents.onCancelEdit(this.props.user);
  }

});

/**
 * Provides a tabular display of the user profile data using labels and data item order provided
 * @param keyData - provides label to be used for each data item
 * @param user - user containing profile data
 * @returns {XML} - tabular display of user profile data
 */
function userProfile(keyData, user) {
  return (
    <table className="wdk-UserProfile-profileData">
      <tbody>
      { keyData.map(item => ( <tr key={item.label}><td>{item.label}:</td><td>{user[item.key]}</td></tr> )) }
      </tbody>
    </table>
  );
}

function toNamedMap(keys, object) {
  return keys.map(key => ({ name: key, value: object[key] }));
}

function tableOf(objArray, addHeader, nameTitle, valueTitle) {
  return (
    <table>
      <tbody>
        { addHeader ? ( <tr><th>{nameTitle}</th><th>{valueTitle}</th></tr> ) : null }
        { objArray.map(val => ( <tr key={val.name}><td>{val.name}</td><td>{val.value}</td></tr> )) }
      </tbody>
    </table>
  );
}

function userForm(user, emailPreferenceData, emailPreferenceSelections, onFormStateChange, onEmailPreferenceChange, saveProfile, cancelEdit) {
  return(
    <form className="wdk-UserProfile-profileForm" name="userProfileForm" onSubmit={saveProfile} >
      <p><i className="fa fa-asterisk"></i> = required</p>
      <fieldset>
        <legend>Identification</legend>
        <div>
          <label><i className="fa fa-asterisk"></i>Email:</label>
          <TextBox type='email' value={user.email} onChange={getChangeHandler('email', onFormStateChange, user)} maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <label><i className="fa fa-asterisk"></i>Retype Email:</label>
          <TextBox type='email' value={user.confirmEmail} onChange={getChangeHandler('confirmEmail', onFormStateChange, user)} maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <label><i className="fa fa-asterisk"></i>First Name:</label>
          <TextBox value={user.firstName} onChange={getChangeHandler('firstName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <label><i className="fa fa-asterisk"></i>Last Name:</label>
          <TextBox value={user.lastName} onChange={getChangeHandler('lastName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <label><i className="fa fa-asterisk"></i>Organization:</label>
          <TextBox value={user.organization} onChange={getChangeHandler('organization', onFormStateChange, user)} maxLength='255' size='100' required />
        </div>
      </fieldset>
      <br />
      <fieldset>
        <legend>Address Info</legend>
      </fieldset>
      <br />
      <fieldset>
        <legend>Preferences</legend>
        <p>Send me email alerts about:</p>
        <CheckboxList name="emailAlerts" items={emailPreferenceData} value={emailPreferenceSelections} onChange={onEmailPreferenceChange} />
      </fieldset>
      <div>
        <button type="button" onClick={cancelEdit} >Cancel</button>
        <input type="submit" value="Submit" />
      </div>
    </form>
  );
}

export default wrappable(UserProfile);