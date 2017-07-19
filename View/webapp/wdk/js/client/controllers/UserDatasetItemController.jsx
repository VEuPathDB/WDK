import { keyBy, pick } from 'lodash';
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
      pick(state, 'userDatasetsById', 'loadError', 'userDatasetUpdating', 'updateError'),
      pick(state.globalData, 'user', 'questions', 'config')
    );
  }

  getTitle(state) {
    const entry = state.userDatasetsById[this.props.match.params.id];
    if (entry && entry.resource) {
      return `User Data Set ${entry.resource.meta.name}`;
    }
    if (entry && !entry.resource) {
      return `User Data Set not found`;
    }
    return `User Data Set ...`;
  }

  getActionCreators() {
    return {
      loadUserDatasetItem,
      updateUserDatasetItem
    };
  }

  loadData(actionCreators, state, props, nextProps) {
    const idChanged = nextProps && nextProps.match.params.id !== props.match.params.id;
    if (idChanged || !state.userDatasetsById[props.match.params.id]) {
      actionCreators.loadUserDatasetItem(Number(props.match.params.id));
    }
  }

  isRenderDataLoadError(state) {
    return state.loadError;
  }

  isRenderDataLoaded(state) {
    const entry = state.userDatasetsById[this.props.match.params.id];
    return entry && !entry.isLoading && state.user && state.questions && state.config;
  }

  renderView(state) {
    const entry = state.userDatasetsById[this.props.match.params.id];
    const isOwner = entry.resource && entry.resource.ownerUserId === state.user.id;
    return (
      <UserDatasetItem
        userDataset={entry.resource}
        updateUserDatasetItem={this.eventHandlers.updateUserDatasetItem}
        userDatasetUpdating={state.userDatasetUpdating}
        updateError={state.updateError}
        isOwner={isOwner}
        questionMap={keyBy(state.questions, 'name')}
        webAppUrl={state.config.webAppUrl}
      />
    );
  }
}

export default wrappable(UserDatasetItemController);
