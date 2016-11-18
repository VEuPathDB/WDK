import {Route, IndexRoute} from 'react-router';
import IndexController from './controllers/IndexController';
import RecordController from './controllers/RecordController';
import NotFoundController from './controllers/NotFoundController';
import AnswerController from './controllers/AnswerController';
import QuestionListController from './controllers/QuestionListController';
import DownloadFormController from './controllers/DownloadFormController';
import UserProfileController from './controllers/UserProfileController';
import UserPasswordChangeController from './controllers/UserPasswordChangeController';
import SiteMapController from './controllers/SiteMapController';
import UserDatasetsController from './controllers/UserDatasetsController';

export default (
  <Route path="/">
    <IndexRoute component={IndexController}/>
    <Route path="search/:recordClass/:question/result" component={AnswerController}/>
    <Route path="record/:recordClass/download/*" component={DownloadFormController}/>
    <Route path="record/:recordClass/*" component={RecordController}/>
    <Route path="step/:stepId/download" component={DownloadFormController}/>
    <Route path="user/profile" component={UserProfileController}/>
    <Route path="user/profile/password" component={UserPasswordChangeController}/>
    <Route path="workspace/datasets" component={UserDatasetsController}/>
    <Route path="data-finder" component={SiteMapController}/>
    <Route path="question-list" component={QuestionListController}/>
    <Route path="*" component={NotFoundController}/>
  </Route>
);
