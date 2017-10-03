import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractViewController from './AbstractViewController';
import * as ActionCreators from '../actioncreators/FavoritesActionCreators';
import FavoritesList from '../components/FavoritesList';
import FavoritesListStore, { State as StoreState } from "../stores/FavoritesListStore";

type State = Pick<StoreState,
  'favoritesLoading' |
  'list' |
  'loadError' |
  'existingFavorite' |
  'editCoordinates' |
  'editValue' |
  'selectedFavorites' |
  'searchText' |
  'filteredList' |
  'sortByKey' |
  'sortDirection' |
  'deletedFavorite'
> & Pick<StoreState['globalData'], 'user' | 'recordClasses'>;

class FavoritesListController extends AbstractViewController<State, FavoritesListStore, typeof ActionCreators> {

  getStoreClass() {
    return FavoritesListStore;
  }

  getStateFromStore() {
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
      sortByKey,
      sortDirection,
      deletedFavorite,
      selectedFavorites
    } = this.store.getState();

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
      sortByKey,
      sortDirection,
      deletedFavorite,
      selectedFavorites
    };
  }

  getTitle () {
    return 'Favorites';
  }

  getActionCreators () {
    return ActionCreators
  }

  loadData () {
    this.eventHandlers.loadFavoritesList();
  }

  isRenderDataLoaded() {
    return this.state.user != null && this.state.favoritesLoading === false;
  }

  isRenderDataLoadError() {
    return this.state.loadError != null;
  }

  renderView() {
    return (
      <FavoritesList
        {...this.state}
        favoritesEvents={this.eventHandlers}
        searchBoxPlaceholder="Search Favorites..."
        searchBoxHelp="All table columns will be searched"
      />
    );
  }
}

export default wrappable(FavoritesListController);
