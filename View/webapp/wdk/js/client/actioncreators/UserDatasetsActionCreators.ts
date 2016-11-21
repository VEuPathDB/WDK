import {ActionCreator} from "../ActionCreator";

const DATASET_LIST_LOADING = 'user-datasets/list-loading';
const DATASET_LIST_RECEIVED = 'user-datasets/list-received';
const DATASET_LIST_ERROR_RECEIVED = 'user-datasets/list-error';

export const actionTypes = {
  DATASET_LIST_LOADING,
  DATASET_LIST_RECEIVED,
  DATASET_LIST_ERROR_RECEIVED
};

const createLoadAction = () => ({ type: DATASET_LIST_LOADING });

export const loadUserDatasetList: ActionCreator = () => (dispatch, { wdkService }) =>
  wdkService.getCurrentUserDatasets()
  .then(userDatasets => ({ type: DATASET_LIST_RECEIVED, payload: { userDatasets } }),
        error => ({ type: DATASET_LIST_ERROR_RECEIVED, payload: { error } }))
  .then(dispatch);
