import { groupBy, isEqual, mapValues } from 'lodash';

import { Filter, MemberFilter } from 'Components/AttributeFilter/Utils/FilterService';
import {
  ActiveFieldSetAction,
  FieldStateUpdatedAction,
  FiltersUpdatedAction,
  OntologyTermsInvalidated,
  SummaryCountsLoadedAction,
} from 'Params/FilterParamNew/ActionCreators';
import { sortDistribution } from 'Params/FilterParamNew/Utils';
import { Action } from 'Utils/ActionCreatorUtils';
import { OntologyTermSummary } from 'Utils/WdkModel';


export type SortSpec = {
  groupBySelected: boolean;
  columnKey: keyof OntologyTermSummary['valueCounts'][number];
  direction: 'asc' | 'desc';
};

type BaseFieldState = {
  ontologyTermSummary?: OntologyTermSummary;
  loading?: boolean;
  errorMessage?: string;
}

export type MemberFieldState = BaseFieldState & {
  sort: SortSpec;
  searchTerm: string;
}

export type RangeFieldState = BaseFieldState & {
  yaxisMax?: number;
  xaxisMin?: number;
  xaxisMax?: number;
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
  loadingFilteredCount: boolean;
}>;

export type Value = {
  filters: Filter[]
}

const initialState: State = {
  fieldStates: {},
  loadingFilteredCount: false
}

// FIXME Set loading and error statuses on ontologyTermSummaries entries
export function reduce(state: State = initialState, action: Action): State {
  if (ActiveFieldSetAction.isType(action)) return {
    ...state,
    activeOntologyTerm: action.payload.activeField,
    fieldStates: state.fieldStates[action.payload.activeField] == null ? {
      ...state.fieldStates,
      [action.payload.activeField]: { }
    } : state.fieldStates
  }

  if (SummaryCountsLoadedAction.isType(action)) return {
    ...state,
    loadingFilteredCount: false,
    filteredCount: action.payload.filtered,
    unfilteredCount: action.payload.unfiltered
  }

  if (FieldStateUpdatedAction.isType(action)) return {
    ...state,
    fieldStates: {
      ...state.fieldStates,
      [action.payload.field]: {
        ...state.fieldStates[action.payload.field],
        ...action.payload.fieldState
      }
    }
  }

  if (FiltersUpdatedAction.isType(action)) return {
    ...state,
    loadingFilteredCount: true,
    fieldStates: handleFilterChange(state, action.payload.prevFilters, action.payload.filters)
  }

  if (OntologyTermsInvalidated.isType(action)) return {
    ...state,
    fieldStates: Object.entries(state.fieldStates).reduce((newFieldStates, [ key, fieldState ]) => {
      return Object.assign(newFieldStates, {
        [key]: action.payload.retainedFields.includes(key)
          ? state.fieldStates[key]
          : {
            ...state.fieldStates[key],
            ontologyTermSummary: undefined
          }
      })
    }, {} as Record<string, FieldState>)
  }

  return state;
}

function handleFilterChange(state: State, prevFilters: Filter[], filters: Filter[]) {
  // Get an array of fields whose associated filters have been modified.
  // Concat prev and new filters arrays, then group them by field name
  const modifiedFields = new Set(Object.entries(groupBy(filters.concat(prevFilters), 'field'))
    // keep filters if prev and new are not equal, or if there is only one for a field name (e.g., added/removed)
    .filter(([, filters]) => filters.length === 1 || !isEqual(filters[0], filters[1]))
    .map(([field]) => field));

  return mapValues(state.fieldStates, (fieldState, fieldTerm) => {
    if (modifiedFields.size > 2 || fieldTerm !== state.activeOntologyTerm) {
      fieldState = {
        ...fieldState,
        ontologyTermSummary: undefined
      }
    }
    if (isMemberFieldState(fieldState) && fieldState.ontologyTermSummary && fieldState.sort.groupBySelected) {
      fieldState = {
        ...fieldState,
        ontologyTermSummary: {
          ...fieldState.ontologyTermSummary,
          valueCounts: sortDistribution(
            fieldState.ontologyTermSummary.valueCounts,
            fieldState.sort,
            filters.find(filter => filter.field === fieldTerm) as MemberFilter
          )
        }
      }
    }
    return fieldState;
  })
}

function isMemberFieldState(fieldState: FieldState): fieldState is MemberFieldState {
  return (fieldState as MemberFieldState).sort !== undefined;
}
