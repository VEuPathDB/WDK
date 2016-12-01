import {ActionCreator} from "../ActionCreator";
import { UserDatasetMeta } from "../utils/WdkModel";

const DATASET_LIST_LOADING = 'user-datasets/list-loading';
const DATASET_LIST_RECEIVED = 'user-datasets/list-received';
const DATASET_LIST_ERROR_RECEIVED = 'user-datasets/list-error';

const DATASET_ITEM_LOADING = 'user-datasets/item-loading';
const DATASET_ITEM_RECEIVED = 'user-datasets/item-received';
const DATASET_ITEM_ERROR = 'user-datasets/item-error';

const DATASET_ITEM_UPDATING = 'user-datasets/item-updating';
const DATASET_ITEM_UPDATE_SUCCESS = 'user-datasets/item-update-success';
const DATASET_ITEM_UPDATE_ERROR = 'user-datasets/item-update-error';

export const actionTypes = {
  DATASET_LIST_LOADING,
  DATASET_LIST_RECEIVED,
  DATASET_LIST_ERROR_RECEIVED,
  DATASET_ITEM_LOADING,
  DATASET_ITEM_RECEIVED,
  DATASET_ITEM_ERROR,
  DATASET_ITEM_UPDATING,
  DATASET_ITEM_UPDATE_SUCCESS,
  DATASET_ITEM_UPDATE_ERROR
};

const createLoadAction = () => ({ type: DATASET_LIST_LOADING });

export const loadUserDatasetList: ActionCreator = () => (dispatch, { wdkService }) =>
  wdkService.getCurrentUserDatasets()
  .then(userDatasets => ({ type: DATASET_LIST_RECEIVED, payload: { userDatasets } }),
        error => ({ type: DATASET_LIST_ERROR_RECEIVED, payload: { error } }))
  .then(dispatch);

export const loadUserDatasetItem: ActionCreator = (id: number) => (dispatch, { wdkService }) =>
  wdkService.getUserDataset(id)
  .then(userDataset => ({ type: DATASET_ITEM_RECEIVED, payload: { id, userDataset } }),
        // FIXME Uncomment below once 404s are returned by service
        // error => ({ type: DATASET_ITEM_ERROR, payload: { error } }))
        error => ({ type: DATASET_ITEM_RECEIVED, payload: { id, userDataset: undefined } }))
  .then(dispatch);

export const updateUserDatasetItem: ActionCreator = (id: number, meta: UserDatasetMeta) => (dispatch, { wdkService }) => {
  dispatch({ type: DATASET_ITEM_UPDATING });
  return wdkService.updateUserDataset(id, meta)
  .then(() => dispatch({ type: DATASET_ITEM_UPDATE_SUCCESS, payload: { id, meta }}),
        (error) => dispatch({ type: DATASET_ITEM_UPDATE_ERROR, payload: { error }}));
}
