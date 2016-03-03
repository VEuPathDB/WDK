/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { wrappable, PureComponent } from '../utils/componentUtils';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

class AppController extends PureComponent {

  render() {
    let child = React.Children.only(this.props.children);
    let childProps = Object.assign({}, this.props, this.context);
    return React.cloneElement(child, childProps);
  }

}

AppController.contextTypes = {
  stores: React.PropTypes.object.isRequired,
  dispatchAction: React.PropTypes.func.isRequired,
  router: React.PropTypes.object.isRequired
};

export default wrappable(AppController);
