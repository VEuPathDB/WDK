import * as React from 'react';
import { wrappable } from 'Utils/ComponentUtils';
import AbstractPageController from 'Core/Controllers/AbstractPageController';

import { User } from 'Utils/WdkUser';
import 'Views/UserDatasets/UserDatasets.scss';
import UserDatasetList from 'Views/UserDatasets/List/UserDatasetList';
import { loadUserDatasetList, updateUserDatasetDetail } from 'Views/UserDatasets/UserDatasetsActionCreators';
import UserDatasetListStore, { State as StoreState } from "Views/UserDatasets/List/UserDatasetListStore";

import UserDatasetEmptyState from 'Views/UserDatasets/EmptyState';

const ActionCreators = { loadUserDatasetList, updateUserDatasetDetail };

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
    return this.state.user != null && this.state.userDatasetsLoading === false;
  }

  isRenderDataLoadError () {
    return false;
    // return this.state.loadError != null;
  }

  renderView () {
    const { userDatasets, loadError, config } = this.state;
    const { projectId, displayName } = config ? config : {};
    const user: User = this.state.user;
    const { updateUserDatasetDetail } = this.eventHandlers;
    const { history, location } = this.props;
    
    const title = this.getTitle();
    const loggedIn: boolean = (typeof user !== 'undefined' && user.isGuest === false);
    const content = !loggedIn
      ? (
        <UserDatasetEmptyState message="Please log in to upload and view your user datasets."/>
      ) : (
        <UserDatasetList
          user={user}
          history={history}
          location={location}
          projectId={projectId}
          projectName={displayName}
          userDatasets={userDatasets}
          updateUserDatasetDetail={updateUserDatasetDetail}
        />
      );

    return (
      <div className="UserDatasetList-Controller">
        {!loggedIn
          ? <h1 className="UserDatasetList-Title">{title}</h1>
          : null
        }
        <div className="UserDatasetList-Content">{content}</div>
      </div>
    )
  }
}

export default wrappable(UserDatasetListController);
