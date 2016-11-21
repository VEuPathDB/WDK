import {ActionCreator} from "../ActionCreator";

const DATASET_LIST_LOADING = 'user-datasets/list-loading';
const DATASET_LIST_RECEIVED = 'user-datasets/list-received';
const DATASET_LIST_ERROR_RECEIVED = 'user-datasets/list-error';

const DATASET_ITEM_LOADING = 'user-datasets/item-loading';
const DATASET_ITEM_RECEIVED = 'user-datasets/item-received';
const DATASET_ITEM_ERROR = 'user-datasets/item-error';

export const actionTypes = {
  DATASET_LIST_LOADING,
  DATASET_LIST_RECEIVED,
  DATASET_LIST_ERROR_RECEIVED,
  DATASET_ITEM_LOADING,
  DATASET_ITEM_RECEIVED,
  DATASET_ITEM_ERROR
};

const createLoadAction = () => ({ type: DATASET_LIST_LOADING });

export const loadUserDatasetList: ActionCreator = () => (dispatch, { wdkService }) =>
  wdkService.getCurrentUserDatasets()
  .then(userDatasets => ({ type: DATASET_LIST_RECEIVED, payload: { userDatasets } }),
        error => ({ type: DATASET_LIST_ERROR_RECEIVED, payload: { error } }))
  .then(dispatch);

export const loadUserDatasetItem: ActionCreator = (id: number) => (dispatch, { wdkService }) =>
  wdkService.getUserDataset(id)
  .then(userDataset => ({ type: DATASET_ITEM_RECEIVED, payload: { userDataset } }),
        error => ({ type: DATASET_ITEM_ERROR, payload: { error } }))
  .then(dispatch);