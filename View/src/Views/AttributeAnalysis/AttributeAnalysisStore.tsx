import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import { Action, Epic } from 'Utils/ActionCreatorUtils';

import * as Data from './BaseAttributeAnalysis';
import { ScopedAnalysisAction } from './BaseAttributeAnalysis/BaseAttributeAnalysisActions';
import * as Histogram from './HistogramAnalysis';
import * as WordCloud from './WordCloudAnalysis';

export type State = BaseState & {
  analyses: Record<string, WordCloud.State | Histogram.State | undefined>
};

export class AttributeAnalysisStore extends WdkStore<State> {

  getInitialState() {
    return {
      ...super.getInitialState(),
      analyses: {}
    }
  }

  handleAction(state: State, action: Action): State {
    if (ScopedAnalysisAction.test(action)) {
      const key = action.payload.stepId + '__' + action.payload.reporter.name;
      let childState = state.analyses[key];
      switch(action.payload.reporter.type) {
        case 'wordCloud': {
          childState = WordCloud.reduce(childState as WordCloud.State, action.payload.action);
          break;
        }
        case 'histogram': {
          childState = Histogram.reduce(childState as Histogram.State, action.payload.action);
          break;
        }
      }
      return {
        ...state,
        analyses: {
          ...state.analyses,
          [key]: childState
        }
      }
    }
    return state;
  }

  getEpics() {
    return [
      scopeEpic(Data.observe)
    ]
  }

}

function scopeEpic(epic: Epic): Epic {
  return function scopedEpic(action$, services) {
    const scopedParentAction$ = action$.filter(ScopedAnalysisAction.test);
    const scopedChildAction$ = epic(scopedParentAction$.map(action => action.payload.action), services);
    return scopedChildAction$.withLatestFrom(scopedParentAction$, (child, parent) => ({ child, parent }))
      .map(({ child, parent }) => {
        return { ...parent, payload: { ...parent.payload, action: child } }
      })
  }
}