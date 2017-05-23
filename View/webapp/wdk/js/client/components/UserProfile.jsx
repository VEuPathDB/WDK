import { wrappable } from '../utils/componentUtils';
import UserFormContainer, { UserFormContainerPropTypes } from './UserFormContainer';

/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
class UserProfile extends UserFormContainer {
  
}

UserProfile.propTypes = UserFormContainerPropTypes;

export default wrappable(UserProfile);
