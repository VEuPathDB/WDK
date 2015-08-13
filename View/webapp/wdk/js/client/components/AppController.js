/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import ContextMixin from '../utils/contextMixin';
import wrappable from '../utils/wrappable';
import CommonActions from '../actions/commonActions';
import PreferenceActions from '../actions/preferenceActions';

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

  mixins: [ React.addons.PureRenderMixin ],

  componentWillMount() {
    let { dispatch, subscribe } = this.props.context;

    dispatch(PreferenceActions.loadPreferences());
    dispatch(CommonActions.fetchCommonData());

    this.storeSubscription = subscribe(state => {
      this.setState({ errors: state.errors });
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
