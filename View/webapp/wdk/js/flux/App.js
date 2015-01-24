import React from 'react';
import Router from 'react-router';

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
