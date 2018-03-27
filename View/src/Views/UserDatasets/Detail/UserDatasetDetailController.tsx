import * as React from 'react';
import { keyBy, pick } from 'lodash';
import { PageControllerProps } from 'Core/CommonTypes';
import { wrappable } from 'Utils/ComponentUtils';
import { Question } from 'Utils/WdkModel';
import AbstractPageController from 'Core/Controllers/AbstractPageController';

import NotLoggedIn from 'Views/UserDatasets/NotLoggedIn';
import UserDatasetDetail from 'Views/UserDatasets/Detail/UserDatasetDetailNew';
import UserDatasetDetailStore, { State as StoreState } from 'Views/UserDatasets/Detail/UserDatasetDetailStore';
import { loadUserDatasetDetail, updateUserDatasetDetail } from 'Views/UserDatasets/UserDatasetsActionCreators';

type State = Pick<StoreState, 'userDatasetsById' | 'loadError' | 'userDatasetUpdating' | 'updateError'>
           & Pick<StoreState["globalData"], 'user' | 'questions' | 'config'>;

const ActionCreators = { loadUserDatasetDetail, updateUserDatasetDetail };

type EventHandlers = typeof ActionCreators;

/**
 * View Controller for User Dataset record.
 *
 * Note that we are accessing the userDataset from an object keyed by the
 * userDataset's id. This avoids race conditions that arise when ajax requests
 * complete in a different order than they were invoked.
 */
class UserDatasetDetailController extends AbstractPageController <State, UserDatasetDetailStore, EventHandlers> {
  props: any;

  getQuestionUrl = (question: Question): string => {
    return `#${question.name}`;
  }

  getStoreClass(): typeof UserDatasetDetailStore {
    return UserDatasetDetailStore;
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

  getTitle () {
    const entry = this.state.userDatasetsById[this.props.match.params.id];
    if (entry && entry.resource) {
      return `User Dataset ${entry.resource.meta.name}`;
    }
    if (entry && !entry.resource) {
      return `User Dataset not found`;
    }
    return `User Dataset ...`;
  }

  getActionCreators () {
    return ActionCreators;
  }

  loadData (prevProps?: PageControllerProps<UserDatasetDetailStore>) {
    const { match } = this.props;
    const { userDatasetsById } = this.state;
    const idChanged = prevProps && prevProps.match.params.id !== match.params.id;
    if (idChanged || !userDatasetsById[match.params.id]) {
      this.eventHandlers.loadUserDatasetDetail(Number(match.params.id));
    }
  }

  isRenderDataLoadError () {
    const { loadError, user } = this.state;
    return (!user || user.isGuest) && loadError != null;
  }

  isRenderDataLoaded () {
    const { match } = this.props;
    const { userDatasetsById, user, questions, config } = this.state;
    const entry = userDatasetsById[match.params.id];
    return (entry && !entry.isLoading && user && questions && config)
      ? true
      : false;
  }

  renderView () {
    const { match } = this.props;
    const { userDatasetsById, user } = this.state;
    const entry = userDatasetsById[match.params.id];
    const isOwner = (user && entry.resource && entry.resource.ownerUserId === user.id);
    return user && user.isGuest
      ? <NotLoggedIn/>
      : <UserDatasetDetail
          isOwner={!!isOwner}
          userDataset={entry.resource!}
          getQuestionUrl={this.getQuestionUrl}
          updateError={this.state.updateError}
          questionMap={keyBy(this.state.questions, 'name')}
          userDatasetUpdating={this.state.userDatasetUpdating}
          updateUserDatasetDetail={this.eventHandlers.updateUserDatasetDetail}
        />
  }
}

export default wrappable(UserDatasetDetailController);
