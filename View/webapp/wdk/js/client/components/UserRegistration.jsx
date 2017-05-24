import { wrappable } from '../utils/componentUtils';
import UserFormContainer, { UserFormContainerPropTypes } from './UserFormContainer';

/**
 * React component for the user profile/account form
 * @type {*|Function}
 */
let UserRegistration = props => (

  <UserFormContainer {...props}
      shouldHideForm={!props.userFormData.isGuest}
      hiddenFormMessage="You must log out before registering a new user."
      titleText="Registration"
      showChangePasswordBox={false}
      submitButtonText="Sign me up!"
      onSubmit={props.userEvents.submitRegistrationForm}/>

);

UserRegistration.propTypes = UserFormContainerPropTypes;

export default wrappable(UserRegistration);
