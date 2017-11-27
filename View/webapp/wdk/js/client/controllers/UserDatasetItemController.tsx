import * as React from 'react';
import { keyBy, pick } from 'lodash';
import { ViewControllerProps } from '../CommonTypes';
import { wrappable } from '../utils/componentUtils';
import AbstractPageController, { PageControllerProps } from './AbstractPageController';
import { loadUserDatasetItem, updateUserDatasetItem } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetItem from '../components/UserDatasetItem';
import UserDatasetItemStore, { State as StoreState } from '../stores/UserDatasetItemStore';

type State = Pick<StoreState, 'userDatasetsById' | 'loadError' | 'userDatasetUpdating' | 'updateError'>
           & Pick<StoreState["globalData"], 'user' | 'questions' | 'config'>;

const ActionCreators = { loadUserDatasetItem, updateUserDatasetItem };

type EventHandlers = typeof ActionCreators;

/**
 * View Controller for User Dataset record.
 *
 * Note that we are accessing the userDataset from an object keyed by the
 * userDataset's id. This avoids race conditions that arise when ajax requests
 * complete in a different order than they were invoked.
 */
class UserDatasetItemController extends AbstractPageController<State, UserDatasetItemStore, EventHandlers> {

  getStoreClass(): typeof UserDatasetItemStore {
    return UserDatasetItemStore;
  }

  getStateFromStore(): State {
    let {
      userDatasetsById,
      loadError,
      userDatasetUpdating,
      updateError,
      globalData: { user, questions, config }
    } = this.store.getState();

    return {
      userDatasetsById,
      loadError,
      userDatasetUpdating,
      updateError,
      user,
      questions,
      config
    }
  }

  getTitle() {
    const entry = this.state.userDatasetsById[this.props.match.params.id];
    if (entry && entry.resource) {
      return `User Data Set ${entry.resource.meta.name}`;
    }
    if (entry && !entry.resource) {
      return `User Data Set not found`;
    }
    return `User Data Set ...`;
  }

  getActionCreators() {
    return ActionCreators;
  }

  loadData(prevProps?: PageControllerProps<UserDatasetItemStore>) {
    const idChanged = prevProps && prevProps.match.params.id !== this.props.match.params.id;
    if (idChanged || !this.state.userDatasetsById[this.props.match.params.id]) {
      this.eventHandlers.loadUserDatasetItem(Number(this.props.match.params.id));
    }
  }

  isRenderDataLoadError() {
    return this.state.loadError != null;
  }

  isRenderDataLoaded() {
    const entry = this.state.userDatasetsById[this.props.match.params.id];
    return (
      entry != null &&
      !entry.isLoading &&
      this.state.user != null &&
      this.state.questions != null &&
      this.state.config != null
    );
  }

  renderView() {
    const entry = this.state.userDatasetsById[this.props.match.params.id];
    const isOwner = (
      entry.resource != null &&
      this.state.user != null &&
      entry.resource.ownerUserId === this.state.user.id
    )
    return (
      <UserDatasetItem
        userDataset={entry.resource!}
        updateUserDatasetItem={this.eventHandlers.updateUserDatasetItem}
        userDatasetUpdating={this.state.userDatasetUpdating}
        updateError={this.state.updateError}
        isOwner={isOwner}
        questionMap={keyBy(this.state.questions, 'name')}
        webAppUrl={this.state.config!.webAppUrl}
      />
    );
  }
}

export default wrappable(UserDatasetItemController);
