import { pick } from 'lodash';
import * as React from 'react';
import { wrappable } from '../utils/componentUtils';
import AbstractPageController from './AbstractPageController';
import * as ActionCreators from '../actioncreators/FavoritesActionCreators';
import _FavoritesList from '../components/FavoritesList';
import FavoritesListStore, { State as StoreState } from "../stores/FavoritesListStore";

// FIXME Convert FavoritesList to TypeScript
const FavoritesList: any = _FavoritesList;

type State = Pick<StoreState,
  'tableState' |
  'tableSelection' |
  'favoritesLoading' |
  'loadError' |
  'existingFavorite' |
  'editCoordinates' |
  'editValue' |
  'searchText'
> & Pick<StoreState['globalData'], 'user' | 'recordClasses'>;

class FavoritesListController extends AbstractPageController<State, FavoritesListStore, typeof ActionCreators> {
  getStoreClass() {
    return FavoritesListStore;
  }

  getStateFromStore() {
    const {
      globalData: { user, recordClasses },
      tableState,
      tableSelection,

      favoritesLoading,
      loadError,
      existingFavorite,
      editCoordinates,
      editValue,
      searchText,
      deletedFavorite,
      filterByType
    } = this.store.getState();

    return {
      tableState,
      tableSelection,
      user,
      recordClasses,
      favoritesLoading,
      loadError,
      existingFavorite,
      editCoordinates,
      editValue,
      searchText,
      deletedFavorite,
      filterByType
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
