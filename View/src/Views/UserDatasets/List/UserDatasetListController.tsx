import * as React from 'react';
import { wrappable } from 'Utils/ComponentUtils';
import AbstractPageController from 'Core/Controllers/AbstractPageController';

import 'Views/UserDatasets/UserDatasets.scss';
import UserDatasetList from 'Views/UserDatasets/List/UserDatasetList';
import { loadUserDatasetList } from 'Views/UserDatasets/UserDatasetsActionCreators';
import UserDatasetListStore, { State as StoreState } from "Views/UserDatasets/List/UserDatasetListStore";

import NotLoggedIn from 'Views/UserDatasets/NotLoggedIn';

const ActionCreators = { loadUserDatasetList };

type State = Pick<StoreState, 'userDatasetsLoading' | 'userDatasets' | 'loadError'>
           & Pick<StoreState["globalData"], 'user'>;

class UserDatasetListController extends AbstractPageController <State, UserDatasetListStore, typeof ActionCreators> {
  getStoreClass () {
    return UserDatasetListStore;
  }

  getStateFromStore () {
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
    const { user, userDatasets, loadError } = this.state;
    const { history } = this.props;
    const title = this.getTitle();
    const content =  !user || (user && user.isGuest)
      ? <NotLoggedIn />
      : <UserDatasetList userDatasets={userDatasets} user={user} history={history} />;

    return (
      <div className="UserDatasetList-Controller">
        <h1 className="UserDatasetList-Title">{title}</h1>
        <div className="UserDatasetList-Content">{content}</div>
      </div>
    )
  }
}

export default wrappable(UserDatasetListController);
