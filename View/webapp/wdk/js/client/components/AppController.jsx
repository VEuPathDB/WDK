/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import {cloneElement, Children, Component, PropTypes} from 'react';
import {wrappable} from '../utils/componentUtils';

/**
 * Root component
 */
class AppController extends Component {

  render() {
    let child = Children.only(this.props.children);
    let childProps = Object.assign({}, this.props, this.context);
    return (
      <div className="wdk-RootContainer">
        {cloneElement(child, childProps)}
      </div>
    );
  }

}

AppController.contextTypes = {
  stores: PropTypes.object.isRequired,
  dispatchAction: PropTypes.func.isRequired,
  router: PropTypes.object.isRequired
};

export default wrappable(AppController);
