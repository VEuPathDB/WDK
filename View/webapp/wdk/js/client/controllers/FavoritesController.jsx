import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import { loadFavoritesList, editCell, saveCellData, changeCell, cancelCellEdit, deleteRow, searchTerm, sortColumn, addRow } from '../actioncreators/FavoritesActionCreators';
import FavoritesList from '../components/FavoritesList';

class FavoritesListController extends WdkViewController {

  getStoreName() {
    return "FavoritesListStore";
  }

  getStateFromStore(store) {
    const {
      globalData: { user, recordClasses },
      favoritesLoading,
      list,
      loadError,
      existingFavorite,
      editCoordinates,
      editValue,
      searchText,
      filteredList,
      sortBy,
      sortDirection,
      deletedFavorite
    } = store.getState();

    return {
      user,
      recordClasses,
      favoritesLoading,
      list,
      loadError,
      existingFavorite,
      editCoordinates,
      editValue,
      searchText,
      filteredList,
      sortBy,
      sortDirection,
      deletedFavorite
    };
  }

  getTitle() {
    return 'Favorites';
  }

  getActionCreators() {
    return {
      loadFavoritesList,
      editCell,
      changeCell,
      saveCellData,
      cancelCellEdit,
      deleteRow,
      searchTerm,
      sortColumn,
      addRow
    };
  }

  loadData() {
    this.eventHandlers.loadFavoritesList();
  }

  isRenderDataLoaded(state) {
    return state.user && state.favoritesLoading === false;
  }

  isRenderDataLoadError(state) {
    return state.loadError;
  }

  renderView(state, eventHandlers) {
    return ( <FavoritesList {...state}
                            favoritesEvents={eventHandlers}
                            searchBoxPlaceholder="Search Favorites..."
                            searchBoxHelp="All table columns will be searched"

    /> );
  }
}

export default wrappable(FavoritesListController);
