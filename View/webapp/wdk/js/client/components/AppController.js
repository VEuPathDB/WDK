/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import ContextMixin from '../utils/contextMixin';
import wrappable from '../utils/wrappable';

let { contextTypes } = ContextMixin;

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

let AppController = React.createClass({

  propTypes: {
    context: React.PropTypes.shape(contextTypes).isRequired
  },

  childContextTypes: contextTypes,

  getChildContext() {
    return this.props.context;
  },

  componentWillMount() {
    let { context } = this.props;
    let { commonActions, preferenceActions } = context.actions;
    let { appStore } = context.stores;

    preferenceActions.loadPreferences();
    commonActions.fetchCommonData();

    this.storeSubscription = appStore.subscribe(({ errors }) => {
      this.setState({ errors });
    });
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  render() {
    let { errors } = this.state;

    if (errors.length > 0) {
      return (
        <div>
          <h3>An Unexpected Error Occurred</h3>
          <div className="wdkAnswerError">{errors}</div>
        </div>
      );
    }
    else {
      return (
        <RouteHandler/>
      );
    }
  }

});

export default wrappable(AppController);
