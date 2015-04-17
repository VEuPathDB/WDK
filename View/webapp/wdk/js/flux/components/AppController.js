/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import Loading from '../components/Loading';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

var AppController = React.createClass({

  propTypes: {
    application: React.PropTypes.object.isRequired
  },

  componentDidMount() {
    var store = this.props.application.getStore('appStore');
    this.storeSubscription = store.subscribe(state => {
      this.setState(state);
    });
  },

  componentWillUnmount() {
    this.storeSubscription.dispose();
  },

  render() {
    if (!this.state) return null;

    var { isLoading, errors } = this.state;

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
        <div>
          { isLoading !== 0 ? <Loading/> : null }
          <RouteHandler {...this.props}/>
        </div>
      );
    }
  }

});

export default AppController;
