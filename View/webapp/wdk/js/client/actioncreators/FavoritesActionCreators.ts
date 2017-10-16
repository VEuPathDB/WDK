import { ActionThunk } from "../ActionCreator";
import { Favorite, RecordInstance } from "../utils/WdkModel";
import { ServiceError } from "../utils/WdkService";

export type ListLoadingAction = {
  type: 'favorites/list-loading'
}

export type ListReceivedAction = {
  type: 'favorites/list-received',
  payload: {
    list: Favorite[]
  }
}

export type FavoriteSelectedAction = {
  type: 'favorites/favorite-selected',
  payload: {
    id: Favorite['id']
  }
};

export type FavoriteDeselectedAction = {
  type: 'favorites/favorite-deselected',
  payload: {
    id: Favorite['id']
  }
};

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
}

export type DeleteRowsAction = {
  type: 'favorites/delete-rows'
}

export type DeleteRowAction = {
  type: 'favorites/delete-row'
}

export type DeleteReceivedAction = {
  type: 'favorites/delete-received'
  payload: { deletedFavorite : Favorite }
}

export type DeleteMultiReceivedAction = {
  type: 'favorites/delete-multi-received'
  payload: { deletedFavorites : Favorite[] }
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
    sortByKey: string
    sortDirection: string
  }
}

export type UndeleteRowsAction = {
  type: 'favorites/undelete-rows'
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

export type UndeletedRowsReceivedAction = {
  type: 'favorites/undeleted-rows-received'
  payload: {
    undeletedFavorites: Favorite[]
  }
}

export type AddErrorAction = {
  type: 'favorites/add-error'
  payload: {
    error: ServiceError
  }
}

export type FilterByTypeAction = {
  type: 'favorites/filter-by-type'
  payload: string
}

type ListAction = ListLoadingAction|ListReceivedAction|ListErrorReceivedAction;
type SaveAction = SaveCellDataAction|SaveReceivedAction|SaveErrorReceivedAction;
type DeleteAction = DeleteRowAction|DeleteRowsAction|DeleteReceivedAction|DeleteMultiReceivedAction|DeleteErrorReceivedAction;
type AddAction = AddRowAction|AddReceivedAction|AddErrorAction|UndeleteRowsAction|UndeletedRowsReceivedAction;

export function loadFavoritesList(): ActionThunk<ListAction>{
  return function run(dispatch, { wdkService }) {
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
  }
}

export function selectFavorite(id: Favorite['id']): FavoriteSelectedAction {
  return { type: 'favorites/favorite-selected', payload: { id } };
}

export function deselectFavorite(id: Favorite['id']): FavoriteDeselectedAction {
  return { type: 'favorites/favorite-deselected', payload: { id }};
}

export function editCell(data: EditCellAction['payload']): EditCellAction {
  return { type: 'favorites/edit-cell', payload: data };
}

export function changeCell(value: string): ChangeCellAction {
  return { type: 'favorites/change-cell', payload: value };
}

export function saveCellData(updatedFavorite: Favorite): ActionThunk<SaveAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/save-cell-data' });
    wdkService.saveFavorite(updatedFavorite).then(
      () => {
        dispatch({ type: 'favorites/save-received', payload: { updatedFavorite } })
      },
      (error: ServiceError) => {
        dispatch({ type: 'favorites/save-error', payload: { error } })
      }
    );
  };
}

export function cancelCellEdit(): CancelCellEditAction {
  return { type: 'favorites/cancel-cell-edit' };
}

export function deleteRows (deletedFavorites: Favorite[]): ActionThunk<DeleteAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/delete-rows' });
    const ids = deletedFavorites.map(favorite => favorite.id);
    wdkService.deleteFavorites(ids).then(
      () => {
        dispatch({ type: 'favorites/delete-multi-received', payload: { deletedFavorites } })
      },
      (error: ServiceError) => {
        dispatch({ type: 'favorites/delete-error', payload: { error }})
      }
    );
  }
}

export function deleteRow(deletedFavorite: Favorite): ActionThunk<DeleteAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/delete-row' });
    wdkService.deleteFavorite(deletedFavorite.id).then(
      () => {
        dispatch({ type: 'favorites/delete-received', payload: { deletedFavorite } })
      },
      (error: ServiceError) => {
        dispatch({ type: 'favorites/delete-error', payload: { error } })
      }
    );
  };
}

export function searchTerm(term: string): SearchTermAction {
  return { type: 'favorites/search-term', payload: term };
}

export function sortColumn(sortByKey: string, sortDirection: string): SortColumnAction {
  return { type: 'favorites/sort-column', payload: { sortByKey, sortDirection } };
}

export function filterByType(recordType: string): FilterByTypeAction {
  return { type: 'favorites/filter-by-type', payload: recordType };
}

export function undeleteRows(undeletedFavorites: Favorite[]): ActionThunk<AddAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/undelete-rows' });
    const ids = undeletedFavorites.map((favorite) => favorite.id);
    wdkService.undeleteFavorites(ids).then(
      () => {
        dispatch({ type: 'favorites/undeleted-rows-received', payload: { undeletedFavorites } });
      },
      (error: ServiceError) => {
        dispatch({ type: 'favorites/add-error', payload: { error } });
      }
    );
  };
}

export function addRow(addedFavorite: Favorite): ActionThunk<AddAction> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: 'favorites/add-row' });
    // TODO: Austin, not sure if we really need this method any more; see the PATCH methods in WdkService
    let record: RecordInstance = {
      displayName: "",
      id: addedFavorite.primaryKey,
      recordClassName: addedFavorite.recordClassName,
      attributes: {},
      tables: {},
      tableErrors: []
    };
    wdkService.addFavorite(record).then(
      () => {
        dispatch({ type: 'favorites/add-received', payload: { addedFavorite } })
      },
      (error: ServiceError) => {
        dispatch({ type: 'favorites/add-error', payload: { error } })
      }
    );
  };
}
