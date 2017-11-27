import { debounce, get } from 'lodash';
import * as React from 'react';

import { loadQuestion, updateDependentParams, updateParamValue } from '../actioncreators/QuestionActionCreators';
import * as ParamModules from '../params';
import * as p from '../params';
import QuestionStore, { State } from '../stores/QuestionStore';
import { wrappable } from '../utils/componentUtils';
import { Parameter, RecordClass, ParameterGroup } from '../utils/WdkModel';
import AbstractPageController from './AbstractPageController';
import { Seq } from "../utils/IterableUtils";

type QuestionState = State['questions'][string];

const ActionCreators = {
  updateParamValue,
  updateDependentParams
}

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

  // Apply modifications to event handlers that are scoped to this
  // LegacyParamController instance.
  eventHandlers: typeof ActionCreators = {
    ...this.eventHandlers,

    // XXX This could be more sophisticated:
    // - latest if prev and next param names are the same
    // - synchronize if prev and next param names are different
    updateDependentParams: debounce(this.eventHandlers.updateDependentParams, 1000)
  }

  paramModules = ParamModules;

  loadData() {
    if (this.state.questionStatus == null) {
      this.dispatchAction(loadQuestion(this.props.match.params.question));
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

  getContext(parameter: Parameter) {
    const { question, paramValues } = this.state;
    return {
      questionName: question.urlSegment,
      parameter,
      paramValues
    };
  }

  renderGroup(group: ParameterGroup) {
    return (
      <div key={group.name}>
        <h2>{group.displayName}</h2>
        {Seq.from(group.parameters)
          .map(pName => this.state.question.parametersByName[pName])
          .filter(p => p.isVisible)
          .map(p => this.renderParameter(p))}
      </div>
    )
  }

  renderParameter(parameter: Parameter) {
    const { paramValues, paramUIState } = this.state;
    const ctx = this.getContext(parameter);
    return (
      <div key={parameter.name}>
        <h3>{parameter.displayName}</h3>
        <p.ParamComponent
          ctx={ctx}
          parameter={parameter}
          value={paramValues[parameter.name]}
          uiState={paramUIState[parameter.name]}
          dispatch={this.dispatchAction}
          onParamValueChange={value => {
            this.eventHandlers.updateParamValue(ctx, value);
            this.eventHandlers.updateDependentParams(ctx, value);
          }}
        />
      </div>
    );
  }

  renderView() {
    return (
      <div>
        <h1>{this.getTitle()}</h1>
        {this.state.question.groups.filter(g => g.isVisible).map(g => this.renderGroup(g))}
      </div>
    );
  }

}

export default wrappable(QuestionController);
