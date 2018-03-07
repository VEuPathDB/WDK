/**
 * Created by dfalke on 8/17/16.
 */

import { Location } from 'history';
import { ReduceStore } from 'flux/utils';
import {StaticDataAction, AllDataAction, StaticData} from 'Core/ActionCreators/StaticDataActionCreators';
import { UserUpdateAction, PreferenceUpdateAction, PreferencesUpdateAction } from 'Core/ActionCreators/UserActionCreators';
import { LocationAction } from 'Core/ActionCreators/RouterActionCreators';
import { UserPreferences } from 'Utils/WdkUser';

type UserAction = UserUpdateAction | PreferenceUpdateAction | PreferencesUpdateAction;
type RouterAction = LocationAction;
type Action = AllDataAction | StaticDataAction | UserAction | RouterAction;

export type GlobalData = StaticData & {
  location: Location;
}

export default class GlobalDataStore extends ReduceStore<GlobalData, Action> {

  /*--------------- Methods that should probably be overridden ---------------*/

  /**
   * Provides an empty object as initial state.
   */
  getInitialState(): GlobalData {
    return <GlobalData>{};
  }

  handleAction(state: GlobalData, action: Action): GlobalData {
    return state;
  }
  /*------------- Methods that should probably not be overridden -------------*/

  /**
   * Handles requested static data item loads and passes remaining actions to
   * handleAction(), which will usually be overridden by the subclass
   */
  reduce(state: GlobalData, action: Action): GlobalData {
    switch(action.type) {
      case 'static/config-loaded':
      case 'static/categories-loaded':
      case 'static/questions-loaded':
      case 'static/recordClasses-loaded':
      case 'static/user-loaded':
      case 'static/preferences-loaded':
      case 'static/all-data-loaded':
      case 'user/user-update':
      case 'router/location-updated':
        return this.handleAction({ ...state, ...action.payload }, action);

      case 'user/preference-update':
        // incorporate new preference values into existing preference object
        let { global: oldGlobal, project: oldProject } = state.preferences;
        let { global: newGlobal, project: newProject } = action.payload;
        let combinedGlobal = newGlobal == null ? oldGlobal : Object.assign({}, oldGlobal, newGlobal);
        let combinedProject = newProject == null ? oldProject : Object.assign({}, oldProject, newProject);
        let combinedPrefs = { global: combinedGlobal, project: combinedProject };
        // treat preference object as if it has just been loaded (with new values present)
        return this.handleAction({ ...state, preferences: combinedPrefs }, action);

      case 'user/preferences-update':
        // replace existing preference object with new preference values
        let replacementPrefs = { ...action.payload };
        // treat preference object as if it has just been loaded (with new values present)
        return this.handleAction({ ...state, preferences: replacementPrefs }, action);

      default:
        return this.handleAction(state, action);
    }
  }
}
