import { matchAction } from 'Utils/ReducerUtils';

import { makeReduce, State as BaseState } from '../BaseAttributeAnalysis';
import { DisplayType, SetBinSize, SetDisplayType } from './HistogramActions';
import { AttributeReportReceived } from '../BaseAttributeAnalysis/BaseAttributeAnalysisActions';

type HistogramState = {
  binSize: number;
  displayType: DisplayType;
}

export type State = BaseState<'attrValue' | 'recordCount', HistogramState>;

const reduceHistogram = matchAction({} as HistogramState,
  [AttributeReportReceived, (state, { report }): HistogramState => ({
    ...state,
    binSize: report.binSize,
    displayType: 'normal'
  })],
  [SetBinSize, (state, binSize): HistogramState => ({ ...state, binSize })],
  [SetDisplayType, (state, displayType): HistogramState => ({ ...state, displayType })],
)

export const reduce = makeReduce<'attrValue' | 'recordCount', HistogramState>(
  { sort: { key: 'attrValue', direction: 'asc' }, search: ''},
  reduceHistogram
);