import React from 'react';
import Router from 'react-router';

// Link is a component used to create links to other routes.
// See https://github.com/rackt/react-router/blob/master/docs/api/components/Link.md
var { Link } = Router;

/**
 * This component is rendered by the DefaultRoute in ../routes.js.
 *
 * It's current purpose is to demonstrate how one can create links to other
 * routes. In this case we use Link, which is provided by the react-router
 * library. The `to` property is the *name* of a route, as defined in
 * ../routes.js. Additional parameters and query arguments can be provided to
 * the Link component as props (params and query, resp). The reason to use Link
 * as opposed to the more common <a> is that Link will generate the correct
 * href attribute based on the router Location implementation specified in the
 * bootstrapping process.
 *
 * See https://github.com/rackt/react-router/blob/master/docs/api/run.md#location-optional
 * and https://github.com/rackt/react-router/blob/master/docs/api/misc/Location.md
 * for more details.
 */
var Index = React.createClass({

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

export default Index;
