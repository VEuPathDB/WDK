import { get } from 'lodash';
import * as React from 'react';

import {
  ActiveQuestionUpdatedAction,
  ParamValueUpdatedAction
} from '../actioncreators/QuestionActionCreators';
import ParamComponent from '../components/Parameter';
import QuestionStore, { State } from '../stores/QuestionStore';
import { wrappable } from '../utils/componentUtils';
import { Seq } from '../utils/IterableUtils';
import { Parameter, ParameterGroup } from '../utils/WdkModel';
import AbstractPageController from './AbstractPageController';

type QuestionState = State['questions'][string];

const ActionCreators = {
  updateParamValue: ParamValueUpdatedAction.create
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

  getContext(parameter: Parameter) {
    const { question, paramValues } = this.state;
    return {
      questionName: question.urlSegment,
      parameter,
      paramValues
    };
  }

  getDependentParams(parameter: Parameter): Seq<Parameter> {
    return Seq.from(parameter.dependentParams)
      .map(name => this.state.question.parametersByName[name])
      .flatMap(dependentParam =>
        Seq.of(dependentParam).concat(this.getDependentParams(dependentParam)))
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
        <ParamComponent
          ctx={ctx}
          parameter={parameter}
          value={paramValues[parameter.name]}
          uiState={paramUIState[parameter.name]}
          dispatch={this.dispatchAction}
          onParamValueChange={paramValue => {
            const dependentParameters = this.getDependentParams(parameter).toArray();
            this.eventHandlers.updateParamValue({ ...ctx, paramValue, dependentParameters });
          }}
        />
      </div>
    );
  }

  renderView() {
    return (
      <div>
        <h1>{this.getTitle()}</h1>
        {this.state.question.groups
         // .filter(g => g.isVisible)
             .map(g => this.renderGroup(g))}
      </div>
    );
  }

}

export default wrappable(QuestionController);
