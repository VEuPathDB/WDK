import React from 'react';
import Router from 'react-router';

// Link is a component used to create links to other routes.
var { Link } = Router;

var IndexPage = React.createClass({

  render() {
    return (
      <div>
        <p>This is the future home of WDK 3.0</p>

        <h2>Resources under development</h2>
        <ul>
          <li><Link to="question-list">Question list</Link></li>
        </ul>
      </div>
    );
  }

});

export default IndexPage;
