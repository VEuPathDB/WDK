import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
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

export default class UserActionCreator extends ActionCreator {

  loadCurrentUser() {

    this._dispatch({ type: actionTypes.USER_LOADING });

    let userPromise = this._service.getCurrentUser();
    let preferencePromise = this._service.getCurrentUserPreferences();

    return Promise.all([ userPromise, preferencePromise ])
    .then(([ user, preferences ]) => {
      this._dispatch({
        type: actionTypes.USER_INITIALIZE_STORE,
        payload: { user, preferences }
      });
      return { user, preferences };
    }, this._errorHandler(actionTypes.APP_ERROR));
  }

  loadBasketStatus(recordClassName, primaryKey) {
    return this._basketStatusAction(
      recordClassName,
      primaryKey,
      this._service.getBasketStatus(recordClassName, primaryKey)
    );
  }

  updateBasketStatus(recordClassName, primaryKey, inBasket) {
    return this._basketStatusAction(
      recordClassName,
      primaryKey,
      this._service.updateBasketStatus(recordClassName, primaryKey, inBasket)
    );
  }

  _basketStatusAction(recordClassName, primaryKey, basketStatusPromise) {
    this._dispatch({
      type: actionTypes.BASKET_STATUS_LOADING,
      payload: { recordClassName, primaryKey }
    });

    return basketStatusPromise.then(inBasket => {
      this._dispatch({
        type: actionTypes.BASKET_STATUS_RECEIVED,
        payload: { recordClassName, primaryKey, inBasket }
      });
      return { recordClassName, primaryKey, inBasket };
    }, this._errorHandler(actionTypes.BASKET_STATUS_ERROR));
  }
}

UserActionCreator.actionTypes = actionTypes;
