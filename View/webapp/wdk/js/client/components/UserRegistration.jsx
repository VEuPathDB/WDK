import { wrappable } from '../utils/componentUtils';
import UserFormContainer, { UserFormContainerPropTypes } from './UserFormContainer';

/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
class UserRegistration extends UserFormContainer {
  
}

UserRegistration.propTypes = UserFormContainerPropTypes;

export default wrappable(UserRegistration);
