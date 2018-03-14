import * as React from 'react';
import { keyBy, pick } from 'lodash';
import { ViewControllerProps } from 'Core/CommonTypes';
import { wrappable } from 'Utils/ComponentUtils';
import { Question } from 'Utils/WdkModel';
import AbstractPageController, { PageControllerProps } from 'Core/Controllers/AbstractPageController';

import NotLoggedIn from 'Views/UserDatasets/NotLoggedIn';
import UserDatasetDetail from 'Views/UserDatasets/Detail/UserDatasetDetail';
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
    const idChanged = prevProps && prevProps.match.params.id !== this.props.match.params.id;
    if (idChanged || !this.state.userDatasetsById[this.props.match.params.id]) {
      this.eventHandlers.loadUserDatasetDetail(Number(this.props.match.params.id));
    }
  }

  isRenderDataLoadError () {
    return !this.props.user.isGuest && this.state.loadError != null;
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

  renderView () {
    const entry = this.state.userDatasetsById[this.props.match.params.id];
    const isOwner = (
      entry.resource != null &&
      this.state.user != null &&
      entry.resource.ownerUserId === this.state.user.id
    )
    return this.props.user && this.props.user.isGuest
      ? <NotLoggedIn/>
      : <UserDatasetDetail
          getQuestionUrl={this.getQuestionUrl}
          userDataset={entry.resource!}
          updateUserDatasetDetail={this.eventHandlers.updateUserDatasetDetail}
          userDatasetUpdating={this.state.userDatasetUpdating}
          updateError={this.state.updateError}
          isOwner={isOwner}
          questionMap={keyBy(this.state.questions, 'name')}
        />
  }
}

export default wrappable(UserDatasetDetailController);
