import {ActionCreator} from "../ActionCreator";
import {Favorite} from "../utils/WdkModel";
import {ServiceError} from "../utils/WdkService";

export type ListLoadingAction = {
  type: 'favorites/list-loading'
}

export type ListReceivedAction = {
  type: 'favorites/list-received',
  payload: {
    list: Favorite[]
  }
}
export type ListErrorReceivedAction = {
  type: 'favorites/list-error',
  payload: {
    error: ServiceError
  }
}

export type EditCellAction = {
  type: 'favorites/edit-cell'
  payload: {
    coordinates: {}
    key: string
    value: string
    rowData: Favorite
  }
}

export type ChangeCellAction = {
  type: 'favorites/change-cell'
  payload: string
}

export type SaveCellDataAction = {
  type: 'favorites/save-cell-data'
}

export type SaveReceivedAction = {
  type: 'favorites/save-received'
  payload: { updatedFavorite : Favorite }
}


export type SaveErrorReceivedAction = {
  type: 'favorites/save-error'
  payload: {
    error: ServiceError
  }
}

export type CancelCellEditAction = {
  type: 'favorites/cancel-cell-edit'
  payload: {}
}

export type DeleteRowAction = {
  type: 'favorites/delete-row'
}

export type DeleteReceivedAction = {
  type: 'favorites/delete-received'
  payload: { deletedFavorite : Favorite}
}

export type DeleteErrorReceivedAction = {
  type: 'favorites/delete-error'
  payload: {
    error: ServiceError
  }
}

export type SearchTermAction = {
    type: 'favorites/search-term'
    payload: string
}

export type SortColumnAction = {
    type: 'favorites/sort-column'
    payload: {
        sortBy: string
        sortDirection: string
    }
}

export type AddRowAction = {
    type: 'favorites/add-row'
}

export type AddReceivedAction = {
    type: 'favorites/add-received'
    payload: {
        addedFavorite: Favorite
    }
}

export type AddErrorAction = {
    type: 'favorites/add-error'
    payload: {
        error: ServiceError
    }
}

type ListAction = ListLoadingAction|ListReceivedAction|ListErrorReceivedAction;
type SaveAction = SaveCellDataAction|SaveReceivedAction|SaveErrorReceivedAction;
type DeleteAction = DeleteRowAction|DeleteReceivedAction|DeleteErrorReceivedAction;
type AddAction = AddRowAction|AddReceivedAction|AddErrorAction;

export const loadFavoritesList: ActionCreator<ListAction> = () => (dispatch, { wdkService }) => {
  dispatch({ type: 'favorites/list-loading' });
  wdkService.getCurrentFavorites()
    .then(
      list => {
        dispatch({type: 'favorites/list-received', payload: {list}})
      },
      (error: ServiceError) => {
        dispatch({type: 'favorites/list-error', payload: {error}})
      }
    )
};

export const editCell = (data: any) => (dispatch: any) => {
  dispatch({ type: 'favorites/edit-cell', payload: data});
};

export const changeCell = (value: string) => (dispatch: any) => {
  dispatch({ type: 'favorites/change-cell', payload: value });
};

export const saveCellData:ActionCreator<SaveAction> = (updatedFavorite: Favorite) => (dispatch, { wdkService }) => {
  dispatch({ type: 'favorites/save-cell-data' });
  wdkService.editFavorite(updatedFavorite)
      .then(
          () => {
            dispatch({type: 'favorites/save-received', payload: {updatedFavorite}})
          },
          (error: ServiceError) => {
            dispatch({type: 'favorites/save-error', payload: {error}})
          }
      )
};

export const cancelCellEdit = () => (dispatch: any) => {
  dispatch({ type: 'favorites/cancel-cell-edit', payload: {}});
};

export const deleteRow:ActionCreator<DeleteAction> = (deletedFavorite: Favorite) => (dispatch, { wdkService } ) => {
    dispatch({ type: 'favorites/delete-row' });
    wdkService.deleteFavorite(deletedFavorite)
        .then(
            () => {
              dispatch({type: 'favorites/delete-received', payload: {deletedFavorite}})
            },
            (error: ServiceError) => {
              dispatch({type: 'favorites/delete-error', payload: {error}})
            }
        )
};

export const searchTerm = (value:string) => (dispatch: any) => {
    dispatch({ type: 'favorites/search-term', payload: value });
};

export const sortColumn = (sortBy:string, sortDirection:string) => (dispatch: any) => {
    dispatch({ type: 'favorites/sort-column', payload: {"sortBy" : sortBy, "sortDirection": sortDirection}});
};


export const addRow:ActionCreator<AddAction> = (addedFavorite: Favorite) => (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/add-row' });
    wdkService.addFavorite(addedFavorite)
        .then(
            () => {
                dispatch({type: 'favorites/add-received', payload: {addedFavorite}})
            },
            (error: ServiceError) => {
                dispatch({type: 'favorites/add-error', payload: {error}})
            }
        )
};