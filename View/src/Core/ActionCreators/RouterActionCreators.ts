/**
 * Action creators related to routing.
 *
 * Note: These actions do not currently alter the URL. They mainly
 * propagate router state to stores.
 */

import { Location } from 'history';
import { ActionThunk } from 'Utils/ActionCreatorUtils';

export type LocationAction = {
  type: 'router/location-updated',
  payload: { location: Location }
}

export function updateLocation(location: Location): LocationAction {
  return { type: 'router/location-updated', payload: { location } }
}

export function transitionToInternalPage(path: string): ActionThunk<never> {
  return function run(dispatch, { transitioner }) {
    transitioner.transitionToInternalPage(path);
  };
}

export function transitionToExternalPage(path: string): ActionThunk<never> {
  return function run(dispatch, { transitioner }) {
    transitioner.transitionToExternalPage(path);
  };
}