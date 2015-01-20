import React from 'react';
import Router from 'react-router';

var { Link } = Router;

export default React.createClass({

  displayName: 'IndexPage',

  mixins: [ Router.Navigation ],

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
