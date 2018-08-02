import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import { Action, Epic } from 'Utils/ActionCreatorUtils';

import * as Data from './BaseAttributeAnalysis';
import { ScopedAnalysisAction } from './BaseAttributeAnalysis/BaseAttributeAnalysisActions';
import { CompositeClientPlugin, PluginContext } from 'Utils/ClientPlugin';

export type State = BaseState & {
  analyses: Record<string, Data.State<string> | undefined>
};

export class AttributeAnalysisStore extends WdkStore<State> {

  getInitialState() {
    return {
      ...super.getInitialState(),
      analyses: {}
    }
  }

  handleAction(state: State, action: Action): State {
    if (!ScopedAnalysisAction.test(action)) return state;

    const { stepId, reporter, context } = action.payload;
    const key = stepId + '__' + reporter.name;
    return {
      ...state,
      analyses: {
        ...state.analyses,
        [key]: this.locatePlugin('attributeAnalysis').reduce(context, state.analyses[key], action.payload.action)
      }
    };
  }

  getEpics() {
    return [
      scopePluginObserve(this.locatePlugin('attributeAnalysis').observe)
    ]
  }

}

function scopePluginObserve(epic: CompositeClientPlugin['observe']): Epic {
  return function scopedEpic(action$, services) {
    const scopedParentAction$ = action$.filter(ScopedAnalysisAction.test);
    const contextActionPair$ = scopedParentAction$.map(action => [ action.payload.context, action.payload.action ] as [PluginContext, Action]);
    const scopedChildAction$ = epic(contextActionPair$, services);
    return scopedChildAction$.withLatestFrom(scopedParentAction$, (child, parent) => ({ child, parent }))
      .map(({ child, parent }) => {
        return { ...parent, payload: { ...parent.payload, action: child } }
      })
  }
}