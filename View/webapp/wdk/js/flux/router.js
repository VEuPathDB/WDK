import React from 'react';
import Router from 'react-router';
import App from './controllers/App';
import IndexPage from './controllers/IndexPage';
import NotFoundPage from './controllers/NotFoundPage';
import AnswerPage from './controllers/AnswerPage';
import QuestionListPage from './controllers/QuestionListPage';

var { Route, DefaultRoute, NotFoundRoute } = Router;

/**
 * Get routes based on `baseUrl`.
 *
 * @param {string} baseUrl The baseUrl used to match paths below
 */
export function getRoutes(baseUrl = '/') {
  return (
    <Route name="app" path={baseUrl} handler={App}>
      <Route name="answer" path="answer/:questionName" handler={AnswerPage}/>,
      <Route name="question-list" handler={QuestionListPage}/>,
      <DefaultRoute handler={IndexPage}/>,
      <NotFoundRoute handler={NotFoundPage}/>,
    </Route>
  );
}
