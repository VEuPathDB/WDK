import { get } from 'lodash';
import * as React from 'react';

import 'Views/UserDatasets/UserDatasets.scss';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import { wrappable } from 'Utils/ComponentUtils';
import { User } from 'Utils/WdkUser';
import UserDatasetEmptyState from 'Views/UserDatasets/EmptyState';
import UserDatasetList from 'Views/UserDatasets/List/UserDatasetList';
import UserDatasetListStore, { State as StoreState } from 'Views/UserDatasets/List/UserDatasetListStore';
import {
  loadUserDatasetList,
  removeUserDataset,
  shareUserDatasets,
  unshareUserDatasets,
  updateProjectFilter,
  updateUserDatasetDetail
} from 'Views/UserDatasets/UserDatasetsActionCreators';

const ActionCreators = {
  updateUserDatasetDetail,
  removeUserDataset,
  shareUserDatasets,
  unshareUserDatasets,
  updateProjectFilter
};

type State = Pick<StoreState, 'userDatasetsLoading' | 'userDatasets' | 'loadError' | 'filterByProject'>
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
      loadError,
      filterByProject
    } = this.store.getState();

    return {
      user,
      config,
      userDatasetsLoading,
      userDatasets,
      loadError,
      filterByProject
    };
  }

  getTitle () {
    return 'My Data Sets';
  }

  getActionCreators () {
    return ActionCreators;
  }

  loadData () {
    this.dispatchAction(loadUserDatasetList());
  }

  isRenderDataLoaded () {
    return this.state.user != null
      && this.state.config != null
      && this.state.userDatasetsLoading === false
      && this.state.filterByProject != null;
  }

  renderView () {
    const { userDatasets, loadError, config, filterByProject } = this.state;
    const { projectId, displayName: projectName } = config;
    const user: User = this.state.user;
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
      filterByProject,
      ...this.eventHandlers
    };
    return (
      <div className="UserDatasetList-Controller">
        {!loggedIn
          ? <h1 className="UserDatasetList-Title">{title}</h1>
          : null
        }
        <div className="UserDatasetList-Content">
          {!loggedIn
            ? <UserDatasetEmptyState message="Please log in to access My Data Sets."/>
            : <UserDatasetList {...listProps} />
          }
        </div>
      </div>
    )
  }
}

export default wrappable(UserDatasetListController);
