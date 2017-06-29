import {Route, IndexRoute} from 'react-router';
import IndexController from './controllers/IndexController';
import RecordController from './controllers/RecordController';
import NotFoundController from './controllers/NotFoundController';
import AnswerController from './controllers/AnswerController';
import QuestionListController from './controllers/QuestionListController';
import DownloadFormController from './controllers/DownloadFormController';
import UserRegistrationController from './controllers/UserRegistrationController';
import UserProfileController from './controllers/UserProfileController';
import UserPasswordChangeController from './controllers/UserPasswordChangeController';
import UserPasswordResetController from './controllers/UserPasswordResetController';
import UserMessageController from './controllers/UserMessageController';
import SiteMapController from './controllers/SiteMapController';
import UserDatasetListController from './controllers/UserDatasetListController';
import UserDatasetItemController from './controllers/UserDatasetItemController';
import FavoritesController from './controllers/FavoritesController';

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
    <Route path="user/registration" component={UserRegistrationController}/>
    <Route path="user/profile" component={UserProfileController}/>
    <Route path="user/profile/password" component={UserPasswordChangeController}/>
    <Route path="user/forgot-password" component={UserPasswordResetController}/>
    <Route path="user/message/:messageId" component={UserMessageController}/>
    <Route path="workspace/datasets" component={UserDatasetListController}/>
    <Route path="workspace/datasets/:id" component={UserDatasetItemController}/>
    <Route path="favorites" component={FavoritesController}/>
    <Route path="data-finder" component={SiteMapController}/>
    <Route path="question-list" component={QuestionListController}/>
    <Route path="*" component={NotFoundController}/>
  </Route>
);
