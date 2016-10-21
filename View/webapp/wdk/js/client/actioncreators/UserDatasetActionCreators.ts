import {ActionCreator} from "../ActionCreator";

const LOADING = 'user-datasets/loading'
const LOAD_SUCCESS = 'user-datasets/loaded'
const LOAD_ERROR = 'user-datasets/error'

export const actionTypes = { LOADING, LOAD_SUCCESS, LOAD_ERROR }

const createLoadAction = () => ({ type: LOADING })

export const loadUserDatasets: ActionCreator = () => (dispatch, { wdkService }) =>
  wdkService.getCurrentUserDatasets()
  .then(userDatasets => ({ type: LOAD_SUCCESS, payload: { userDatasets } }),
        error => ({ type: LOAD_ERROR, payload: { error } }))
  .then(dispatch)
