import { Observable } from 'rxjs';

import { ActionCreatorServices, combineEpics } from '../../utils/ActionCreatorUtils';
import { ParamLoadedAction, ParamValueUpdatedAction } from '../../actioncreators/QuestionActionCreators';
import { Action } from '../../dispatcher/Dispatcher';
import { makeActionCreator, payload } from '../../utils/ActionCreatorUtils';
import { Filter } from '../../utils/FilterService';
import { FilterParamNew, ParameterValues } from '../../utils/WdkModel';
import { Context, isContextType } from '../Utils';
import { FieldState, MemberFieldState } from './State';
import { isType } from './Utils';
import { findFirstLeaf, getFilters, isMemberField, sortDistribution } from './Utils';
import WdkService from '../../utils/WdkService';


type Ctx = Context<FilterParamNew>

const defaultMemberFieldSort: MemberFieldState['sort'] = {
  columnKey: 'value',
  direction: 'asc',
  groupBySelected: false
}

// Action Creators
// ---------------

export const ActiveFieldSetAction = makeActionCreator(
  'filter-param-new/active-field-set',
  payload<Context<FilterParamNew> & {
    activeField: string;
    loadOntologyTermSummary: boolean;
  }>()
)

export const SummaryCountsLoadedAction = makeActionCreator(
  'filter-param-new/summary-counts-loaded',
  payload<Context<FilterParamNew> & {
    filtered: number;
    unfiltered: number;
  }>()
)

export const FieldStateUpdatedAction = makeActionCreator(
  'filter-param-new/field-state-updated',
  payload<Context<FilterParamNew> & {
    field: string;
    fieldState: Record<string, any>;
  }>()
)

export const FiltersUpdatedAction = makeActionCreator(
  'filter-param-new/filters-updated',
  payload<Context<FilterParamNew> & {
    prevFilters: Filter[];
    filters: Filter[];
  }>()
)

// Epics
// -----

// export default combineEpics(initEpic, updateActiveFieldEpic, updateSummaryCountsEpic);

export default function initEpic(action$: Observable<Action>, services: ActionCreatorServices): Observable<Action> {
  return action$
    .filter(ParamLoadedAction.isType)
    .mergeMap(action => {
      if (!isContextType(action.payload.ctx, isType)) return Observable.empty();

      const { ctx } = action.payload;
      const filters = getFiltersFromContext(ctx);
      const activeField = filters.length === 0 ? findFirstLeaf(getOntologyFromContext(ctx)) : filters[0].field;
      const paramAction$ = action$.filter(action => (
        (ActiveFieldSetAction.isType(action) || FiltersUpdatedAction.isType(action)) &&
        action.payload.questionName === ctx.questionName &&
        action.payload.parameter.name === ctx.parameter.name
      ));

      // The order here is important. We want to first merge the child epics
      // (updateActiveFieldEpic and ypdateSummaryCountsEpic), THEN we want to
      // merge the Observable of ActiveFieldSetAction. This will ensure that
      // updateActiveFieldEpic receives that action.
      return updateActiveFieldEpic(paramAction$, services)
        .merge(updateSummaryCountsEpic(paramAction$, services))
        .merge(Observable.of(ActiveFieldSetAction.create({ ...ctx, activeField, loadOntologyTermSummary: true })));
    })
}

export function updateActiveFieldEpic(action$: Observable<Action>, { wdkService }: ActionCreatorServices): Observable<Action> {
  return action$
    .filter(ActiveFieldSetAction.isType)
    .filter(action => action.payload.loadOntologyTermSummary)
    .debounceTime(1000)
    .mergeMap(action => {
      const { questionName, paramValues, parameter, activeField } = action.payload;
      const filters = getFiltersFromContext(action.payload);
      return Observable.from(getOntologyTermSummary(wdkService, activeField, questionName, parameter, filters, paramValues))
        .merge(getSummaryCounts(wdkService, questionName, parameter, filters, paramValues))
        .takeUntil(action$.filter(ActiveFieldSetAction.isType));
    })
}

export function updateSummaryCountsEpic(action$: Observable<Action>, { wdkService }: ActionCreatorServices) {
  return action$.filter(FiltersUpdatedAction.isType)
    .debounceTime(1000)
    .mergeMap(action => {
      const { parameter, questionName, paramValues, filters } = action.payload;
      return Observable.from(getSummaryCounts(wdkService, questionName, parameter, filters, paramValues))
        .takeUntil(action$.filter(FiltersUpdatedAction.isType));
    })
}


// Helpers
// -------

function getOntologyTermSummary(
  wdkService: WdkService,
  activeField: string,
  questionName: string,
  parameter: FilterParamNew,
  filters: Filter[],
  paramValues: ParameterValues
) {
  return wdkService.getOntologyTermSummary(questionName, parameter.name, filters, activeField, paramValues).then(
    summary => {
      let fieldState: FieldState = isMemberField(parameter, activeField) ? {
        loading: false,
        sort: defaultMemberFieldSort,
        ontologyTermSummary: sortDistribution(summary, defaultMemberFieldSort)
      } : {
          loading: false,
          ontologyTermSummary: summary
        };
      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: activeField,
        fieldState
      })
    },
    error => {
      console.error(error);
      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: activeField,
        fieldState: {
          loading: false,
          errorMessage: 'Unable to load ontologyTermSummary for "' + activeField + '".'
        }
      });
    }
  )
}

function getSummaryCounts(
  wdkService: WdkService,
  questionName: string,
  parameter: FilterParamNew,
  filters: Filter[],
  paramValues: ParameterValues
) {
  return wdkService.getFilterParamSummaryCounts(questionName, parameter.name, filters, paramValues).then(
    counts => SummaryCountsLoadedAction.create({
      questionName,
      parameter,
      paramValues,
      ...counts
    })
  )
}

function getFiltersFromContext(ctx: Ctx) {
  return getFilters(ctx.paramValues[ctx.parameter.name]);
}

function getOntologyFromContext(ctx: Ctx) {
  return ctx.parameter.ontology;
}
