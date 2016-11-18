/**
 * Created by dfalke on 8/17/16.
 */

import { invert, pick } from 'lodash';
import { Location } from 'history';
import { ReduceStore } from 'flux/utils';
import {Action} from '../dispatcher/Dispatcher';
import { StaticDataProps } from '../utils/StaticDataUtils';
import {staticDataConfigMap, StaticDataConfigMap} from '../actioncreators/StaticDataActionCreators';
import { actionTypes as userActionTypes } from '../actioncreators/UserActionCreators';
import { actionTypes as routerActionTypes } from '../actioncreators/RouterActionCreators';
import {User, UserPreferences} from "../utils/WdkUser";
import {RecordClass, Question} from "../utils/WdkModel";
import {Ontology} from "../utils/OntologyUtils";
import {ServiceConfig} from "../utils/WdkService";
import {CategoryNode} from "../utils/CategoryUtils";

// create a map to static data item configs, but with actionTypes as keys instead of prop names
let actionMap = Object.keys(staticDataConfigMap).reduce((actionMap, key) =>
  Object.assign(actionMap, { [staticDataConfigMap[key].actionType]: staticDataConfigMap[key] }), <StaticDataConfigMap>{});
let userActionMap = invert(userActionTypes);
let routerActionMap = invert(routerActionTypes);

type GlobalDataItem = ServiceConfig
                    | Ontology<CategoryNode>
                    | RecordClass[]
                    | Question[]
                    | User
                    | UserPreferences
                    | Location;

export interface GlobalData  {
  config: ServiceConfig;
  ontology: Ontology<CategoryNode>;
  recordClasses: RecordClass[];
  questions: Question[];
  user: User;
  preferences: UserPreferences;
  location: Location;
  [key: string]: GlobalDataItem;
}

export default class GlobalDataStore extends ReduceStore<GlobalData, Action> {

  /*--------------- Methods that should probably be overridden ---------------*/

  /**
   * Provides an empty object as initial state.
   */
  getInitialState() {
    return <GlobalData>{};
  }

  /**
   * Does nothing by default for other actions; subclasses will probably override
   */
  handleAction(state: GlobalData, action: Action) {
    return state;
  }

  /*---------- Methods that may be overridden in special cases ----------*/

  /**
   * Default handling of each static data item load.
   */
  handleStaticDataItemAction(state: GlobalData, itemName: string, payload: any) {
    return Object.assign({}, state, { [itemName]: payload[itemName] });
  }

  /**
   * Default handle of user actions
   */
  handleUserAction(state: GlobalData, action: Action) {
    let { type, payload } = action;
    switch(type) {
      case userActionTypes.USER_UPDATE:
        return this.handleStaticDataItemAction(state, StaticDataProps.USER, payload);
      case userActionTypes.USER_PREFERENCE_UPDATE: {
        // incorporate new preference values into existing preference object
        let newPrefs = Object.assign({}, state[StaticDataProps.PREFERENCES], payload);
        // treat preference object as if it has just been loaded (with new values present)
        return this.handleStaticDataItemAction(state, StaticDataProps.PREFERENCES, { [StaticDataProps.PREFERENCES]: newPrefs });
      }
      default:
        return state;
    }
  }

  /**
   * Default handler of router actions
   * @param state
   * @param action
   */
  handleRouterAction(state: GlobalData, action: Action) {
    switch (action.type) {
      case routerActionTypes.LOCATION_UPDATED: return Object.assign( {}, state,
        pick(action.payload, 'location'));
      default: return state;
    }
  }

  /*------------- Methods that should probably not be overridden -------------*/

  /**
   * Handles requested static data item loads and passes remaining actions to
   * handleAction(), which will usually be overridden by the subclass
   */
  reduce(state: GlobalData, action: Action) {
    let { type, payload } = action;
    if (type in actionMap) {
      // this action corresponds to a static data item that this store needs
      return this.handleStaticDataItemAction(state, actionMap[type].elementName, payload);
    }
    // special cases for updates to user data (static but not const)
    else if (type in userActionMap) {
      return this.handleUserAction(state, action)
    }
    else if (type in routerActionMap) {
      return this.handleRouterAction(state, action);
    }
    else {
      return this.handleAction(state, action);
    }
  }
}
