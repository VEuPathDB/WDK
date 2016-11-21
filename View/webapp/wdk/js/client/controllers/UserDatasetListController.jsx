import { pick } from 'lodash';
import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadUserDatasetList } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetList from '../components/UserDatasetList';

class UserDatasetListController extends WdkViewController {

  getStoreName() {
    return "UserDatasetListStore";
  }

  getStateFromStore(store) {
    return pick(store.getState(), 'userDatasetsLoading', 'userDatasets', 'loadError');
  }

  getTitle() {
    return 'User Data Sets';
  }

  getActionCreators() {
    return {
      loadUserDatasetList
    };
  }

  loadData() {
    this.eventHandlers.loadUserDatasetList();
  }

  isRenderDataLoaded(state) {
    return state.userDatasetsLoading === false;
  }

  renderView(state) {
    return ( <UserDatasetList userDatasets={state.userDatasets} /> );
  }
}

export default wrappable(UserDatasetListController);
