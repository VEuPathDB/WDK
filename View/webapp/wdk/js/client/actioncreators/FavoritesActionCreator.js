export let actionTypes = {
  FAVORITES_STATUS_LOADING: 'favorites/status-loading',
  FAVORITES_STATUS_RECEIVED: 'favorites/status-received',
  FAVORITES_STATUS_ERROR: 'favorites/status-error'
};

/**
 * @param {RecordDescriptor} record
 */
export function loadFavoritesStatus(record) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.getFavoritesStatus(record)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Boolean} status
 */
export function updateFavoritesStatus(record, status) {
  return function run(dispatch, { wdkService }) {
    return dispatch(setFavoritesStatus(
      record,
      wdkService.updateFavoritesStatus(record, status)
    ));
  };
}

/**
 * @param {RecordDescriptor} record
 * @param {Promise<Boolean>} statusPromise
 */
function setFavoritesStatus(record, statusPromise) {
  return function run(dispatch) {
    dispatch({
      type: actionTypes.FAVORITES_STATUS_LOADING,
      payload: { record }
    });
    return statusPromise.then(status => {
      return dispatch({
        type: actionTypes.FAVORITES_STATUS_RECEIVED,
        payload: { record, status }
      });
    }, error => {
      dispatch({
        type: actionTypes.FAVORITES_STATUS_ERROR,
        payload: { record, error }
      });
      throw error;
    });
  };
}
