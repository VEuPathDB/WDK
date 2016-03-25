export let actionTypes = {
  BASKET_STATUS_LOADING: 'user/basket-status-loading',
  BASKET_STATUS_RECEIVED: 'user/basket-status-received',
  BASKET_STATUS_ERROR: 'user/basket-status-error'
};

/**
 * @typedef RecordDescriptor
 * @type {Object}
 * @property {string} recordClassName
 * @property {Object.<string, string>} id
 */

/**
 * @param {RecordDescriptor} record
 */
export function loadBasketStatus(record) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.getBasketStatus(record)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Boolean} status
 */
export function updateBasketStatus(record, status) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setBasketStatus(
      record,
      wdkService.updateBasketStatus(record, status)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Promise<Boolean>} basketStatusPromise
 */
function setBasketStatus(record, basketStatusPromise) {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.BASKET_STATUS_LOADING,
      payload: { record }
    });
    return basketStatusPromise.then(status => {
      return dispatch({
        type: actionTypes.BASKET_STATUS_RECEIVED,
        payload: { record, status }
      });
    }, error => {
      dispatch({
        type: actionTypes.BASKET_STATUS_ERROR,
        payload: { record, error }
      });
      throw error;
    });
  };
}
