import { pick } from 'lodash';
import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadUserDatasetItem } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetItem from '../components/UserDatasetItem';

/**
 * View Controller for User Dataset record
 */
class UserDatasetItemController extends WdkViewController {

  getStoreName() {
    return "UserDatasetItemStore";
  }

  getStateFromStore(store) {
    return pick(store.getState(), 'userDataset');
  }

  getTitle(state) {
    return `User Data Set ${state.userDataset ? state.userDataset.meta.name : '...'}`;
  }

  getActionCreators() {
    return {
      loadUserDatasetItem
    };
  }

  loadData(state, props) {
    this.eventHandlers.loadUserDatasetItem(props.params.id);
  }

  isRenderDataLoaded(state) {
    return !!state.userDataset;
  }

  renderView(state) {
    return ( <UserDatasetItem userDataset={state.userDataset} /> );
  }
}

export default wrappable(UserDatasetItemController);
