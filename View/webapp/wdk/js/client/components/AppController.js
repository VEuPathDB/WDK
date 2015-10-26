/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import { wrappable } from '../utils/componentUtils';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

let AppController = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    return (
      <RouteHandler {...this.props}/>
    );
  }

});

export default wrappable(AppController);
