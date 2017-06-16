import PropTypes from 'prop-types';
import { wrappable } from '../utils/componentUtils';


/**
 * This React component is a placeholder for any application specific properties that may have added by the overriding application.
 * @type {*|Function}
 */
function ApplicationSpecificProperties() {
  return null;
}


ApplicationSpecificProperties.propTypes = {

  /** The user object to be modified */
  user:  PropTypes.object.isRequired,

  /** Called with a parameter representing the new state when a form element changes */
  onFormStateChange:  PropTypes.func.isRequired,

  /** The WDK name for the user's application specific properties */
  name:  PropTypes.string.isRequired

};

export default wrappable(ApplicationSpecificProperties);
