import React from 'react';
import {
  Route,
  DefaultRoute,
  NotFoundRoute
} from 'react-router';
import App from './components/App';
import Index from './components/Index';
import NotFound from './components/NotFound';
import Answer from './components/Answer';
import QuestionList from './components/QuestionList';

/**
 * Get routes based on `baseUrl`.
 *
 * @param {string} baseUrl The baseUrl used to match paths below
 */
export function getRoutes(baseUrl = '/') {
  return (
    <Route name="app" path={baseUrl} handler={App}>
      <Route name="answer" path="answer/:questionName" handler={Answer}/>,
      <Route name="question-list" handler={QuestionList}/>,
      <DefaultRoute handler={Index}/>,
      <NotFoundRoute handler={NotFound}/>,
    </Route>
  );
}
