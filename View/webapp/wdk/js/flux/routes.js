import {
  Route,
  DefaultRoute,
  NotFoundRoute
} from 'react-router';
import React from 'react';
import AppController from './components/AppController';
import IndexController from './components/IndexController';
import NotFoundController from './components/NotFoundController';
import AnswerController from './components/AnswerController';
import QuestionListController from './components/QuestionListController';

/**
 * Get routes based on `rootUrl`.
 *
 * @param {string} rootUrl The rootUrl used to match paths below
 */
function getRoutes(rootUrl = '/') {
  return (
    <Route name="app" path={rootUrl} handler={AppController}>
      <Route name="answer" path="answer/:questionName" handler={AnswerController}/>,
      <Route name="question-list" handler={QuestionListController}/>,
      <DefaultRoute handler={IndexController}/>,
      <NotFoundRoute handler={NotFoundController}/>,
    </Route>
  );
}

export default {
  getRoutes
};
