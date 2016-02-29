// Action types
export let actionTypes = {
  USER_LOADING: 'user/loading',
  USER_INITIALIZE_STORE: 'user/initialize',
  USER_PROFILE_UPDATE: 'user/profile-update',
  USER_PROPERTY_UPDATE: 'user/property-update',
  USER_PREFERENCE_UPDATE: 'user/preference-update',
  BASKET_STATUS_LOADING: 'user/basket-status-loading',
  BASKET_STATUS_RECEIVED: 'user/basket-status-received',
  BASKET_STATUS_ERROR: 'user/basket-status-error',
  APP_ERROR: 'user/error'
};

export function loadCurrentUser() {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = wdkService.getCurrentUser();
    let preferencePromise = wdkService.getCurrentUserPreferences();

    return Promise.all([ userPromise, preferencePromise ])
    .then(([ user, preferences ]) => {
      dispatch({
        type: actionTypes.USER_INITIALIZE_STORE,
        payload: { user, preferences }
      });
      return { user, preferences };
    }, error => {
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });
  };
}

export function loadBasketStatus(recordClassName, primaryKey) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      recordClassName,
      primaryKey,
      wdkService.getBasketStatus(recordClassName, primaryKey)
    ));
  };
}

export function updateBasketStatus(recordClassName, primaryKey, inBasket) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      recordClassName,
      primaryKey,
      wdkService.updateBasketStatus(recordClassName, primaryKey, inBasket)
    ));
  };
}

function setBasketStatus(recordClassName, primaryKey, basketStatusPromise) {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.BASKET_STATUS_LOADING,
      payload: { recordClassName, primaryKey }
    });

    return basketStatusPromise.then(inBasket => {
      dispatch({
        type: actionTypes.BASKET_STATUS_RECEIVED,
        payload: { recordClassName, primaryKey, inBasket }
      });
      return { recordClassName, primaryKey, inBasket };
    }, error => {
      dispatch({
        type: actionTypes.BASKET_STATUS_ERROR,
        payload: { error }
      });
      throw error;
    });
  };
}
