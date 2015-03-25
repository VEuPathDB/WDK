/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import { RouteHandler } from 'react-router';
import Loading from '../components/Loading';
import createStoreMixin from '../mixins/createStoreMixin';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */

var storeMixin = createStoreMixin('appStore');

var App = React.createClass({

  mixins: [ storeMixin ],

  getStateFromStores(stores) {
    return stores.appStore.getState();
  },

  render() {
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

export default App;
