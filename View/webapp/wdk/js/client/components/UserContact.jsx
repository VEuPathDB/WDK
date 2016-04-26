import React from 'react';
import TextBox from './TextBox';
import TextArea from './TextArea';
import { wrappable, getChangeHandler } from '../utils/componentUtils';

const UserContact = (props) => {
  let user = props.user;
  return (
    <fieldset>
      <legend>Contact Info</legend>
      <div>
        <label htmlFor="streetAddress">Street Address:</label>
        <TextArea id="streetAddress" value={user.address}
                  onChange={props.onTextChange('address')} maxLength='500' cols='100' rows="3"/>
      </div>
      <div>
        <label htmlFor="city">City:</label>
        <TextBox id="city" value={user.city} onChange={props.onTextChange('city')}
                 maxLength='255' size='100'/>
      </div>
      <div>
        <label htmlFor="state">State/Province:</label>
        <TextBox id="state" value={user.state} onChange={props.onTextChange('state')}
                 maxLength='255' size='100'/>
      </div>
      <div>
        <label htmlFor="country">Country:</label>
        <TextBox id="country" value={user.country} onChange={props.onTextChange('country')}
                 maxLength='255' size='100'/>
      </div>
      <div>
        <label htmlFor="zipCode">Postal Code:</label>
        <TextBox id="zipCode" value={user.zipCode} onChange={props.onTextChange('zipCode')}
                 maxLength='20' size='10'/>
      </div>
      <div>
        <label htmlFor="phoneNumber">Phone Number:</label>
        <TextBox id="phoneNumber" value={user.phoneNumber}
                 onChange={props.onTextChange('phoneNumber')} maxLength='50' size='25'/>
      </div>
    </fieldset>
  );
};

export default wrappable(UserContact);