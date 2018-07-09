import { matchAction, Reducer, combineReducers } from 'Utils/ReducerUtils';
import { ServiceError } from 'Utils/WdkService';

import {
  AttributeReportCancelled,
  AttributeReportFailed,
  AttributeReportReceived,
  AttributeReportRequested,
  TableSorted,
  TableSearched,
  TabSelected
} from './BaseAttributeAnalysisActions';


// Report state
// ------------

type InitialState = { status: 'idle' };
type FetchingState = { status: 'fetching' };
type ErrorState = { status: 'error', error: ServiceError };
type SuccessState = { status: 'success', report: any };

type ReportState = InitialState | FetchingState | ErrorState | SuccessState;

const reduceReport = <Reducer<ReportState>>matchAction({ status: 'idle' },
  [AttributeReportRequested, () => ({ status: 'fetching' })],
  [AttributeReportReceived, (state, { report }) => ({ status: 'success', report })],
  [AttributeReportFailed, (state, { error }) => ({ status: 'error', error })],
  [AttributeReportCancelled, () => ({ status: 'idle' })]
)


// Table state
// -----------

type TableState<T extends string> = {
  sort: { key: T; direction: 'asc' | 'desc'; }
  search: string;
}

const makeReduceTable = <T extends string>(init: TableState<T>) => <Reducer<TableState<T>>>matchAction(init,
  [AttributeReportCancelled, (state) => init],
  [TableSorted, (state, sort) => ({ ...state, sort })],
  [TableSearched, (state, search) => ({ ...state, search })],
)


// Tabs state
// ----------

type TabsState = {
  activeTab: 'table' | 'visualization';

}

const reduceTabs = <Reducer<TabsState>>matchAction({ activeTab: 'visualization' },
  [AttributeReportCancelled, (state) => ({ activeTab: 'visualization' })],
  [TabSelected, (state, activeTab) => ({ ...state, activeTab })]
)


// Composite state
// ---------------

export type State<T extends string, S = {}> = {
  data: ReportState;
  table: TableState<T>;
  tabs: TabsState;
  visualization: S;
}

export const makeReduce = <T extends string, S>(initTableState: TableState<T>, reduceVisualization: Reducer<S> = () => ({} as S)): Reducer<State<T, S>> => combineReducers({
  data: reduceReport,
  table: makeReduceTable<T>(initTableState),
  tabs: reduceTabs,
  visualization: reduceVisualization
})