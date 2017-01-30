import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadUserDatasetList } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetList from '../components/UserDatasetList';

class UserDatasetListController extends WdkViewController {

  getStoreName() {
    return "UserDatasetListStore";
  }

  getStateFromStore(store) {
    const {
      globalData: { user },
      userDatasetsLoading,
      userDatasets,
      loadError
    } = store.getState();

    return {
      user,
      userDatasetsLoading,
      userDatasets,
      loadError
    };
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
    return state.user && state.userDatasetsLoading === false;
  }

  isRenderDataLoadError(state) {
    return state.loadError;
  }

  renderView(state) {
    return ( <UserDatasetList userDatasets={state.userDatasets} user={state.user} /> );
  }
}

export default wrappable(UserDatasetListController);
