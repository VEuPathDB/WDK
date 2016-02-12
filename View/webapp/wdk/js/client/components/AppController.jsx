/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import { RouteHandler } from 'react-router';
import { wrappable } from '../utils/componentUtils';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

let AppController = React.createClass({

  mixins: [ PureRenderMixin ],

  childContextTypes: {
    stores: React.PropTypes.object,
    actionCreators: React.PropTypes.object,
    router: React.PropTypes.func
  },

  getChildContext() {
    return {
      stores: this.props.stores,
      actionCreators: this.props.actionCreators,
      router: this.props.router
    };
  },

  render() {
    return (
      <RouteHandler {...this.props}/>
    );
  }

});

export default wrappable(AppController);
