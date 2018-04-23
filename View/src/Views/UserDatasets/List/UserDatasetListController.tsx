import * as React from 'react';

import 'Views/UserDatasets/UserDatasets.scss';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import { wrappable } from 'Utils/ComponentUtils';
import { UserDataset } from 'Utils/WdkModel';
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
  updateUserDatasetDetail,
} from 'Views/UserDatasets/UserDatasetsActionCreators';

const ActionCreators = {
  updateUserDatasetDetail,
  removeUserDataset,
  shareUserDatasets,
  unshareUserDatasets,
  updateProjectFilter
};

class UserDatasetListController extends AbstractPageController <StoreState, UserDatasetListStore, typeof ActionCreators> {

  getStoreClass () {
    return UserDatasetListStore;
  }

  getStateFromStore () {
    return this.store.getState();
  }

  getTitle () {
    return 'My Data Sets';
  }

  getActionCreators () {
    return ActionCreators;
  }

  loadData () {
    if (this.state.status === 'not-requested') {
      this.dispatchAction(loadUserDatasetList());
    }
  }

  isRenderDataLoaded () {
    return this.state.status === 'complete';
  }

  isRenderDataLoadError() {
    return this.state.status === 'error';
  }

  renderView () {
    if (this.state.status !== 'complete') return null;

    const { userDatasets, userDatasetsById, filterByProject, globalData: { user, config } } = this.state;
    const { projectId, displayName: projectName } = config;
    const { history, location } = this.props;

    const title = this.getTitle();
    const loggedIn: boolean = (typeof user !== 'undefined' && user.isGuest === false);
    const listProps = {
      user,
      history,
      location,
      projectId,
      projectName,
      userDatasets: userDatasets.map(id => userDatasetsById[id].resource) as UserDataset[],
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
