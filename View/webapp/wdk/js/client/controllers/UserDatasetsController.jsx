import { pick } from 'lodash';
import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadUserDatasets } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasets from '../components/UserDatasets';

class UserDatasetsController extends WdkViewController {

  getStoreName() {
    return "UserDatasetsStore";
  }

  getStateFromStore(store) {
    return pick(store.getState(), 'userDatasetsLoading', 'userDatasets', 'loadError');
  }

  getTitle() {
    return 'User Data Sets';
  }

  getActionCreators() {
    return {
      loadUserDatasets
    };
  }

  loadData() {
    this.eventHandlers.loadUserDatasets();
  }

  isRenderDataLoaded(state) {
    return state.userDatasetsLoading === false;
  }

  renderView(state) {
    return ( <UserDatasets userDatasets={state.userDatasets} /> );
  }
}

export default wrappable(UserDatasetsController);
