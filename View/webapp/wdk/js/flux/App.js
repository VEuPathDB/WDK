import React from 'react';
import Router from 'react-router';

var { RouteHandler, Link } = Router;

// TODO Add common header stuff in this component, e.g. <UserInfo>
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
