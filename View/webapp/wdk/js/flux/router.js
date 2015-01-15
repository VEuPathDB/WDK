import React from 'react';
import Router from 'react-router';
import IndexPage from './pages/IndexPage';
import NotFoundPage from './pages/NotFoundPage';
import AnswerPage from './pages/AnswerPage';

var { Route, RouteHandler, DefaultRoute, NotFoundRoute } = Router;

/**
 * Example of a "wholistic" routes definition. App defines the basic page
 * layout.
 *
 * var routes = (
 *   <Route name="app" path="/" handler={App}>
 *     <Route name="answer" path="answer/:questionName" handler={AnswerPage}/>
 *     <Route name="user" handler={User}/>
 *     <Route name="project" handler={Project}/>
 *     <DefaultRoute handler={IndexPage}/>
 *     <NotFoundRoute handler={NotFoundPage}/>
 *   </Route>
 * );
 */

/**
 * This is a transitional routes definition. Notice that there is not top-level
 * route, and that this is an array.
 *
 * Example usage:
 *
 *     Router.run(routes, '/project', function(Handler) {
 *       React.render(<Handler/>, document.getElementById("app"));
 *     });
 *
 */
export var routes = [
  <Route name="answer" path="answer/:questionName" handler={AnswerPage}/>,
  <DefaultRoute handler={IndexPage}/>,
  <NotFoundRoute handler={NotFoundPage}/>,
];

export default Router.create({ routes });
