import React from 'react';
import TextBox from './TextBox';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

let UserProfile = React.createClass({

  render() {

    // convert data into easily mappable objects
    let userFields = toNamedMap(Object.keys(this.props.user)
      .filter(value => value != 'properties' && value != 'id'), this.props.user);
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
                {tableOf(userFields, false)}
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
    this.props.userEvents.onEditProfile(this.props.user);
  },

  updateProfile() {
    this.props.userEvents.onUpdateProfile(this.props.user);
  },
  
  cancelEdit() {
    this.props.userEvents.onCancelEdit(this.props.user);
  }

});

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
    <div className="wdk-userForm">
      <h1>User Form</h1>
      <div>
        <div><label>First Name:</label></div>
        <div><TextBox value={user.firstName} onChange={getChangeHandler('firstName', onFormStateChange, user)} /></div>
      </div>
      <div>
        <div><label>Last Name:</label></div>
        <div><TextBox value={user.lastName} onChange={getChangeHandler('lastName', onFormStateChange, user)} /></div>
      </div>
      <div>
        <div><label>Organization:</label></div>
        <div><TextBox value={user.organization} onChange={getChangeHandler('organization', onFormStateChange, user)} /></div>
      </div>
      <div>
         <div><label>Email:</label></div>
         <div><TextBox value={user.email} onChange={getChangeHandler('email', onFormStateChange, user)} /></div>
      </div>
      <div>
        <input type="submit" value="Cancel" onClick={cancelEdit} />
        <input type="submit" value='Submit' onClick={updateProfile} />
      </div>
    </div>
  );
}

export default wrappable(UserProfile);