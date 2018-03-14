import {ActionThunk} from "Utils/ActionCreatorUtils";
import {UserDatasetMeta, UserDataset} from "Utils/WdkModel";
import {ServiceError} from "Utils/WdkService";

export type ListLoadingAction = {
  type: 'user-datasets/list-loading'
}
export type ListReceivedAction = {
  type: 'user-datasets/list-received',
  payload: {
    userDatasets: UserDataset[]
  }
}
export type ListErrorReceivedAction = {
  type: 'user-datasets/list-error',
  payload: {
    error: ServiceError
  }
}
export type DetailLoading = {
  type: 'user-datasets/detail-loading',
  payload: {
    id: number
  }
}
export type DetailReceivedAction = {
  type: 'user-datasets/detail-received',
  payload: {
    id: number,
    userDataset?: UserDataset
  }
}
export type DetailErrorAction = {
  type: 'user-datasets/detail-error',
  payload: {
    error: ServiceError
  }
}
export type DetailUpdatingAction = {
  type: 'user-datasets/detail-updating'
}
export type DetailUpdateSuccessAction = {
  type: 'user-datasets/detail-update-success',
  payload: {
    userDataset: UserDataset
  }
}
export type DetailUpdateErrorAction = {
  type: 'user-datasets/detail-update-error',
  payload: {
    error: ServiceError
  }
}

type ListAction = ListLoadingAction|ListReceivedAction|ListErrorReceivedAction;
type DetailAction = DetailLoading|DetailReceivedAction|DetailErrorAction;
type UpdateAction = DetailUpdatingAction|DetailUpdateSuccessAction|DetailUpdateErrorAction;


export function loadUserDatasetList(): ActionThunk<ListAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'user-datasets/list-loading' });
    wdkService.getCurrentUserDatasets().then(
      userDatasets => {
        dispatch({ type: 'user-datasets/list-received', payload: { userDatasets } })
      },
      (error: ServiceError) => {
        dispatch({ type: 'user-datasets/list-error', payload: { error } })
      }
    )
  }
}

export function loadUserDatasetDetail(id: number): ActionThunk<DetailAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'user-datasets/detail-loading', payload: { id } });
    wdkService.getUserDataset(id).then(
      userDataset => {
        dispatch({ type: 'user-datasets/detail-received', payload: { id, userDataset } })
      },
      (error: ServiceError) => {
        dispatch(error.status === 404 ? { type: 'user-datasets/detail-received', payload: { id, userDataset: undefined } }
          : { type: 'user-datasets/detail-error', payload: { error } })
      }
    )
  }
}

export function updateUserDatasetDetail(userDataset: UserDataset, meta: UserDatasetMeta): ActionThunk<UpdateAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'user-datasets/detail-updating' });
    wdkService.updateUserDataset(userDataset.id, meta).then(
      () => {
        dispatch({ type: 'user-datasets/detail-update-success', payload: { userDataset: { ...userDataset, meta } } } as UpdateAction)
      },
      (error: ServiceError) => {
        dispatch({ type: 'user-datasets/detail-update-error', payload: { error } })
      }
    )
  }
}
