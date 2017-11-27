import { keyBy, mapValues } from 'lodash';

import * as AttributeFilter from '../../utils/AttributeFilter';
import { Filter } from '../../utils/FilterService';
import { OntologyTermSummary } from '../../utils/WdkModel';
import { Action } from './ActionCreators';
import * as ActionTypes from './Constants';


export type SortSpec = {
  groupBySelected: boolean;
  columnKey: string;
  direction: string;
};

type BaseFieldState = {
  ontologyTermSummary?: OntologyTermSummary;
  loading?: boolean;
  errorMessage?: string;
}

export type MemberFieldState = BaseFieldState & {
  sort: SortSpec;
}

export type RangeFieldState = BaseFieldState & {
  // TODO Store x- and y-axis settings
}

export type FieldState = MemberFieldState | RangeFieldState;

export type State = Readonly<{
  errorMessage?: string;
  loading?: boolean;
  activeOntologyTerm?: string;
  hideFilterPanel?: boolean;
  hideFieldPanel?: boolean;
  fieldStates: Readonly<Record<string, FieldState>>;
  filteredCount?: number;
  unfilteredCount?: number;
}>;

export type Value = {
  filters: AttributeFilter.Filter[]
}

const initialState: State = {
  fieldStates: {}
}

// FIXME Set loading and error statuses on ontologyTermSummaries entries
export function reduce(state: State = initialState, action: Action): State {
  switch (action.type) {

    case ActionTypes.ACTIVE_FIELD_SET:
      return {
        ...state,
        activeOntologyTerm: action.payload.activeField,
        fieldStates: state.fieldStates[action.payload.activeField] == null ? {
          ...state.fieldStates,
          [action.payload.activeField]: { }
        } : state.fieldStates
      }

    case ActionTypes.SUMMARY_COUNTS_LOADED:
      return {
        ...state,
        filteredCount: action.payload.filtered,
        unfilteredCount: action.payload.unfiltered
      }

    case ActionTypes.FIELD_STATE_UPDATED:
      return {
        ...state,
        fieldStates: {
          ...state.fieldStates,
          [action.payload.field]: {
            ...state.fieldStates[action.payload.field],
            ...action.payload.fieldState
          }
        }
      }

    case ActionTypes.FILTERS_UPDATED:
      return {
        ...state,
        fieldStates: handleFilterChange(state, action.payload.prevFilters, action.payload.filters)
      }

  }

  return state;
}

function handleFilterChange(state: State, prevFilters: Filter[], filters: Filter[]) {
  // for each changed member filter, set `groupBySelected` to false
  let filtersByField = keyBy(filters, 'field');
  let prevFiltersByField = keyBy(prevFilters, 'field');
  return mapValues(state.fieldStates, (fieldState, fieldTerm) => {
    if (filtersByField[fieldTerm] !== prevFiltersByField[fieldTerm]) {
      fieldState = fieldTerm == state.activeOntologyTerm ? fieldState : {
        ...fieldState,
        ontologyTermSummary: undefined
      };
      if(
        isMemberFieldState(fieldState) &&
        fieldState.sort.groupBySelected
      ) {
        return Object.assign({}, fieldState, {
          sort: Object.assign({}, fieldState.sort, { groupBySelected: false })
        });
      }
    }
    return fieldState;
  })
}

function isMemberFieldState(fieldState: FieldState): fieldState is MemberFieldState {
  return (fieldState as MemberFieldState).sort !== undefined;
}
