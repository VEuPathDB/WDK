import { debounce, flow, get, isEqual, partial } from 'lodash';
import React from 'react';
import ReactDOM from 'react-dom';

import { loadQuestion, updateParamValue, updateDependentParams } from '../actioncreators/QuestionActionCreators';
import * as ParamModules from '../params';
import QuestionStore, { State } from '../stores/QuestionStore';
import { Parameter } from '../utils/WdkModel';
import AbstractViewController from './AbstractViewController';

const ActionCreators = {
  loadQuestion,
  updateParamValue,
  updateDependentParams
}

type Props = {
  questionName: string;
  paramName: string;
  paramValues: Record<string, string>;
}

type QuestionState = State['questions'][string];

export default class LegacyParamController extends AbstractViewController<
  QuestionState,
  QuestionStore,
  typeof ActionCreators,
  Props
> {

  // Apply modifications to event handlers that are scoped to this
  // LegacyParamController instance.
  eventHandlers: typeof ActionCreators = {
    ...this.eventHandlers,

    // XXX This could be more sophisticated:
    // - debounce if prev and next param names are the same
    // - synchronize if prev and next param names are different
    updateDependentParams: debounce(this.eventHandlers.updateDependentParams, 1000)
  }

  paramModules = ParamModules;

  getStoreClass() {
    return QuestionStore;
  }

  getStateFromStore() {
    return get(this.store.getState(), ['questions', this.props.questionName], {}) as QuestionState;
  }

  getActionCreators() {
    return ActionCreators;
  }

  loadData(prevProps?: Props) {
    if (
      this.state.questionStatus == null
    ) {
      this.eventHandlers.loadQuestion(this.props.questionName, this.props.paramValues);
    }

    else if (prevProps != null) {
      let changedParams = Object.entries(this.props.paramValues)
        .filter(([name, value]) => (
          prevProps.paramValues[name] !== value &&
          this.state.paramValues[name] !== value
        ));
      if (changedParams.length > 1) {
        console.warn('Received multiple changed param values: %o', changedParams);
      }
      changedParams.forEach(([name, value]) => {
        let parameter = this.state.question.parameters.find(p => p.name === name);
        if (parameter) {
          this.eventHandlers.updateParamValue(this.getContext(parameter), value);
          this.eventHandlers.updateDependentParams(this.getContext(parameter), value);
        }
      });
    }
  }

  isRenderDataLoadError() {
    return this.state.questionStatus === 'error';
  }

  isRenderDataLoaded() {
    return this.state.questionStatus === 'complete';
  }

  isRenderDataNotFound() {
    return this.state.questionStatus === 'not-found';
  }

  getContext<T extends Parameter>(parameter: T): ParamModules.Context<T> {
    return {
      questionName: this.state.question.name,
      parameter: parameter,
      paramValues: this.state.paramValues
    }
  }

  renderView() {
    const parameter = this.state.question.parameters.find(p => p.name === this.props.paramName);

    if (parameter == null) return null;

    const ctx = this.getContext(parameter);

    return (
      <div>
        <this.paramModules.ParamComponent
          ctx={ctx}
          parameter={parameter}
          dispatch={this.dispatchAction}
          value={this.state.paramValues[parameter.name]}
          uiState={this.state.paramUIState[parameter.name]}
          onParamValueChange={(value: string) => {
            this.eventHandlers.updateParamValue(ctx, value);
            this.eventHandlers.updateDependentParams(ctx, value);
          }}
        />
        <ParamterInput
          name={this.props.paramName}
          value={this.state.paramValues[this.props.paramName]}
        />
      </div>
    )
  }

}

type ParameterInputProps = {
  name: string;
  value: string;
}

/**
 * Input element that emits change events so that it can participate in classic
 * question page (see wdk/js/components/paramterHandlers.js).
 */
class ParamterInput extends React.Component<ParameterInputProps> {

  input: HTMLInputElement | null;

  dispatchChangeEvent = debounce(this._dispatchChangeEvent, 1000);

  componentDidUpdate(prevProps: ParameterInputProps) {
    if (prevProps.value !== this.props.value) {
      this.dispatchChangeEvent();
    }
  }

  _dispatchChangeEvent() {
    if (this.input == null) {
      console.warn("Input field is not defined. Skipping event dispatch.");
      return;
    }
    this.input.dispatchEvent(new Event('change', { bubbles: true }));
  }

  render() {
    return (
      <input
        ref={el => this.input = el}
        type="hidden"
        id={this.props.name}
        name={`value(${this.props.name})`}
        value={this.props.value}
      />
    );
  }

}
