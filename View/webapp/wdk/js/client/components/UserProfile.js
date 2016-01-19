import React from 'react';
import { wrappable } from '../utils/componentUtils';

let UserProfile = React.createClass({

  render() {

    // convert data into easily mappable objects
    let userFields = toNamedMap(Object.keys(this.props.user)
      .filter(value => value != 'properties' && value != 'id'), this.props.user);
    let properties = toNamedMap(Object.keys(this.props.user.properties), this.props.user.properties);
    let preferences = toNamedMap(Object.keys(this.props.preferences), this.props.preferences);

    return (
      <div style={{ margin: "0 2em"}}>
        <h1>Your Profile</h1>
        {tableOf(userFields, false)}
        <h2>Properties</h2>
        {tableOf(properties, true, "Name", "Value")}
        <h2>Preferences</h2>
        {tableOf(preferences, true, "Name", "Value")}
      </div>
    );
  }

});

function toNamedMap(keys, object) {
  return keys.map(key => ({ name: key, value: object[key] }));
}

function tableOf(objArray, addHeader, nameTitle, valueTitle) {
  return (
    <table>
      <tbody>
        { addHeader ? ( <tr><th>{nameTitle}</th><th>{valueTitle}</th></tr> ) : null }
        { objArray.map(val => ( <tr><td>{val.name}</td><td>{val.value}</td></tr> )) }
      </tbody>
    </table>
  );
}

export default wrappable(UserProfile);