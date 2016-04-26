import React from 'react';
import TextBox from './TextBox';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

const UserIdentity = (props) => {
  let user = props.user;
  return (
    <fieldset>
      <legend>Identification</legend>
      <div>
        <label htmlFor="userEmail"><i className="fa fa-asterisk"></i>Email:</label>
        <TextBox type='email' id='userEmail'
                 value={user.email} onChange={props.onEmailChange("email")}
                 maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
      </div>
      <div>
        <label htmlFor="confirmUserEmail"><i className="fa fa-asterisk"></i>Retype Email:</label>
        <TextBox type='email' id='confirmUserEmail'
                 value={user.confirmEmail} onChange={props.onEmailChange("confirmEmail")}
                 maxLength='255' size='100' required placeholder='Your email is used as your unique user id' />
      </div>
      <div>
        <label htmlFor="firstName"><i className="fa fa-asterisk"></i>First Name:</label>
        <TextBox id="firstName" value={user.firstName} onChange={props.onTextChange("firstName")} maxLength='50' size='25' required />
      </div>
      <div>
        <label htmlFor="middleName">Middle Name:</label>
        <TextBox id="middleName" value={user.middleName} onChange={props.onTextChange("middleName")} maxLength='50' size='25'/>
      </div>
      <div>
        <label htmlFor="lastName"><i className="fa fa-asterisk"></i>Last Name:</label>
        <TextBox id="lastName" value={user.lastName} onChange={props.onTextChange("lastName")} maxLength='50' size='25' required />
      </div>
      <div>
        <label htmlFor="title">Title:</label>
        <TextBox id="title" value={user.title} onChange={props.onTextChange("title")} maxLength='50' size='25' />
      </div>
      <div>
        <label htmlFor="department">Department:</label>
        <TextBox id="department" value={user.department} onChange={props.onTextChange("department")} maxLength='50' size='25' />
      </div>
      <div>
        <label htmlFor="organization"><i className="fa fa-asterisk"></i>Organization:</label>
        <TextBox id="organization" value={user.organization} onChange={props.onTextChange("organization")} maxLength='255' size='100' required />
      </div>
    </fieldset>
  );
};

export default wrappable(UserIdentity);