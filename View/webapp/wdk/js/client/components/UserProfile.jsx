import React from 'react';
import TextBox from './TextBox';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

let UserProfile = React.createClass({

  render() {

    // convert data into easily mappable objects
    let userKeyData = [{key:'email', label:'Email'},
                       {key:'firstName', label:"First Name"},
                       {key:'lastName', label:"Last Name"},
                       {key:'organization', label:'Organization'}];
    let userFields = toNamedMap(Object.keys(this.props.user)
      .filter(value => value != 'properties' && value != 'id' && value !== 'isGuest'), this.props.user);
    let properties = toNamedMap(Object.keys(this.props.user.properties), this.props.user.properties);
    let preferences = toNamedMap(Object.keys(this.props.preferences), this.props.preferences);

    return (
      <div style={{ margin: "0 2em"}}>
        {this.props.user !== null && !this.props.user.isGuest ?
          this.props.isEdit ?
            <div>
              {userForm(this.props.user, this.props.userEvents.onFormStateChange, this.updateProfile, this.cancelEdit)}
            </div>
            :
            <div>
              <div>
                <h1>Your Profile <i className="fa fa-pencil" onClick={this.editProfile}></i></h1>
                {userProfile(userKeyData, this.props.user)}
                <h2>Properties</h2>
                {tableOf(properties, true, "Name", "Value")}
              </div>
              <h2>Preferences</h2>
              {tableOf(preferences, true, "Name", "Value")}
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

  updateProfile() {
    delete this.props.user.confirmEmail;
    this.props.userEvents.onUpdateProfile(this.props.user);
  },
  
  cancelEdit() {
    this.props.userEvents.onCancelEdit(this.props.user);
  }

});

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
};

function tableOf(objArray, addHeader, nameTitle, valueTitle) {
  return (
    <table>
      <tbody>
        { addHeader ? ( <tr><th>{nameTitle}</th><th>{valueTitle}</th></tr> ) : null }
        { objArray.map(val => ( <tr key={val.name}><td>{val.name}</td><td>{val.value}</td></tr> )) }
      </tbody>
    </table>
  );
};

function userForm(user, onFormStateChange, updateProfile, cancelEdit) {
  return(
    <form className="wdk-UserProfile-profileForm" name="userProfileForm">
      <fieldset>
        <legend>User Profile</legend>
        <span>* = required</span>
        <div>
          <div><span>*</span><label>Email:</label></div>
          <TextBox type='email' value={user.email} onChange={getChangeHandler('email', onFormStateChange, user)} maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <div><span>*</span><label>Retype Email:</label></div>
          <TextBox type='email' value={user.confirmEmail} onChange={getChangeHandler('confirmEmail', onFormStateChange, user)} maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
        </div>
        <div>
          <div><span>*</span><label>First Name:</label></div>
          <TextBox value={user.firstName} onChange={getChangeHandler('firstName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <div><span>*</span><label>Last Name:</label></div>
          <TextBox value={user.lastName} onChange={getChangeHandler('lastName', onFormStateChange, user)} maxLength='50' size='25' required />
        </div>
        <div>
          <div><span>*</span><label>Organization:</label></div>
          <TextBox value={user.organization} onChange={getChangeHandler('organization', onFormStateChange, user)} maxLength='255' size='100' required />
        </div>
      </fieldset>
      <div>
        <button onClick={cancelEdit} >Cancel</button>
        <button onClick={updateProfile} >Submit</button>
      </div>
    </form>
  );
}

export default wrappable(UserProfile);