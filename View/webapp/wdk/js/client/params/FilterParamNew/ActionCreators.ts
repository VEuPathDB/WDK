import { Observable } from 'rxjs';

import {
  GroupVisibilityChangedAction,
  ParamValueUpdatedAction,
  QuestionLoadedAction,
  ParamErrorAction
} from '../../actioncreators/QuestionActionCreators';
import { Action } from '../../dispatcher/Dispatcher';
import QuestionStore, { QuestionState } from '../../stores/QuestionStore';
import { makeActionCreator, payload } from '../../utils/ActionCreatorUtils';
import { combineEpics, EpicServices } from '../../utils/ActionCreatorUtils';
import { Filter } from '../../utils/FilterService';
import { FilterParamNew } from '../../utils/WdkModel';
import WdkService from '../../utils/WdkService';
import { Context } from '../Utils';
import { FieldState, MemberFieldState, State } from './State';
import { isType } from './Utils';
import { findFirstLeaf, getFilters, isMemberField, sortDistribution } from './Utils';
import { groupBy, isEqual } from 'lodash';


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

export const OntologyTermsInvalidated = makeActionCreator(
  'filter-param-new/ontology-terms-invalidated',
  payload<Context<FilterParamNew> & {
    retainedFields: string[]
  }>()
)

// Epics
// -----

export default combineEpics(initEpic, updateDependentParamsActiveFieldEpic);

/**
 * When a Question is loaded, listen for parameter-specific actions and load data as needed.
 */
function initEpic(action$: Observable<Action>, services: EpicServices<QuestionStore>): Observable<Action> {
  return action$
    .filter(QuestionLoadedAction.isType)
    .mergeMap(action => {
      const { questionName } = action.payload;
      const { question, paramValues } = services.store.state.questions[questionName];
      const isVisible = (parameter: FilterParamNew) => (
        services.store.state.questions[questionName].groupUIState[parameter.group].isVisible
      )

      // Create an observable per filter param to load ontology term summaries
      // and counts when an active ontology term is changed, or when a param
      // value changes, but only if its group is visible.
      return Observable.from(question.parameters)
        .filter(isType)
        .mergeMap(parameter => {
          // Create an Observable<FilterParamNew> based on actions
          const valueChangedParameter$ = action$.filter(FiltersUpdatedAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === parameter.name)
            .map(action => {
              const { prevFilters, filters } = action.payload;
              const { activeOntologyTerm, fieldStates } =
                services.store.state.questions[questionName].paramUIState[parameter.name];
              const loadSummary = activeOntologyTerm != null && (
                fieldStates[activeOntologyTerm].ontologyTermSummary == null ||
                !isEqual( prevFilters.filter(f => f.field != activeOntologyTerm)
                        , filters.filter(f => f.field !== activeOntologyTerm) )
              );

              return {
                parameter,
                loadCounts: true,
                loadSummary
              };
            })
            .debounceTime(1000)

          const activeOntologyTermChangedParameter$ = action$.filter(ActiveFieldSetAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === parameter.name)
            .mapTo({ parameter, loadCounts: true, loadSummary: true })
            .filter(({parameter}) => {
              const { activeOntologyTerm, fieldStates }: State = services.store.state.questions[questionName].paramUIState[parameter.name];
              return activeOntologyTerm != null && fieldStates[activeOntologyTerm].ontologyTermSummary == null;
            })
            .debounceTime(1000)

          const groupVisibilityChangeParameter$ = action$.filter(GroupVisibilityChangedAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.groupName === parameter.group)
            .mapTo({ parameter, loadCounts: true, loadSummary: true })
            .filter(({parameter}) => {
              const { activeOntologyTerm, fieldStates }: State = services.store.state.questions[questionName].paramUIState[parameter.name];
              return activeOntologyTerm != null && fieldStates[activeOntologyTerm].ontologyTermSummary == null;
            })

          const parameter$ = Observable.merge(
            valueChangedParameter$,
            activeOntologyTermChangedParameter$,
            groupVisibilityChangeParameter$
          )
          .filter(({ parameter }) => isVisible(parameter))
          .switchMap(({parameter, loadCounts, loadSummary}) => {
            return Observable.merge(
              loadCounts
                ? getSummaryCounts(services.wdkService, parameter, services.store.state.questions[questionName])
                : Observable.empty(),
              loadSummary
                ? getOntologyTermSummary(services.wdkService, parameter, services.store.state.questions[questionName])
                : Observable.empty()
            ) as Observable<Action>;
          });

          const filters = getFilters(paramValues[parameter.name]);
          const activeField = filters.length === 0 ? findFirstLeaf(parameter.ontology) : filters[0].field;

          // The order here is important. We want to first merge the child epics
          // (updateActiveFieldEpic and ypdateSummaryCountsEpic), THEN we want to
          // merge the Observable of ActiveFieldSetAction. This will ensure that
          // updateActiveFieldEpic receives that action.
          return parameter$
            .merge(Observable.of(ActiveFieldSetAction.create({
              questionName,
              parameter,
              paramValues,
              activeField
            })))
        });
    })
}

function updateDependentParamsActiveFieldEpic(action$: Observable<Action>, { wdkService, store }: EpicServices<QuestionStore>): Observable<Action> {
  return action$.filter(ParamValueUpdatedAction.isType)
    .debounceTime(1000)
    .switchMap(action => {
      const { questionName, dependentParameters } = action.payload;
      const { paramValues, paramUIState } = store.state.questions[questionName];
      return Observable.from(dependentParameters)
        .filter(isType)
        .mergeMap(parameter => {
          const questionState = store.getState().questions[questionName];
          return Observable.of(OntologyTermsInvalidated.create({
            questionName,
            parameter,
            paramValues,
            retainedFields: [paramUIState[parameter.name].activeOntologyTerm]
          })).merge(
            getOntologyTermSummary(wdkService, parameter, questionState),
            getSummaryCounts(wdkService, parameter, questionState)
          );
        })
    })
}


// Helpers
// -------

function getOntologyTermSummary(
  wdkService: WdkService,
  parameter: FilterParamNew,
  state: QuestionState
) {
  const { question, paramValues, paramUIState } = state;
  const questionName = question.urlSegment;
  const { activeOntologyTerm } = paramUIState[parameter.name];
  const filters = (JSON.parse(paramValues[parameter.name]).filters as Filter[])
    .filter(filter => filter.field !== activeOntologyTerm);
  return wdkService.getOntologyTermSummary(questionName, parameter.name, filters, activeOntologyTerm, paramValues).then(
    summary => {
      const fieldState: FieldState = isMemberField(parameter, activeOntologyTerm)
        ? {
          loading: false,
          sort: defaultMemberFieldSort,
          searchTerm: '',
          ontologyTermSummary: {
            ...summary,
            valueCounts: sortDistribution(summary.valueCounts, defaultMemberFieldSort)
          }
        }
        : {
          loading: false,
          ontologyTermSummary: summary
        };

      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: activeOntologyTerm,
        fieldState
      })
    },
    error => {
      console.error(error);
      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: activeOntologyTerm,
        fieldState: {
          loading: false,
          errorMessage: 'Unable to load ontologyTermSummary for "' + activeOntologyTerm + '".'
        }
      });
    }
  )
}

function getSummaryCounts(
  wdkService: WdkService,
  parameter: FilterParamNew,
  state: QuestionState
) {
  const { question, paramValues, paramUIState } = state;
  const questionName = question.urlSegment;
  const paramName = parameter.name;
  const filters = JSON.parse(paramValues[parameter.name]).filters;
  return wdkService.getFilterParamSummaryCounts(questionName, paramName, filters, paramValues).then(
    counts => SummaryCountsLoadedAction.create({
      questionName,
      parameter,
      paramValues,
      ...counts
    }),
    error => ParamErrorAction.create({
      questionName,
      error,
      paramName
    })
  )
}

function getFiltersFromContext(ctx: Ctx) {
  return getFilters(ctx.paramValues[ctx.parameter.name]);
}

function getOntologyFromContext(ctx: Ctx) {
  return ctx.parameter.ontology;
}
