/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import {PropTypes} from 'react';
import {wrappable} from '../utils/componentUtils';

/** Root component */
function AppController(props) {
  return (
    <div className="wdk-RootContainer">
      {props.children}
    </div>
  );
}

AppController.propTypes = {
  children: PropTypes.element.isRequired
};

export default wrappable(AppController);
