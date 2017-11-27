import { isEqual } from 'lodash';

import { ActionThunk } from '../../ActionCreator';
import { Filter, MemberFilter } from '../../utils/FilterService';
import { FilterParamNew } from '../../utils/WdkModel';
import { Context } from '../index';
import { ACTIVE_FIELD_SET, FIELD_STATE_UPDATED, FILTERS_UPDATED, SUMMARY_COUNTS_LOADED } from './Constants';
import { FieldState, MemberFieldState, SortSpec } from './State';
import { findFirstLeaf, getFilters, isMemberField, sortDistribution } from './Utils';

type Ctx = Context<FilterParamNew>

const defaultMemberFieldSort: MemberFieldState['sort'] = {
  columnKey: 'value',
  direction: 'asc',
  groupBySelected: false
}

// Actions
// -------

// Properties we expect for all FilterParamNew Actions
type ActionBase = {
  payload: Context<FilterParamNew>
}

export type Action = ActionBase & (
  {
    type: typeof ACTIVE_FIELD_SET,
    payload: { activeField: string }
  } | {
    type: typeof SUMMARY_COUNTS_LOADED,
    payload: { filtered: number, unfiltered: number }
  } | {
    type: typeof FIELD_STATE_UPDATED,
    payload: { field: string, fieldState: Record<string, any> }
  } | {
    type: typeof FILTERS_UPDATED,
    payload: { prevFilters: Filter[], filters: Filter[] }
  }
)

// ActionCreators
// --------------

export function init(ctx: Ctx): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    const filters = getFiltersFromContext(ctx);
    const activeField = filters.length === 0 ? findFirstLeaf(getOntologyFromContext(ctx)) : filters[0].field;
    dispatch(updateActiveField(ctx, activeField, filters, true));
    dispatch(updateSummaryCounts(ctx, filters));
  }
}

export function updateActiveField(ctx: Ctx, activeField: string, allFilters: Filter[], loadSummary: boolean): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    const { parameter, questionName, paramValues } = ctx;
    dispatch({ type: ACTIVE_FIELD_SET, payload: { ...ctx, activeField }});

    if (loadSummary) {
      const filters = allFilters.filter(f => f.field !== activeField);
      dispatch(updateFieldState(ctx, activeField, { loading: true }))

      wdkService.getOntologyTermSummary(questionName, parameter.name, filters, activeField, paramValues).then(
        summary => {
          let fieldState: FieldState = isMemberField(parameter, activeField) ? {
            loading: false,
            sort: defaultMemberFieldSort,
            ontologyTermSummary: sortDistribution(summary, defaultMemberFieldSort)
          } : {
            loading: false,
            ontologyTermSummary: summary
          };
          dispatch(updateFieldState(ctx, activeField, fieldState))
        },
        error => {
          dispatch(updateFieldState(ctx, activeField, {
            loading: false,
            errorMessage: 'Unable to load ontologyTermSummary for "' + activeField + '".'
          }));
          console.error(error);
        }
      )
    }
  }
}

export function updateSummaryCounts(ctx: Ctx, filters: Filter[]): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    const { parameter, questionName, paramValues } = ctx;
    wdkService.getFilterParamSummaryCounts(questionName, parameter.name, filters, paramValues).then(
      counts => {
        dispatch({ type: SUMMARY_COUNTS_LOADED, payload: { ...counts, ...ctx } })
      }
    )
  };
}

export function updateFilters(ctx: Ctx, filters: Filter[], activeField?: string): ActionThunk<Action> {
  return dispatch => {
    const prevFilters = getFiltersFromContext(ctx);

    dispatch({
      type: FILTERS_UPDATED,
      payload: { ...ctx, prevFilters, filters }
    });

    if (activeField != null) {
      // Update summary counts for active field if other field filters have been modified
      const prevOtherFilters = prevFilters.filter(f => f.field != activeField);
      const otherFilters = filters.filter(f => f.field !== activeField);
      if (!isEqual(prevOtherFilters, otherFilters)) {
        dispatch(updateActiveField(ctx, activeField, filters, true));
      }
    }

  };
}


// membership filter action creators
// ---------------------------------

export function updateMemberFieldSort(ctx: Ctx, field: string, prevFieldState: FieldState, sort: SortSpec): Action {
  const { paramValues, parameter } = ctx;
  const filters = getFilters(paramValues[parameter.name]);
  return updateFieldState(ctx, field, {
    sort,
    ontologyTermSummary: sortDistribution(prevFieldState.ontologyTermSummary, sort, filters.find(f => f.field === field) as MemberFilter)
  })
}

// XXX Should these be coarse or fine grained?
export function updateFieldState(ctx: Ctx, field: string, fieldState: Record<string, any>): Action {
  return {
    type: FIELD_STATE_UPDATED,
    payload: { ...ctx, field, fieldState }
  };
}

function getFiltersFromContext(ctx: Ctx) {
  return getFilters(ctx.paramValues[ctx.parameter.name]);
}

function getOntologyFromContext(ctx: Ctx) {
  return ctx.parameter.ontology;
}
