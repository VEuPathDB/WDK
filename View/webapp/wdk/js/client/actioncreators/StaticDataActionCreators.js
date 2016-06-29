import { StaticDataProps, broadcast } from '../utils/StaticDataUtils';

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

export let staticDataConfigMap = {
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

function handleLoadError(error, dispatch) {
  console.error(error);
  dispatch(broadcast({
    type: actionTypes.STATIC_DATA_LOAD_ERROR,
    payload: { error }
  }));
}

function getPromise(dataItemName, dispatch, wdkService) {
  let { elementName, serviceCall, actionType } = staticDataConfigMap[dataItemName];
  return wdkService[serviceCall]().then(element => {
    console.log("WDK " + elementName + " loaded");
    dispatch(broadcast({
      type: actionType,
      payload: { [elementName]: element }
    }));
    return element;
  });
}

function getLoader(dataItemName) {
  return function run(dispatch, { wdkService }) {
    getPromise(dataItemName, dispatch, wdkService)
      .catch(error => { handleLoadError(error, dispatch); });
  };
}

export function loadAllStaticData() {
  let dataItemKeys = Object.keys(staticDataConfigMap);
  return function run(dispatch, { wdkService }) {
    let promiseArray = dataItemKeys.map(key => getPromise(key, dispatch, wdkService));
    Promise.all(promiseArray).then(resultArray => {
      let payload = {};
      for (let i = 0; i < dataItemKeys.length; i++) {
        payload[dataItemKeys[i]] = resultArray[i];
      }
      console.log("WDK static data loaded");
      dispatch(broadcast({
        type: actionTypes.STATIC_DATA_LOADED,
        payload: payload
      }));
    }).catch(error => { handleLoadError(error, dispatch); });
  };
}
