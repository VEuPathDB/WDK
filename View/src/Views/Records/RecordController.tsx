import * as React from 'react';
import { get, isEqual, omit, pick } from 'lodash';
import AbstractPageController from 'Core/Controllers/AbstractPageController';
import { ViewControllerProps } from 'Core/CommonTypes';
import {wrappable} from 'Utils/ComponentUtils';
import {
  loadRecordData,
  updateSectionVisibility,
  updateNavigationQuery,
  updateAllFieldVisibility,
  updateNavigationCategoryExpansion,
  updateNavigationVisibility
} from 'Views/Records/RecordViewActionCreators';
import * as UserActionCreators from 'Core/ActionCreators/UserActionCreators';
import RecordUI from 'Views/Records/RecordUI';
import RecordViewStore, { State } from "Views/Records/RecordViewStore";

const ActionCreators = {
  ...UserActionCreators,
  loadRecordData,
  updateSectionVisibility,
  updateNavigationQuery,
  updateAllFieldVisibility,
  updateNavigationCategoryExpansion,
  updateNavigationVisibility
};

// FIXME Remove when RecordUI is converted to Typescript
const CastRecordUI: any = RecordUI;

/** View Controller for record page */
class RecordController extends AbstractPageController<State, RecordViewStore, typeof ActionCreators> {

  getStoreClass() {
    return RecordViewStore;
  }

  getStateFromStore() {
    return this.store.getState();
  }

  // XXX Should parent class always include UserActionCreators, so all
  // components have access to them?
  getActionCreators() {
    return ActionCreators;
  }

  isRenderDataLoaded() {
    return (
      this.state.recordClass != null &&
      this.state.record != null &&
      this.state.globalData.user != null &&
      !this.state.isLoading
    );
  }

  isRenderDataLoadError() {
    return (
      this.state.error != null &&
      this.state.error.status !== 404 &&
      this.state.record == null
    );
  }

  isRenderDataNotFound() {
    return (
      this.state.error != null &&
      this.state.error.status === 404
    );
  }

  getTitle() {
    return this.state.recordClass.displayName + ' ' + this.state.record.displayName;
  }

  getErrorMessage(status: number) {
    return 'The requested record ' + (status === 404 ?
      'does not exist.' : 'could not be loaded.');
  }

  loadData(previousProps?: ViewControllerProps<RecordViewStore>) {
    // load data if params have changed
    if (!isEqual(get(this.props, 'match.params'), get(previousProps, 'match.params'))) {
      let { recordClass, primaryKey } = this.props.match.params;
      let pkValues = primaryKey.split('/');
      this.eventHandlers.loadRecordData(recordClass, pkValues);
    }
  }

  renderStateError() {
    if (this.state.error == null) return ;

    return (
      <div style={{padding: '1.5em', fontSize: '2em', color: 'darkred', textAlign: 'center'}}>
        {this.getErrorMessage(this.state.error.status)}
      </div>
    );
  }

  renderRecord() {
    if (this.state.record == null) return null;

    let { record, recordClass, inBasket, favoriteId,
      loadingBasketStatus, loadingFavoritesStatus } = this.state;
    let loadingClassName = 'fa fa-circle-o-notch fa-spin';
    let headerActions = [];
    if (recordClass.useBasket) {
      headerActions.push({
        label: inBasket ? 'Remove from basket' : 'Add to basket',
        iconClassName: loadingBasketStatus ? loadingClassName : 'fa fa-shopping-basket',
        onClick: (event: Event) => {
          event.preventDefault();
          this.eventHandlers.updateBasketStatus(record, !inBasket);
        }
      });
    }
    headerActions.push({
      label: favoriteId ? 'Remove from favorites' : 'Add to favorites',
      iconClassName: loadingFavoritesStatus ? loadingClassName : 'fa fa-lg fa-star',
      onClick: (event: Event) => {
        event.preventDefault();
        if (favoriteId) {
          this.eventHandlers.removeFavorite(record, favoriteId);
        }
        else {
          this.eventHandlers.addFavorite(record);
        }
      }
    });

    if (recordClass.formats.some(format => format.scopes.includes('record'))) {
      headerActions.push({
        label: 'Download ' + recordClass.displayName,
        iconClassName: 'fa fa-lg fa-download',
        href: '/record/' + recordClass.urlSegment + '/download/' +
          record.id.map(pk => pk.value).join('/')
      });
    }

    return (
      <CastRecordUI
        {...omit(this.state, 'user')}
        {...this.eventHandlers}
        headerActions={headerActions}
      />
    );
  }

  renderView() {
    return (
      <div>
        {this.renderStateError()}
        {this.renderRecord()}
      </div>
    );
  }

}

export default wrappable(RecordController);
