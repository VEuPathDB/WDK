/**
 * Top level application controller.
 *
 * This controller is matched and rendered for __any__ route.
 */
import React from 'react';
import Router from 'react-router';

/*
 * RouterHandler is a special React component that the router uses to inject
 * matching child routes. For example, when the answer route is matched,
 * AnswerPage will be rendered as a child of RouteHandler.
 */
var { RouteHandler } = Router;

var App = React.createClass({

  render() {
    return (
      <div>
        <h1>WDK 3.0</h1>
        <RouteHandler {...this.props}/>
      </div>
    );
  }

});

export default App;
