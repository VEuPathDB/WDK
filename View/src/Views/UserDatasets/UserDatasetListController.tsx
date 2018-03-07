import * as React from 'react';
import 'Views/UserDatasets/UserDatasets.scss';
import { wrappable } from 'Utils/ComponentUtils';
import UserDatasetList from 'Views/UserDatasets/UserDatasetList';
import { showLoginForm } from 'Core/ActionCreators/UserActionCreators';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import { loadUserDatasetList } from 'Views/UserDatasets/UserDatasetsActionCreators';
import UserDatasetListStore, { State as StoreState } from "Views/UserDatasets/UserDatasetListStore";

import Icon from 'Components/Icon/IconAlt';

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

  renderNotLoggedInState () {
    return (
      <div className="UserDatasetList-NotLoggedIn">
        <Icon fa="list-alt" />
        <p>Please <a onClick={() => showLoginForm()}>log in</a> to upload and view your user datasets.</p>
      </div>
    );
  }

  renderView () {
    const { user, userDatasets, loadError } = this.state;
    const { history } = this.props;
    const title = this.getTitle();

    const NotLoggedIn = this.renderNotLoggedInState;
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
