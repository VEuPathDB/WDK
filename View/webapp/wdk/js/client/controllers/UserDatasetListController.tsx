import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractPageController from './AbstractPageController';
import { loadUserDatasetList } from '../actioncreators/UserDatasetsActionCreators';
import UserDatasetList from '../components/UserDatasetList';
import UserDatasetListStore, { State as StoreState } from "../stores/UserDatasetListStore";

const ActionCreators = { loadUserDatasetList };

type State = Pick<StoreState, 'userDatasetsLoading' | 'userDatasets' | 'loadError'>
           & Pick<StoreState["globalData"], 'user'>;

class UserDatasetListController extends AbstractPageController<State, UserDatasetListStore, typeof ActionCreators> {

  getStoreClass() {
    return UserDatasetListStore;
  }

  getStateFromStore() {
    const {
      globalData: { user },
      userDatasetsLoading,
      userDatasets,
      loadError
    } = this.store.getState();

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
    return ActionCreators;
  }

  loadData() {
    this.eventHandlers.loadUserDatasetList();
  }

  isRenderDataLoaded() {
    return this.state.user != null && this.state.userDatasetsLoading === false;
  }

  isRenderDataLoadError() {
    return this.state.loadError != null;
  }

  renderView() {
    return ( <UserDatasetList userDatasets={this.state.userDatasets} user={this.state.user!} history={this.props.history} /> );
  }
}

export default wrappable(UserDatasetListController);
