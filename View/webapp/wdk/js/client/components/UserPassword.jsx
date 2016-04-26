import React from 'react';
import { PropTypes } from 'react';
import { wrappable } from '../utils/componentUtils';

/**
 * This React stateless function provides a link to the password change form inside a password change fieldset
 * @param props
 * @returns {XML}
 * @constructor
 */
const UserPassword = (props) => {
  let user = props.user;
  return (
    <fieldset>
      <legend>Password</legend>
      <div>
        <a href="#">Change your password</a>
      </div>
    </fieldset>
  );
};


UserPassword.propTypes = {

  /** The user object to be modified */
  user:  PropTypes.object.isRequired

};


export default wrappable(UserPassword);