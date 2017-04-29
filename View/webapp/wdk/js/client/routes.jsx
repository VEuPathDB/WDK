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
import UserDatasetListController from './controllers/UserDatasetListController';
import UserDatasetItemController from './controllers/UserDatasetItemController';

// TODO Uncomment when question page is moved
// import QuestionController from './controllers/QuestionController';

export default (
  <Route path="/">
    <IndexRoute component={IndexController}/>
    {/* TODO Uncomment when question page is moved
    <Route path="search/:recordClass/:question" component={QuestionController}/>
    */}
    <Route path="search/:recordClass/:question/result" component={AnswerController}/>
    <Route path="record/:recordClass/download/*" component={DownloadFormController}/>
    <Route path="record/:recordClass/*" component={RecordController}/>
    <Route path="step/:stepId/download" component={DownloadFormController}/>
    <Route path="user/profile" component={UserProfileController}/>
    <Route path="user/profile/password" component={UserPasswordChangeController}/>
    <Route path="workspace/datasets" component={UserDatasetListController}/>
    <Route path="workspace/datasets/:id" component={UserDatasetItemController}/>
    <Route path="data-finder" component={SiteMapController}/>
    <Route path="question-list" component={QuestionListController}/>
    <Route path="*" component={NotFoundController}/>
  </Route>
);
