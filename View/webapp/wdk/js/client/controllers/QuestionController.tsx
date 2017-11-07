import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractPageController from './AbstractPageController';
import { loadQuestion } from '../actioncreators/QuestionActionCreators';
import { State, default as QuestionStore } from "../stores/QuestionStore";

const ActionCreators = { loadQuestion }

export default wrappable(class QuestionController extends AbstractPageController<State, QuestionStore, typeof ActionCreators> {

  getActionCreators() {
    return ActionCreators;
  }

  getStoreClass() {
    return QuestionStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  loadData() {
    this.eventHandlers.loadQuestion(this.props.match.params.question);
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
    return !this.state.question ? 'Loading' :
        `Search for ${this.props.match.params.recordClass} by ${this.state.question.displayName}`;
  }

  renderView() {
    const { question } = this.state;

    return (
      <div>
        <h1>{this.getTitle()}</h1>
        <pre>{JSON.stringify(question, null, 4)}</pre>
      </div>
    );
  }

})
