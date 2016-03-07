import React from 'react';
import ReactDOM from 'react-dom';
import { Router, Route, IndexRoute, useRouterHistory } from 'react-router';
import { createHistory, useBasename } from 'history';

import WdkContext from './WdkContext';
import AppController from './components/AppController';
import IndexController from './components/IndexController';
import RecordController from './components/RecordController';
import NotFoundController from './components/NotFoundController';
import AnswerController from './components/AnswerController';
import QuestionListController from './components/QuestionListController';
import StepDownloadFormController from './components/StepDownloadFormController';
import UserProfileController from './components/UserProfileController';
import SiteMapController from './components/SiteMapController';

/**
 * Get routes based on `rootUrl`.
 *
 * @param {string} rootUrl The rootUrl used to match paths below
 */
export function start(rootUrl, rootElement, context, additionalRoutes = []) {
  // This makes it possible to omit the rootUrl in the Link Component, etc.
  // The custom history object will preprend the rootUrl.
  // E.g., it will convert "/record/gene/123" => "/{rootUrl}/record/gene/123".
  let createAppHistory = useRouterHistory(useBasename(createHistory));
  let history = createAppHistory({ basename: rootUrl });
  return ReactDOM.render((
    <WdkContext {...context}>
      <Router history={history}>
        <Route path="/" component={AppController}>
          <IndexRoute component={IndexController}/>
          <Route path="search/:recordClass/:question/result" component={AnswerController}/>
          <Route path="record/:recordClass/download/*" component={StepDownloadFormController}/>
          <Route path="record/:recordClass/*" component={RecordController}/>
          <Route path="step/:stepId/download" component={StepDownloadFormController}/>
          <Route path="user/profile" component={UserProfileController}/>
          <Route path="data-finder" component={SiteMapController}/>
          <Route path="question-list" component={QuestionListController}/>
          {additionalRoutes.map(route => ( <Route key={route.name} {...route}/> ))}
          <Route path="*" component={NotFoundController}/>
        </Route>
      </Router>
    </WdkContext>
  ), rootElement);
}
