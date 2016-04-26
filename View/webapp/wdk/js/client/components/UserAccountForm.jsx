import React from 'react';
import UserIdentity from './UserIdentity';
import UserPassword from './UserPassword';
import UserContact from './UserContact';
import ApplicationSpecificProperties from './ApplicationSpecificProperties';
import { wrappable } from '../utils/componentUtils';

const UserAccountForm = (props) => {
  let { user, onTextChange, onEmailChange, onFormStateChange, isChanged, saveProfile } = props;

  return(
    <form className="wdk-UserProfile-profileForm" name="userProfileForm" onSubmit={saveProfile} >
      <p><i className="fa fa-asterisk"></i> = required</p>
      <UserIdentity user={user} onEmailChange={onEmailChange} onTextChange={onTextChange} />
      <br />
      <UserPassword user={user} />
      <br />
      <UserContact user={user} onTextChange={onTextChange} />
      <br />
      <ApplicationSpecificProperties user={user} onFormStateChange={onFormStateChange} />
      <div>
        <input type="submit" value="Save" disabled={isChanged ? "" : "disabled"} />
      </div>
    </form>
  );
}

export default wrappable(UserAccountForm);