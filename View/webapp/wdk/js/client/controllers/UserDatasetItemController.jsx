import { pick } from 'lodash';
import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadUserDatasetItem, updateUserDatasetItem } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetItem from '../components/UserDatasetItem';

/**
 * View Controller for User Dataset record.
 *
 * Note that we are accessing the userDataset from an object keyed by the
 * userDataset's id. This avoids race conditions that arise when ajax requests
 * complete in a different order than they were invoked.
 */
class UserDatasetItemController extends WdkViewController {

  getStoreName() {
    return 'UserDatasetItemStore';
  }

  getStateFromStore(store) {
    let state = store.getState();
    return Object.assign(
      pick(state, 'userDatasetsById'),
      pick(state.globalData, 'user')
    );
  }

  getTitle(state) {
    return `User Data Set ${state.userDataset ? state.userDataset.meta.name : '...'}`;
  }

  getActionCreators() {
    return {
      loadUserDatasetItem,
      updateUserDatasetItem
    };
  }

  loadData(state, props, nextProps) {
    const idChanged = nextProps && nextProps.params.id !== props.params.id;
    if (idChanged || !state.userDatasetsById[props.params.id]) {
      this.eventHandlers.loadUserDatasetItem(Number(props.params.id));
    }
  }

  isRenderDataLoaded(state) {
    return state.userDatasetsById[this.props.params.id] && state.user;
  }

  renderView(state) {
    let userDataset = state.userDatasetsById[this.props.params.id];
    return (
      <UserDatasetItem
        userDataset={userDataset}
        updateUserDatasetItem={this.eventHandlers.updateUserDatasetItem}
        isOwner={userDataset.ownerUserId === state.user.id}
      />
    );
  }
}

export default wrappable(UserDatasetItemController);
