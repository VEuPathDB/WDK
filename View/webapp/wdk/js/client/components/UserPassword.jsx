import React from 'react';
import { wrappable } from '../utils/componentUtils';

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

export default wrappable(UserPassword);