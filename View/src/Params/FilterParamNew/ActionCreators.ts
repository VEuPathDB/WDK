import { isEqual } from 'lodash';
import { Observable } from 'rxjs';

import { Filter } from 'Components/AttributeFilter/Types';
import {
  GroupVisibilityChangedAction,
  ParamErrorAction,
  ParamsUpdatedAction,
  QuestionLoadedAction,
  UnloadQuestionAction,
} from 'Core/ActionCreators/QuestionActionCreators';
import { FieldState, MemberFieldState, State } from 'Params/FilterParamNew/State';
import { findFirstLeaf, getFilters, isMemberField, isType, sortDistribution } from 'Params/FilterParamNew/Utils';
import { Context } from 'Params/Utils';
import { Action, combineEpics, EpicServices, makeActionCreator } from 'Utils/ActionCreatorUtils';
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

function makeFpnActionCreator<S, T extends string>(type: T) {
  return makeActionCreator<Context<FilterParamNew> & S, T>(type);
}

export const ActiveFieldSetAction =
  makeFpnActionCreator<{ activeField: string }, 'filter-param-new/active-field-set'>('filter-param-new/active-field-set')

export const FieldCountUpdateRequestAction =
  makeFpnActionCreator<{ field: string }, 'filter-param-new/field-count-update-request'>('filter-param-new/field-count-update-request')

export const SummaryCountsLoadedAction =
  makeFpnActionCreator<
    {
      filtered: number;
      unfiltered: number;
      nativeFiltered: number;
      nativeUnfiltered: number;
    },
    'filter-param-new/summary-counts-loaded'
    >('filter-param-new/summary-counts-loaded')

export const FieldStateUpdatedAction =
  makeFpnActionCreator<
    {
      field: string;
      fieldState: FieldState;
    },
    'filter-param-new/field-state-updated'
    >('filter-param-new/field-state-updated')

export const FiltersUpdatedAction =
  makeFpnActionCreator<
    {
      prevFilters: Filter[];
      filters: Filter[];
    },
    'filter-param-new/filters-updated'
    >('filter-param-new/filters-updated')

export const OntologyTermsInvalidated =
  makeFpnActionCreator<
    {
      retainedFields: string[]
    },
    'filter-param-new/ontology-terms-invalidated'
    >('filter-param-new/ontology-terms-invalidated')

// Epics
// -----

export default combineEpics(initEpic, updateDependentParamsActiveFieldEpic);

type LoadDeps = {
  paramName: string,
  loadCounts: boolean,
  loadSummaryFor: string | null,
  questionState: QuestionState
};

/**
 * When a Question is loaded, listen for parameter-specific actions and load data as needed.
 */
function initEpic(action$: Observable<Action>, services: EpicServices<QuestionStore>): Observable<Action> {
  return action$
    .filter(QuestionLoadedAction.test)
    .mergeMap(action => {
      const { questionName } = action.payload;
      const questionState = getQuestionState(services.store, questionName);
      if (questionState == null) return Observable.empty();
      const { question, paramValues } = questionState;
      const isVisible = (paramName: string) => {
        const state = getQuestionState(services.store, questionName);
        if (state == null) return false;

        const parameter = getFilterParamNewFromState(state, paramName);
        return state.groupUIState[parameter.group].isVisible;
      }

      // Create an observable per filter param to load ontology term summaries
      // and counts when an active ontology term is changed, or when a param
      // value changes, but only if its group is visible.
      return Observable.from(question.parameters)
        .filter(isType)
        .map(parameter => ({ paramName: parameter.name, groupName: parameter.group }))
        .mergeMap(({paramName, groupName}) => {
          // Create an Observable<FilterParamNew> based on actions
          const valueChangedParameter$ = action$.filter(FiltersUpdatedAction.test)
            .filter(
              action => action.payload.questionName === questionName &&
              action.payload.parameter.name === paramName
            )
            .mergeMap(action => {
              const { prevFilters, filters } = action.payload;
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates } =
                questionState.paramUIState[paramName];
              const loadSummary = activeOntologyTerm != null && (
                fieldStates[activeOntologyTerm].summary == null ||
                !isEqual( prevFilters.filter(f => f.field != activeOntologyTerm)
                        , filters.filter(f => f.field !== activeOntologyTerm) )
              );

              return Observable.of({
                questionState,
                paramName,
                loadCounts: true,
                loadSummaryFor: loadSummary ? activeOntologyTerm : null
              });
            })
            .debounceTime(1000)

          const activeOntologyTermChangedParameter$ = action$.filter(ActiveFieldSetAction.test)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === paramName)
            .mergeMap(() => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates }: State = questionState.paramUIState[paramName];
              if (activeOntologyTerm != null && fieldStates[activeOntologyTerm].summary == null) {
                return Observable.of({
                  paramName,
                  loadCounts: true,
                  loadSummaryFor: questionState.paramUIState[paramName].activeOntologyTerm,
                  questionState
                })
              }
              return Observable.empty() as Observable<LoadDeps>;
            })

          const forceSummaryUpdateParameter$ = action$.filter(FieldCountUpdateRequestAction.test)
            .filter(action => action.payload.questionName === questionName && action.payload.parameter.name === paramName)
            .mergeMap(action => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              return Observable.of({
                paramName,
                loadCounts: false,
                loadSummaryFor: action.payload.field,
                questionState
              })
            })

          const groupVisibilityChangeParameter$ = action$.filter(GroupVisibilityChangedAction.test)
            .filter(action => action.payload.questionName === questionName && action.payload.groupName === groupName)
            .mergeMap(() => {
              const questionState = getQuestionState(services.store, questionName);
              if (questionState == null) return Observable.empty() as Observable<LoadDeps>;

              const { activeOntologyTerm, fieldStates }: State = questionState.paramUIState[paramName];
              if (activeOntologyTerm != null && fieldStates[activeOntologyTerm].summary == null) {
                return Observable.of({
                  paramName,
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
          .filter(({ paramName }) => isVisible(paramName))
          .switchMap(({paramName, loadCounts, loadSummaryFor, questionState}) => {
            return Observable.merge(
              loadCounts
                ? getSummaryCounts(services.wdkService, paramName, questionState)
                : Observable.empty(),
              loadSummaryFor
                ? getOntologyTermSummary(services.wdkService, paramName, questionState, loadSummaryFor)
                : Observable.empty()
            ) as Observable<Action>;
          });

          const filters = getFilters(paramValues[paramName]);
          const activeField = filters.length === 0
            ? findFirstLeaf(getFilterParamNewFromState(getQuestionState(services.store, questionName), paramName).ontology)
            : filters[0].field;

          // The order here is important. We want to first merge the child epics
          // (updateActiveFieldEpic and ypdateSummaryCountsEpic), THEN we want to
          // merge the Observable of ActiveFieldSetAction. This will ensure that
          // updateActiveFieldEpic receives that action.
          return parameter$
            .merge(Observable.of(ActiveFieldSetAction.create({
              questionName,
              parameter: getFilterParamNewFromState(questionState, paramName),
              paramValues,
              activeField
            })))
        })
        .takeUntil(action$.filter(killAction => (
          UnloadQuestionAction.test(killAction) &&
          killAction.payload.questionName === action.payload.questionName
        )));
    })
}

function updateDependentParamsActiveFieldEpic(action$: Observable<Action>, { wdkService, store }: EpicServices<QuestionStore>): Observable<Action> {
  return action$.filter(ParamsUpdatedAction.test)
    .switchMap(action => {
      const { questionName, parameters } = action.payload;
      return Observable.from(parameters)
        .filter(isType)
        .mergeMap(parameter => {
          const questionState = getQuestionState(store, questionName);
          if (questionState == null) return Observable.empty() as Observable<Action>;
          const { paramValues, paramUIState } = questionState;
          const { activeOntologyTerm } = paramUIState[parameter.name] as State;
          const { ontology } = parameter;
          const filters = getFilters(paramValues[parameter.name]);

          const activeField = ontology.findIndex(item => item.term === activeOntologyTerm) > -1
            ? activeOntologyTerm
            : filters.length === 0 ? findFirstLeaf(ontology) : filters[0].field;

          return Observable.of(OntologyTermsInvalidated.create({
            questionName,
            parameter,
            paramValues,
            retainedFields: [/* activeOntologyTerm */]
          })).merge(
            activeField ? getOntologyTermSummary(wdkService, parameter.name, questionState, activeField) : Observable.empty() as Observable<Action>,
            getSummaryCounts(wdkService, parameter.name, questionState)
          );
        })
        .takeUntil(action$.filter(killAction => (
          UnloadQuestionAction.test(killAction) &&
          killAction.payload.questionName === action.payload.questionName
        )))
    })
}


// Helpers
// -------

function getOntologyTermSummary(
  wdkService: WdkService,
  paramName: string,
  state: QuestionState,
  ontologyTerm: string
): Observable<Action> {
  const { question, paramValues, paramUIState } = state;
  const questionName = question.urlSegment;
  const parameter = getFilterParamNewFromState(state, paramName);

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
  paramName: string,
  state: QuestionState
): Observable<Action> {
  const { question, paramValues } = state;
  const questionName = question.urlSegment;
  const parameter = getFilterParamNewFromState(state, paramName);
  const filters = JSON.parse(paramValues[paramName]).filters;

  return Observable.from(wdkService.getFilterParamSummaryCounts(questionName, paramName, filters, paramValues).then(
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
  ));
}

function getFiltersFromContext(ctx: Ctx) {
  return getFilters(ctx.paramValues[ctx.parameter.name]);
}

function getOntologyFromContext(ctx: Ctx) {
  return ctx.parameter.ontology;
}

function getQuestionState(store: QuestionStore, questionName: string) {
  const state = store.state.questions[questionName];
  if (state == null) throw new Error(`Question ${questionName} does not exist in store state.`);
  return state;
}

function getFilterParamNewFromState(state: QuestionState, paramName: string) {
  const parameter = state.question.parametersByName[paramName];
  if (parameter == null || parameter.type !== 'FilterParamNew') {
    throw new Error(`Parameter ${paramName} in ${state.question.name} is not a FilterParamNew.`);
  }
  return parameter;
}