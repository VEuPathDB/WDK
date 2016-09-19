import { omit, pick } from 'lodash';
import WdkViewController from './WdkViewController';
import {wrappable} from '../utils/componentUtils';
import {
  loadRecordData,
  updateSectionVisibility,
  updateNavigationQuery,
  updateAllFieldVisibility,
  updateNavigationSubcategoryVisibility,
  updateNavigationVisibility,
  updateActiveSection
} from '../actioncreators/RecordViewActionCreators';
import * as UserActionCreators from '../actioncreators/UserActionCreators';
import RecordUI from '../components/RecordUI';

/** View Controller for record page */
class RecordController extends WdkViewController {

  getStoreName() {
    return 'RecordViewStore';
  }

  getStateFromStore(store) {
    let state = store.getState();
    return Object.assign(omit(state, 'globalData'), pick(state.globalData, 'user'));
  }

  // XXX Should parent class always include UserActionCreators, so all
  // components have access to them?
  getActionCreators() {
    return Object.assign({
      updateSectionVisibility,
      updateNavigationQuery,
      updateAllFieldVisibility,
      updateNavigationSubcategoryVisibility,
      updateNavigationVisibility,
      updateActiveSection
    }, UserActionCreators);
  }

  isRenderDataLoaded(state) {
    return !state.isLoading;
  }

  getTitle(state) {
    return  state.error ? 'Error: ' + this.getErrorMessage(state)
          : state.recordClass == null || state.record == null ? "Loading..."
          : state.recordClass.displayName + ' ' + state.record.displayName;
  }

  getErrorMessage(state) {
    return 'The requested record ' + (state.error.status === 404 ?
      'does not exist.' : 'could not be loaded.');
  }

  loadData(state, props, previousProps) {
    // We need to check pathname to ignore hash changes.
    if (previousProps == null || props.location.pathname !== previousProps.location.pathname) {
      let { recordClass, splat } = props.params;
      this.dispatchAction(loadRecordData(
        recordClass, splat.split('/'), props.location.hash.slice(1) || null));
    }
  }

  renderError(state) {
    if (state.error) {
      return (
        <div style={{padding: '1.5em', fontSize: '2em', color: 'darkred', textAlign: 'center'}}>
          {this.getErrorMessage(state)}
        </div>
      );
    }
  }

  renderRecord(state, eventHandlers) {
    if (state.record) {
      let { user, record, recordClass, inBasket, inFavorites,
        loadingBasketStatus, loadingFavoritesStatus } = state;
      let loadingClassName = 'fa fa-circle-o-notch fa-spin';
      let headerActions = [];
      if (recordClass.useBasket) {
        headerActions.push({
          label: inBasket ? 'Remove from basket' : 'Add to basket',
          iconClassName: loadingBasketStatus ? loadingClassName : 'fa fa-shopping-basket',
          onClick(event) {
            event.preventDefault();
            eventHandlers.updateBasketStatus(user, record, !inBasket);
          }
        });
      }
      headerActions.push({
        label: inFavorites ? 'Remove from favorites' : 'Add to favorites',
        iconClassName: loadingFavoritesStatus ? loadingClassName : 'fa fa-lg fa-star',
        onClick(event) {
          event.preventDefault();
          eventHandlers.updateFavoritesStatus(user, record, !inFavorites);
        }
      },
      {
        label: 'Download ' + recordClass.displayName,
        iconClassName: 'fa fa-lg fa-download',
        href: '/record/' + recordClass.urlSegment + '/download/' +
          record.id.map(pk => pk.value).join('/')
      });

      return (
        <RecordUI
          {...omit(state, 'user')}
          {...eventHandlers}
          headerActions={headerActions}
        />
      );
    }
  }

  renderView(state, eventHandlers) {
    return (
      <div>
        {this.renderError(state, eventHandlers)}
        {this.renderRecord(state, eventHandlers)}
      </div>
    );
  }

}

export default wrappable(RecordController);
