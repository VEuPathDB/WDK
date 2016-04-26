import React from 'react';
import { PropTypes } from 'react';
import { wrappable } from '../utils/componentUtils';


/**
 * This React component is a placeholder for any application specific properties that may have added by the overriding application.
 * @type {*|Function}
 */
const ApplicationSpecificProperties = React.createClass({

  render() { return(null) }

});


ApplicationSpecificProperties.propTypes = {

  /** The user object to be modified */
  user:  PropTypes.object.Required,

  /** Called with a parameter representing the new state when a form element changes */
  onFormStateChange:  PropTypes.func.Required,

  /** The WDK name for the user's application specific properties */
  name:  PropTypes.string.Required

};

export default wrappable(ApplicationSpecificProperties);