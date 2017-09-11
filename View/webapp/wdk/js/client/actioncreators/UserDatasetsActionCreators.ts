import {ActionThunk} from "../ActionCreator";
import {UserDatasetMeta, UserDataset} from "../utils/WdkModel";
import {ServiceError} from "../utils/WdkService";

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
export type ItemLoading = {
  type: 'user-datasets/item-loading',
  payload: {
    id: number
  }
}
export type ItemReceivedAction = {
  type: 'user-datasets/item-received',
  payload: {
    id: number,
    userDataset?: UserDataset
  }
}
export type ItemErrorAction = {
  type: 'user-datasets/item-error',
  payload: {
    error: ServiceError
  }
}
export type ItemUpdatingAction = {
  type: 'user-datasets/item-updating'
}
export type ItemUpdateSuccessAction = {
  type: 'user-datasets/item-update-success',
  payload: {
    userDataset: UserDataset
  }
}
export type ItemUpdateErrorAction = {
  type: 'user-datasets/item-update-error',
  payload: {
    error: ServiceError
  }
}

type ListAction = ListLoadingAction|ListReceivedAction|ListErrorReceivedAction;
type ItemAction = ItemLoading|ItemReceivedAction|ItemErrorAction;
type UpdateAction = ItemUpdatingAction|ItemUpdateSuccessAction|ItemUpdateErrorAction;


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

export function loadUserDatasetItem(id: number): ActionThunk<ItemAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'user-datasets/item-loading', payload: { id } });
    wdkService.getUserDataset(id).then(
      userDataset => {
        dispatch({ type: 'user-datasets/item-received', payload: { id, userDataset } })
      },
      (error: ServiceError) => {
        dispatch(error.status === 404 ? { type: 'user-datasets/item-received', payload: { id, userDataset: undefined } }
          : { type: 'user-datasets/item-error', payload: { error } })
      }
    )
  }
}

export function updateUserDatasetItem(userDataset: UserDataset, meta: UserDatasetMeta): ActionThunk<UpdateAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'user-datasets/item-updating' });
    wdkService.updateUserDataset(userDataset.id, meta).then(
      () => {
        dispatch({ type: 'user-datasets/item-update-success', payload: { userDataset: { ...userDataset, meta } } } as UpdateAction)
      },
      (error: ServiceError) => {
        dispatch({ type: 'user-datasets/item-update-error', payload: { error } })
      }
    )
  }
}
