import React from 'react';
import ReactDOM from 'react-dom';
import ReactRouter from 'react-router';

import AppController from './components/AppController';
import IndexController from './components/IndexController';
import RecordController from './components/RecordController';
import NotFoundController from './components/NotFoundController';
import AnswerController from './components/AnswerController';
import QuestionListController from './components/QuestionListController';
import StepDownloadFormController from './components/StepDownloadFormController';

let { Route, DefaultRoute, NotFoundRoute } = ReactRouter;
/**
 * Get routes based on `rootUrl`.
 *
 * @param {string} rootUrl The rootUrl used to match paths below
 */

export function start(rootUrl, rootElement, props) {
  let router = ReactRouter.create({
    routes: (
      <Route name="app" path={rootUrl} handler={AppController}>
        <Route name="answer" path="search/:recordClass/:question/result" handler={AnswerController}/>
        <Route name="record" path="record/:recordClass/*" handler={RecordController}/>
        <Route name="stepDownloadForm" path="step/:stepId/download" handler={StepDownloadFormController}/>
        <Route name="question-list" handler={QuestionListController}/>
        <DefaultRoute handler={IndexController}/>
        <NotFoundRoute handler={NotFoundController}/>
      </Route>
    ),
    location: ReactRouter.HistoryLocation
  });

  // Root contains the matching handlers, which are a type of React component: View-Controllers
  router.run((Root, state) => {
    // Remove the auth_tkt query param from url
    // XXX Implement router filters?
    if ('auth_tkt' in state.query) {
      state.query.auth_tkt = undefined;
      router.replaceWith(
        state.pathname,
        state.params,
        state.query
      );
    }
    else {
      ReactDOM.render(<Root {...state} {...props} router={router} />, rootElement);
    }
  });

  return router;
}
