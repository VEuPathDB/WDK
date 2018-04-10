import { get, isEqual } from 'lodash';
import { Observable } from 'rxjs';

import { Filter } from 'Components/AttributeFilter/Utils/FilterService';
import {
  GroupVisibilityChangedAction,
  ParamErrorAction,
  ParamValueUpdatedAction,
  QuestionLoadedAction,
  UnloadQuestionAction,
  ParamsUpdatedAction,
} from 'Core/ActionCreators/QuestionActionCreators';
import { FieldState, MemberFieldState, State } from 'Params/FilterParamNew/State';
import { findFirstLeaf, getFilters, isMemberField, isType, sortDistribution } from 'Params/FilterParamNew/Utils';
import { Context } from 'Params/Utils';
import { Action, combineEpics, EpicServices, makeActionCreator, payload } from 'Utils/ActionCreatorUtils';
import { FilterParamNew } from 'Utils/WdkModel';
import WdkService from 'Utils/WdkService';
import QuestionStore, { QuestionState } from 'Views/Question/QuestionStore';


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

export const FieldCountUpdateRequestAction = makeActionCreator(
  'filter-param-new/field-count-update-request',
  payload<Context<FilterParamNew> & {
    field: string;
  }>()
)

export const SummaryCountsLoadedAction = makeActionCreator(
  'filter-param-new/summary-counts-loaded',
  payload<Context<FilterParamNew> & {
    filtered: number;
    unfiltered: number;
    nativeFiltered: number;
    nativeUnfiltered: number;
  }>()
)

export const FieldStateUpdatedAction = makeActionCreator(
  'filter-param-new/field-state-updated',
  payload<Context<FilterParamNew> & {
    field: string;
    fieldState: FieldState;
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

type LoadDeps = {
  parameter: FilterParamNew,
  loadCounts: boolean,
  loadSummaryFor: string | null,
  questionState: QuestionState
};

/**
 * When a Question is loaded, listen for parameter-specific actions and load data as needed.
 */
function initEpic(action$: Observable<Action>, services: EpicServices<QuestionStore>): Observable<Action> {
  return action$
    .filter(QuestionLoadedAction.isType)
    .mergeMap(action => {
      const { questionName } = action.payload;
      const questionState = getQuestionState(services.store, questionName);
      if (questionState == null) return Observable.empty();
      const { question, paramValues } = questionState;
      const isVisible = (parameter: FilterParamNew) => (
        get(getQuestionState(services.store, questionName), ['groupUIState', parameter.group, 'isVisible'], false)
      )

      // Create an observable per filter param to load ontology term summaries
      // and counts when an active ontology term is changed, or when a param
      // value changes, but only if its group is visible.
      return Observable.from(question.parameters)
        .filter(isType)
        .mergeMap(parameter => {
          // Create an Observable<FilterParamNew> based on actions
          const valueChangedParameter$ = action$.filter(FiltersUpdatedAction.isType)
            .filter(
              action => action.payload.questionName === questionName &&
              action.payload.parameter.name === parameter.name
            )
            .mergeMap(action => {
              const { prevFilters, filters } = action.payload;
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates } =
                questionState.paramUIState[parameter.name];
              const loadSummary = activeOntologyTerm != null && (
                fieldStates[activeOntologyTerm].summary == null ||
                !isEqual( prevFilters.filter(f => f.field != activeOntologyTerm)
                        , filters.filter(f => f.field !== activeOntologyTerm) )
              );

              return Observable.of({
                questionState,
                parameter,
                loadCounts: true,
                loadSummaryFor: loadSummary ? activeOntologyTerm : null
              });
            })
            .debounceTime(1000)

          const activeOntologyTermChangedParameter$ = action$.filter(ActiveFieldSetAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === parameter.name)
            .mergeMap(() => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates }: State = questionState.paramUIState[parameter.name];
              if (activeOntologyTerm != null && fieldStates[activeOntologyTerm].summary == null) {
                return Observable.of({
                  parameter,
                  loadCounts: true,
                  loadSummaryFor: questionState.paramUIState[parameter.name].activeOntologyTerm,
                  questionState
                })
              }
              return Observable.empty() as Observable<LoadDeps>;
            })
            .debounceTime(1000)

          const forceSummaryUpdateParameter$ = action$.filter(FieldCountUpdateRequestAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === parameter.name)
            .mergeMap(action => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              return Observable.of({
                parameter,
                loadCounts: false,
                loadSummaryFor: action.payload.field,
                questionState
            })
            })

          const groupVisibilityChangeParameter$ = action$.filter(GroupVisibilityChangedAction.isType)
            .filter(action => action.payload.questionName === questionName && action.payload.groupName === parameter.group)
            .mergeMap(() => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates }: State = questionState.paramUIState[parameter.name];
              if (activeOntologyTerm != null && fieldStates[activeOntologyTerm].summary == null) {
                return Observable.of({
                  parameter,
                  loadCounts: true,
                  loadSummaryFor: activeOntologyTerm,
                  questionState
                })
              }
              return Observable.empty() as Observable<LoadDeps>;
            });

          const parameter$ = Observable.merge(
            valueChangedParameter$,
            activeOntologyTermChangedParameter$,
            groupVisibilityChangeParameter$
          )
          .filter(({ parameter }) => isVisible(parameter))
          .switchMap(({parameter, loadCounts, loadSummaryFor, questionState}) => {
            return Observable.merge(
              loadCounts
                ? getSummaryCounts(services.wdkService, parameter, questionState)
                : Observable.empty(),
              loadSummaryFor
                ? getOntologyTermSummary(services.wdkService, parameter, questionState, loadSummaryFor)
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
        })
        .takeUntil(action$.filter(killAction => (
          UnloadQuestionAction.isType(killAction) &&
          killAction.payload.questionName === action.payload.questionName
        )));
    })
}

function updateDependentParamsActiveFieldEpic(action$: Observable<Action>, { wdkService, store }: EpicServices<QuestionStore>): Observable<Action> {
  return action$.filter(ParamsUpdatedAction.isType)
    .debounceTime(1000)
    .switchMap(action => {
      const { questionName, parameters } = action.payload;
      return Observable.from(parameters)
        .filter(isType)
        .mergeMap(parameter => {
          const questionState = getQuestionState(store, questionName);
          if (questionState == null) return Observable.empty() as Observable<Action>;
          const { paramValues, paramUIState } = questionState;
          const { activeOntologyTerm } = paramUIState[parameter.name];

          return Observable.of(OntologyTermsInvalidated.create({
            questionName,
            parameter,
            paramValues,
            retainedFields: [/* activeOntologyTerm */]
          })).merge(
            getOntologyTermSummary(wdkService, parameter, questionState, activeOntologyTerm),
            getSummaryCounts(wdkService, parameter, questionState)
          );
        })
        .takeUntil(action$.filter(killAction => (
          UnloadQuestionAction.isType(killAction) &&
          killAction.payload.questionName === action.payload.questionName
        )))
    })
}


// Helpers
// -------

function getOntologyTermSummary(
  wdkService: WdkService,
  parameter: FilterParamNew,
  state: QuestionState,
  ontologyTerm: string
): Observable<Action> {
  const { question, paramValues, paramUIState } = state;
  const questionName = question.urlSegment;
  if (ontologyTerm == null) return Observable.empty();

  // FIXME Add loading and invalid for fieldState
  const filters = (JSON.parse(paramValues[parameter.name]).filters as Filter[])
    .filter(filter => filter.field !== ontologyTerm);
  return Observable.of(FieldStateUpdatedAction.create({
    questionName,
    parameter,
    paramValues,
    field: ontologyTerm,
    fieldState: {
      loading: true
    }
  }))
  .concat(wdkService.getOntologyTermSummary(questionName, parameter.name, filters, ontologyTerm, paramValues).then(
    summary => {
      const fieldState: FieldState = isMemberField(parameter, ontologyTerm)
        ? {
          invalid: false,
          loading: false,
          sort: defaultMemberFieldSort,
          searchTerm: '',
          summary: {
            ...summary,
            valueCounts: sortDistribution(summary.valueCounts, defaultMemberFieldSort)
          }
        }
        : {
          invalid: false,
          loading: false,
          summary: summary
        };

      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: ontologyTerm,
        fieldState
      })
    },
    error => {
      console.error(error);
      return FieldStateUpdatedAction.create({
        questionName,
        parameter,
        paramValues,
        field: ontologyTerm,
        fieldState: {
          invalid: false,
          loading: false,
          errorMessage: 'Unable to load summary for "' + ontologyTerm + '".'
        }
      });
    }
  ))
}

function getSummaryCounts(
  wdkService: WdkService,
  parameter: FilterParamNew,
  state: QuestionState
) {
  const { question, paramValues } = state;
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

function getQuestionState(store: QuestionStore, questionName: string) {
  return store.state.questions[questionName];
}