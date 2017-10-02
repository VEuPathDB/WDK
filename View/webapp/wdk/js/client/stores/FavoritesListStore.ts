import WdkStore, { BaseState } from './WdkStore';
import { isEqual } from 'lodash';
import { SortDirection } from 'react-virtualized';
import { ListLoadingAction,
  ListReceivedAction,
  ListErrorReceivedAction,
  FavoriteSelectedAction,
  FavoriteDeselectedAction,
  EditCellAction,
  ChangeCellAction,
  CancelCellEditAction,
  SaveCellDataAction,
  SaveReceivedAction,
  SaveErrorReceivedAction,
  DeleteRowAction,
  DeleteReceivedAction,
  DeleteErrorReceivedAction,
  SearchTermAction,
  SortColumnAction,
  AddRowAction,
  AddReceivedAction,
  AddErrorAction
} from '../actioncreators/FavoritesActionCreators';
import { Favorite } from '../utils/WdkModel';

type Action = ListLoadingAction|ListReceivedAction|ListErrorReceivedAction
|FavoriteSelectedAction|FavoriteDeselectedAction
|EditCellAction|ChangeCellAction|CancelCellEditAction
|SaveCellDataAction|SaveReceivedAction|SaveErrorReceivedAction
|DeleteRowAction|DeleteReceivedAction|DeleteErrorReceivedAction
|SearchTermAction|SortColumnAction
|AddRowAction|AddReceivedAction|AddErrorAction;

export interface State extends BaseState {
  favoritesLoading: boolean;
  list: Favorite[];
  filteredList: Favorite[];
  loadError: Error | null;
  existingFavorite: Favorite;
  editCoordinates: {};
  editValue: string;
  saveError: Error | null;
  deleteError: Error | null;
  deletedFavorite: Favorite | null;
  searchText: string;
  sortBy: string;
  sortDirection: string;
  selectedFavorites: number[];
  key: string;
}

export default class FavoritesListStore extends WdkStore<State> {

  getInitialState() {
    return Object.assign({
      favoritesLoading: false,
      key: "",
      list: [],
      filteredList: [],
      loadError: null,
      existingFavorite: {},
      editCoordinates: {},
      editValue: '',
      saveError: null,
      deleteError: null,
      deletedFavorite: null,
      searchText: '',
      sortBy: 'display',
      sortDirection: SortDirection.ASC,
      selectedFavorites: [],
    }, super.getInitialState());
  }

  handleAction(state: State, action: Action): State {
    switch (action.type) {

      // Identifies a loading page so that a spinner can be put in place of page data.
      case 'favorites/list-loading': return {
          ...state,
          favoritesLoading: true
        };

      // Once the favorites list is received, it is placed in the list and filteredList variables.  The fiteredList
      // is the list that will be presented to the user.
      case 'favorites/list-received': {
        const { list } = action.payload;
        const favoritesLoading = false;
        const listIds = list.map((fav: Favorite) => fav.id);
        const selectedFavorites = state.selectedFavorites.filter(id => {
          return listIds.indexOf(id) >= 0;
        });
        return {
          ...state,
          list,
          favoritesLoading,
          selectedFavorites,
          filteredList: list
        };
      }

      case 'favorites/favorite-selected': {
        const { id } = action.payload;
        const selectedFavorites = state.selectedFavorites.includes(id)
          ? state.selectedFavorites
          : [...state.selectedFavorites, id];
        return {
          ...state,
          selectedFavorites
        };
      }

      case 'favorites/favorite-deselected': {
        const { id } = action.payload;
        const selectedFavorites = state.selectedFavorites.includes(id)
          ? state.selectedFavorites.filter(fav => fav !== id)
          : state.selectedFavorites;
        return {
          ...state,
          selectedFavorites
        };
      }

      // If the user visits this page without being logged in, the 403 status code is returned.  The user needs
      // something more meaningful than a generic error message in that case.
      case 'favorites/list-error':
        if (action.payload.error.status === 403) {
          return {
            ...state,
            favoritesLoading: false
          }
        }
        else {
          return {
            ...state,
            favoritesLoading: false,
            loadError: action.payload.error
          }
        }

      // State applied when a cell is edited.  The assumption is that only one edit at a time is done.  So
      // if a user clicks edit links consecutively, only the cell for which the last edit link was clicked gains focus.
      // The coordinates of that cell are saved to the editCoordinates variable.  The variable, editValue holds the edited
      // value.  The existingFavorite variable holds the information for the favorite being edited.
      case 'favorites/edit-cell': return {
          ...state,
          key: action.payload.key,
          existingFavorite: Object.assign(state.existingFavorite,action.payload.rowData),
          editCoordinates: action.payload.coordinates,
          editValue: action.payload.value
        };

      // The variable holding the edited value is updated upon changes.
      case 'favorites/change-cell': return {
          ...state,
          editValue: action.payload
        };

      // When the edit is cancelled the variable identifying the cell to be edited
      case 'favorites/cancel-cell-edit': return {
          ...state,
          editCoordinates: {}
        };

      // The favorites list is saved with the updated favorite but whether that updated favorite is added to the filtered
      // version of that list depends on whether the changes have altered its ability to meet the existing search criteria.
      case 'favorites/save-received':
        let updatedList = state.list.map((favorite) => isEqual(favorite.id, state.existingFavorite.id) ? action.payload.updatedFavorite : favorite);
        let updatedFilteredList = this._meetsSearchCriteria(action.payload.updatedFavorite, state.searchText, state) ?
        state.filteredList.map((favorite) => isEqual(favorite.id, state.existingFavorite.id) ? action.payload.updatedFavorite : favorite)
        : state.filteredList.filter((favorite) => !isEqual(favorite.id, action.payload.updatedFavorite.id));
        return {
          ...state,
          list: updatedList,
          filteredList: updatedFilteredList,
          editCoordinates: {},
        };

      case 'favorites/save-error':
        return {
          ...state,
          saveError: action.payload.error
        };

      case 'favorites/delete-received':
        updatedList = state.list.filter((favorite) => !isEqual(favorite.id, action.payload.deletedFavorite.id));
        updatedFilteredList = state.filteredList.filter((favorite) => !isEqual(favorite.id, action.payload.deletedFavorite.id));
        return {
          ...state,
          list: updatedList,
          filteredList: updatedFilteredList,
          deletedFavorite: action.payload.deletedFavorite,
          editCoordinates: {}
        };

      case 'favorites/delete-error':
        return {
          ...state,
          deleteError: action.payload.error
        };

      case 'favorites/search-term': {
        let filteredList = action.payload.length ?
        state.list.filter((favorite) => this._meetsSearchCriteria(favorite, action.payload, state))
        : state.list;
        return {
          ...state,
          searchText: action.payload,
          filteredList: filteredList,
          editCoordinates: {}
        };
      }

      case 'favorites/sort-column':
      return {
        ...state,
        sortBy: action.payload.sortBy,
        sortDirection: action.payload.sortDirection
      };

      // Probably should not be mutating
      case 'favorites/add-received': {
        let { list, searchText, filteredList } = state;
        let { addedFavorite } = action.payload;
        let meetsCriteria = this._meetsSearchCriteria(addedFavorite, searchText, state);

        return {
          ...state,
          list: list.concat([addedFavorite]),
          filteredList: !meetsCriteria ? filteredList : filteredList.concat(addedFavorite),
          deletedFavorite: null
        };
      }

      default:
        return state;
    }
  }

  _meetsSearchCriteria(favorite:Favorite, searchText:string, state:State) {
    searchText = searchText.toLowerCase();
    return (
      (favorite.displayName.toLowerCase().indexOf(searchText) > -1) ||
      (this._getType(favorite, state).toLowerCase().indexOf(searchText) > -1) ||
      (favorite.description != null && favorite.description.toLowerCase().indexOf(searchText) > -1) ||
      (favorite.group != null && favorite.group.toLowerCase().indexOf(searchText) > -1)
    );
  }

  _getType(favorite:Favorite, state:State) {
    if (state.globalData.recordClasses == null) return 'Unknown';
    let recordClass = state.globalData.recordClasses.find((recordClass) => recordClass.name === favorite.recordClassName);
    return recordClass == null ? 'Unknown' : recordClass.displayName;
  }
}
