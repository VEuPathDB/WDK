/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import AppStore from '../stores/appStore';
import CommonActions from '../actions/commonActions';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

let AppController = React.createClass({

  propTypes: {
    application: React.PropTypes.object.isRequired
  },

  childContextTypes: {
    application: React.PropTypes.object.isRequired
  },

  getChildContext() {
    let { application } = this.props
    return { application };
  },

  getInitialState() {
    return {
      errors: []
    };
  },

  componentWillMount() {
    let { application } = this.props;
    let store = application.getStore(AppStore);
    let commonActions = application.getActions(CommonActions);
    commonActions.fetchCommonData();
    this.storeSubscription = store.subscribe(state => {
      this.setState(state);
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

export default AppController;
