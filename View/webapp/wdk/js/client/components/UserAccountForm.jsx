import React, { PropTypes } from 'react';
import UserIdentity from './UserIdentity';
import UserPassword from './UserPassword';
import ApplicationSpecificProperties from './ApplicationSpecificProperties';
import { wrappable } from '../utils/componentUtils';

/**
 * This React component provides the form wrapper and enclosed fieldsets for the user profile/account form.
 * @param props
 * @returns {XML}
 * @constructor
 */
const UserAccountForm = (props) => {
  let { config, user, onPropertyChange, onEmailChange, onConfirmEmailChange, disableSubmit, saveProfile } = props;
  return(
    <form className="wdk-UserProfile-profileForm" name="userProfileForm" onSubmit={saveProfile} >
      <p><i className="fa fa-asterisk"></i> = required</p>
      <UserIdentity user={user} onEmailChange={onEmailChange} onConfirmEmailChange={onTextChange}
          onPropertyChange={onPropertyChange} propDefs={config.userProfileProperties}/>
      <br />
      <UserPassword user={user} wdkConfig={props.wdkConfig} />
      <br />
      <ApplicationSpecificProperties user={user} onPropertyChange={onPropertyChange} propDefs={config.userProfileProperties} />
      <div>
        <input type="submit" value="Save" disabled={disableSubmit} />
      </div>
    </form>
  );
};

UserAccountForm.propTypes = {

  /** The user object to be modified */
  user: PropTypes.object.isRequired,

  /** The on change handler for email text box inputs */
  onEmailChange:  PropTypes.func.isRequired,

  /** The on change handler for text box inputs */
  onTextChange: PropTypes.func.isRequired,

  /** Indicates that submit button should be enabled/disabled */
  disableSubmit:  PropTypes.bool.isRequired,

  /** The on submit handler for the form */
  saveProfile:  PropTypes.func.isRequired,
  
  /** WDK config for setting correct change password link */
  wdkConfig:  PropTypes.object.isRequired
};

export default wrappable(UserAccountForm);
