import { StaticDataProps, broadcast } from '../utils/StaticDataUtils';
import WdkService from "../utils/WdkService";
import {ActionCreator, DispatchAction} from "../ActionCreator";

export let actionTypes = {

  // actions triggered by individual static data loads
  CONFIG_LOADED: "static/config-loaded",
  ONTOLOGY_LOADED: "static/categories-loaded",
  QUESTIONS_LOADED: "static/questions-loaded",
  RECORDCLASSES_LOADED: "static/recordClasses-loaded",
  USER_LOADED: "static/user-loaded",
  PREFERENCES_LOADED: "static/preferences-loaded",

  // action triggered when all static data loaded
  STATIC_DATA_LOADED: "static/all-data-loaded",

  // action triggered if static data could not be loaded
  STATIC_DATA_LOAD_ERROR: "static/load-error"
};


export type StaticDataConfigItem = {
  elementName: string;
  serviceCall: string;
  actionType: string;
};

export type StaticDataConfigMap = {
  [key: string]: StaticDataConfigItem;
};

export let staticDataConfigMap: StaticDataConfigMap = {
  [StaticDataProps.CONFIG]: {
    elementName: StaticDataProps.CONFIG, serviceCall: 'getConfig', actionType: actionTypes.CONFIG_LOADED
  },
  [StaticDataProps.ONTOLOGY]: {
    elementName: StaticDataProps.ONTOLOGY, serviceCall: 'getOntology', actionType: actionTypes.ONTOLOGY_LOADED
  },
  [StaticDataProps.QUESTIONS]: {
    elementName: StaticDataProps.QUESTIONS, serviceCall: 'getQuestions', actionType: actionTypes.QUESTIONS_LOADED
  },
  [StaticDataProps.RECORDCLASSES]: {
    elementName: StaticDataProps.RECORDCLASSES, serviceCall: 'getRecordClasses', actionType: actionTypes.RECORDCLASSES_LOADED
  },
  [StaticDataProps.USER]: {
    elementName: StaticDataProps.USER, serviceCall: 'getCurrentUser', actionType: actionTypes.USER_LOADED
  },
  [StaticDataProps.PREFERENCES]: {
    elementName: StaticDataProps.PREFERENCES, serviceCall: 'getCurrentUserPreferences', actionType: actionTypes.PREFERENCES_LOADED
  }
};

// these entry points are not used directly by WDK, which loads all at once using loadAllStaticData()
export function loadConfig() { return getLoader(StaticDataProps.CONFIG); }
export function loadOntology() { return getLoader(StaticDataProps.ONTOLOGY); }
export function loadQuestions() { return getLoader(StaticDataProps.QUESTIONS); }
export function loadRecordClasses() { return getLoader(StaticDataProps.RECORDCLASSES); }
export function loadUser() { return getLoader(StaticDataProps.USER); }
export function loadPreferences() { return getLoader(StaticDataProps.PREFERENCES); }

function handleLoadError(error: Error) {
  console.error(error);
  return broadcast({
    type: actionTypes.STATIC_DATA_LOAD_ERROR,
    payload: { error }
  });
}

function getPromise(dataItemName: string, dispatch: DispatchAction, wdkService: WdkService) {
  let { elementName, serviceCall, actionType } = staticDataConfigMap[dataItemName];
  return (wdkService as any)[serviceCall]().then((element: any) => {
    console.log("WDK " + elementName + " loaded");
    dispatch(broadcast({
      type: actionType,
      payload: { [elementName]: element }
    }));
    return element;
  });
}

let getLoader: ActionCreator = (dataItemName: string) => {
  return function run(dispatch, { wdkService }) {
    return getPromise(dataItemName, dispatch, wdkService)
      .catch((error: Error) => handleLoadError(error));
  };
};

export let loadAllStaticData: ActionCreator = () => {
  let dataItemKeys = Object.keys(staticDataConfigMap);
  return function run(dispatch, { wdkService }) {
    let promiseArray = dataItemKeys.map(key => getPromise(key, dispatch, wdkService));
    return Promise.all(promiseArray).then(resultArray => {
      let payload: {[key: string]: any} = {};
      for (let i = 0; i < dataItemKeys.length; i++) {
        payload[dataItemKeys[i]] = resultArray[i];
      }
      console.log("WDK static data loaded");
      return broadcast({
        type: actionTypes.STATIC_DATA_LOADED,
        payload: payload
      });
    }).catch(error => handleLoadError(error));
  };
};
