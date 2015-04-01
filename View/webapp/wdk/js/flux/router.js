import React from 'react';
import {
  Route,
  DefaultRoute,
  NotFoundRoute
} from 'react-router';
import AppController from './components/AppController';
import IndexController from './components/IndexController';
import NotFoundController from './components/NotFoundController';
import AnswerController from './components/AnswerController';
import QuestionListController from './components/QuestionListController';

/**
 * Get routes based on `baseUrl`.
 *
 * @param {string} baseUrl The baseUrl used to match paths below
 */
export function getRoutes(baseUrl = '/') {
  return (
    <Route name="app" path={baseUrl} handler={AppController}>
      <Route name="answer" path="answer/:questionName" handler={AnswerController}/>,
      <Route name="question-list" handler={QuestionListController}/>,
      <DefaultRoute handler={IndexController}/>,
      <NotFoundRoute handler={NotFoundController}/>,
    </Route>
  );
}
