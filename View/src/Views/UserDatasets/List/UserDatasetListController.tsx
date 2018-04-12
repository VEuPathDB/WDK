import * as React from 'react';
import { wrappable } from 'Utils/ComponentUtils';
import AbstractPageController from 'Core/Controllers/AbstractPageController';

import { User } from 'Utils/WdkUser';
import 'Views/UserDatasets/UserDatasets.scss';
import UserDatasetList from 'Views/UserDatasets/List/UserDatasetList';
import {
  loadUserDatasetList,
  updateUserDatasetDetail,
  removeUserDataset,
  shareUserDatasets,
  unshareUserDatasets
} from 'Views/UserDatasets/UserDatasetsActionCreators';
import UserDatasetListStore, { State as StoreState } from "Views/UserDatasets/List/UserDatasetListStore";

import UserDatasetEmptyState from 'Views/UserDatasets/EmptyState';

const ActionCreators = {
  loadUserDatasetList,
  updateUserDatasetDetail,
  removeUserDataset,
  shareUserDatasets,
  unshareUserDatasets
};

type State = Pick<StoreState, 'userDatasetsLoading' | 'userDatasets' | 'loadError'>
           & Pick<StoreState["globalData"], 'user' | 'config'>;

class UserDatasetListController extends AbstractPageController <State, UserDatasetListStore, typeof ActionCreators> {
  getStoreClass () {
    return UserDatasetListStore;
  }

  getStateFromStore () {
    const {
      globalData: { user, config },
      userDatasetsLoading,
      userDatasets,
      loadError
    } = this.store.getState();

    return {
      user,
      config,
      userDatasetsLoading,
      userDatasets,
      loadError
    };
  }

  getTitle () {
    return 'My Datasets';
  }

  getActionCreators () {
    return ActionCreators;
  }

  loadData () {
    this.eventHandlers.loadUserDatasetList();
  }

  isRenderDataLoaded () {
    return this.state.user != null
      && this.state.config != null
      && this.state.userDatasetsLoading === false;
  }

  renderView () {
    const { userDatasets, loadError, config } = this.state;
    const { projectId, displayName: projectName } = config;
    const user: User = this.state.user;
    const { updateUserDatasetDetail, shareUserDatasets, removeUserDataset } = this.eventHandlers;
    const { history, location } = this.props;

    const title = this.getTitle();
    const loggedIn: boolean = (typeof user !== 'undefined' && user.isGuest === false);
    const listProps = {
      user,
      history,
      location,
      projectId,
      projectName,
      userDatasets,
      removeUserDataset,
      shareUserDatasets,
      unshareUserDatasets,
      updateUserDatasetDetail
    };
    return (
      <div className="UserDatasetList-Controller">
        {!loggedIn
          ? <h1 className="UserDatasetList-Title">{title}</h1>
          : null
        }
        <div className="UserDatasetList-Content">
          {!loggedIn
            ? <UserDatasetEmptyState message="Please log in to upload and view your user datasets."/>
            : <UserDatasetList {...listProps} />
          }
        </div>
      </div>
    )
  }
}

export default wrappable(UserDatasetListController);
