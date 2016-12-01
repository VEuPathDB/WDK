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
      pick(state, 'userDatasetsById', 'loadError'),
      pick(state.globalData, 'user')
    );
  }

  getTitle(state) {
    const entry = state.userDatasetsById[this.props.params.id];
    return `User Data Set ${entry && entry.resource ? entry.resource.meta.name : '...'}`;
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

  isRenderDataLoadError(state) {
    return state.loadError;
  }

  isRenderDataLoaded(state) {
    const entry = state.userDatasetsById[this.props.params.id];
    return entry && !entry.isLoading && state.user;
  }

  renderView(state) {
    const entry = state.userDatasetsById[this.props.params.id];
    const isOwner = entry.resource && entry.resource.ownerUserId === state.user.id;
    return (
      <UserDatasetItem
        userDataset={entry.resource}
        updateUserDatasetItem={this.eventHandlers.updateUserDatasetItem}
        isOwner={isOwner}
      />
    );
  }
}

export default wrappable(UserDatasetItemController);
