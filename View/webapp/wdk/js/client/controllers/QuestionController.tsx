import Icon from '../components/Icon';
import { get } from 'lodash';
import * as React from 'react';

import {
  ActiveQuestionUpdatedAction,
  GroupStateUpdatedAction,
  GroupVisibilityChangedAction,
  ParamValueUpdatedAction,
} from '../actioncreators/QuestionActionCreators';
import DefaultQuestionForm from '../components/DefaultQuestionForm';
import QuestionStore, { State } from '../stores/QuestionStore';
import { wrappable } from '../utils/componentUtils';
import { Seq } from '../utils/IterableUtils';
import { Parameter, ParameterGroup } from '../utils/WdkModel';
import AbstractPageController from './AbstractPageController';

type QuestionState = State['questions'][string];

const ActionCreators = {
  updateParamValue: ParamValueUpdatedAction.create,
  setGroupVisibility: GroupVisibilityChangedAction.create
}

export type EventHandlers = typeof ActionCreators;

class QuestionController extends AbstractPageController<QuestionState, QuestionStore, typeof ActionCreators> {

  getActionCreators() {
    return ActionCreators;
  }

  getStoreClass() {
    return QuestionStore;
  }

  getStateFromStore() {
    return get(this.store.getState(), ['questions', this.props.match.params.question], {}) as QuestionState;
  }

  loadData() {
    if (this.state.questionStatus == null) {
      this.dispatchAction(ActiveQuestionUpdatedAction.create({
        questionName: this.props.match.params.question
      }));
    }
  }

  isRenderDataLoaded() {
    return this.state.questionStatus === 'complete';
  }

  isRenderDataLoadError() {
    return this.state.questionStatus === 'error';
  }

  isRenderDataNotFound() {
    return this.state.questionStatus === 'not-found';
  }

  getTitle() {
    return !this.state.question || !this.state.recordClass ? 'Loading' :
      `Search for ${this.state.recordClass.displayNamePlural}
      by ${this.state.question.displayName}`;
  }

  renderView() {
    return (
      <DefaultQuestionForm
        state={this.state}
        eventHandlers={this.eventHandlers}
        dispatchAction={this.dispatchAction}
      />
    );
  }

}

export default wrappable(QuestionController);
