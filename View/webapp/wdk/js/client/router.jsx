import {cloneElement, Children, PropTypes} from 'react';
import { Router, Route, IndexRoute } from 'react-router';

import AppController from './components/AppController';
import IndexController from './components/IndexController';
import RecordController from './components/RecordController';
import NotFoundController from './components/NotFoundController';
import AnswerRouteHandler from './components/AnswerRouteHandler';
import QuestionListController from './components/QuestionListController';
import StepDownloadFormController from './components/StepDownloadFormController';
import UserProfileController from './components/UserProfileController';
import SiteMapController from './components/SiteMapController';

/**
 * @param {string} history The history used to navigate between routes
 * @param {Object} context Context object passed to WdkContext.
 * @param {Array<Object>} additionalRoutes Route configs to add to router.
 */
export function create(history, context, additionalRoutes = []) {
  function createElement(Component, props) {
    return <Component {...props} {...context}/>;
  }
  return (
    <Router history={history} createElement={createElement}>
      <Route path="/" component={AppController}>
        <IndexRoute component={IndexController}/>
        <Route path="search/:recordClass/:question/result" component={AnswerRouteHandler}/>
        <Route path="record/:recordClass/download/*" component={StepDownloadFormController}/>
        <Route path="record/:recordClass/*" component={RecordController}/>
        <Route path="step/:stepId/download" component={StepDownloadFormController}/>
        <Route path="user/profile" component={UserProfileController}/>
        <Route path="data-finder" component={SiteMapController}/>
        <Route path="question-list" component={QuestionListController}/>
        {additionalRoutes.map(route => ( <Route key={route.path} {...route}/> ))}
        <Route path="*" component={NotFoundController}/>
      </Route>
    </Router>
  );
}
