import { broadcast } from '../utils/StaticDataUtils';
import WdkService from "../utils/WdkService";
import {ActionThunk, DispatchAction} from "../ActionCreator";
import {ServiceConfig} from "../utils/WdkService";
import {CategoryOntology} from "../utils/CategoryUtils";
import {Question, RecordClass} from "../utils/WdkModel";
import {User, UserPreferences} from "../utils/WdkUser";

const CONFIG = "config";
const ONTOLOGY = "ontology";
const QUESTIONS = "questions";
const RECORDCLASSES = "recordClasses";
const USER = "user";
const PREFERENCES = "preferences";

export type StaticData = {
  config: ServiceConfig;
  ontology: CategoryOntology;
  questions: Question[];
  recordClasses: RecordClass[];
  user: User;
  preferences: UserPreferences;
}

type StaticDataActionTypes = {
  config: 'static/config-loaded';
  ontology: 'static/categories-loaded';
  questions: 'static/questions-loaded';
  recordClasses: 'static/recordClasses-loaded';
  user: 'static/user-loaded';
  preferences: 'static/preferences-loaded';
}

type StaticDataActions<K extends keyof StaticData> = {
  type: StaticDataActionTypes[K];
  payload: Pick<StaticData, K>
}


// actions triggered by individual static data loads
export type ConfigAction = StaticDataActions<typeof CONFIG>
export type OntologyAction = StaticDataActions<typeof ONTOLOGY>
export type QuestionsAction = StaticDataActions<typeof QUESTIONS>
export type RecordClassesAction = StaticDataActions<typeof RECORDCLASSES>
export type UserAction = StaticDataActions<typeof USER>
export type PreferencesAction = StaticDataActions<typeof PREFERENCES>

// action triggered when all static data loaded
export type AllDataAction = {
  type: "static/all-data-loaded",
  payload: StaticData
}

// action triggered if static data could not be loaded
export type LoadErrorAction = {
  type: "static/load-error",
  payload: { error: Error }
}

export type StaticDataAction = ConfigAction
                             | OntologyAction
                             | QuestionsAction
                             | RecordClassesAction
                             | UserAction
                             | PreferencesAction

type StaticDataKey = keyof StaticData;

type StaticDataConfigMapEntry<K extends keyof StaticData> = {
  elementName: K;
  serviceCall: string;
  actionType: StaticDataActions<K>['type']
}

type StaticDataConfigMap = {
  [K in keyof StaticData]: StaticDataConfigMapEntry<K>
}

export let staticDataConfigMap: StaticDataConfigMap = {
  config: {
    elementName: CONFIG, serviceCall: 'getConfig', actionType: 'static/config-loaded'
  },
  ontology: {
    elementName: ONTOLOGY, serviceCall: 'getOntology', actionType: 'static/categories-loaded'
  },
  questions: {
    elementName: QUESTIONS, serviceCall: 'getQuestions', actionType: 'static/questions-loaded'
  },
  recordClasses: {
    elementName: RECORDCLASSES, serviceCall: 'getRecordClasses', actionType: 'static/recordClasses-loaded'
  },
  user: {
    elementName: USER, serviceCall: 'getCurrentUser', actionType: 'static/user-loaded'
  },
  preferences: {
    elementName: PREFERENCES, serviceCall: 'getCurrentUserPreferences', actionType: 'static/preferences-loaded'
  }
};

// these entry points are not used directly by WDK, which loads all at once using loadAllStaticData()
export function loadConfig() { return getLoader(CONFIG); }
export function loadOntology() { return getLoader(ONTOLOGY); }
export function loadQuestions() { return getLoader(QUESTIONS); }
export function loadRecordClasses() { return getLoader(RECORDCLASSES); }
export function loadUser() { return getLoader(USER); }
export function loadPreferences() { return getLoader(PREFERENCES); }

function handleLoadError(error: Error): LoadErrorAction {
  console.error(error);
  return broadcast({
    type: 'static/load-error',
    payload: { error }
  }) as LoadErrorAction;
}

function getPromise(
  dataItemName: StaticDataKey,
  dispatch: DispatchAction<StaticDataAction>,
  wdkService: WdkService
) {
  let { elementName, serviceCall, actionType } = staticDataConfigMap[dataItemName];
  return (wdkService as any)[serviceCall]().then((element: StaticData[typeof elementName]) => {
    console.log("WDK " + elementName + " loaded");
    dispatch(broadcast({
      type: actionType,
      payload: { [elementName]: element }
    }) as StaticDataAction);
    return element;
  });
}

function getLoader(dataItemName: StaticDataKey): ActionThunk<StaticDataAction> {
  return function run(dispatch, { wdkService }) {
    return getPromise(dataItemName, dispatch, wdkService)
      .catch((error: Error) => handleLoadError(error));
  };
};

export function loadAllStaticData(): ActionThunk<AllDataAction> {
  let dataItemKeys = Object.keys(staticDataConfigMap);
  return function run(dispatch, { wdkService }) {
    let promiseArray = dataItemKeys.map(key => getPromise(key as StaticDataKey, dispatch as any, wdkService));
    return Promise.all(promiseArray).then(resultArray => {
      let payload: {[key: string]: any} = {};
      for (let i = 0; i < dataItemKeys.length; i++) {
        payload[dataItemKeys[i]] = resultArray[i];
      }
      console.log("WDK static data loaded");
      dispatch(broadcast({
        type: 'static/all-data-loaded',
        payload: payload
      }) as AllDataAction);
    }).catch((error: Error) => handleLoadError(error));
  };
};
