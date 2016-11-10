/**
 * Action creators related to routing.
 *
 * Note: These actions do not currently alter the URL. They mainly
 * propagate router state to stores.
 */

import { Location } from '~react-router~history';
import { ActionCreator } from '../ActionCreator';

const LOCATION_UPDATED = 'router/location-updated';

export const actionTypes = {
  LOCATION_UPDATED
};

export const updateLocation: ActionCreator = (location: Location) => ({
  type: LOCATION_UPDATED,
  payload: { location }
});
