import {PropTypes} from 'react';
import WdkViewController from './WdkViewController';
import {wrappable} from '../utils/componentUtils';
import {
  loadRecordData,
  updateSectionVisibility as toggleSection,
  updateNavigationQuery,
  updateAllFieldVisibility,
  updateNavigationSubcategoryVisibility,
  updateNavigationVisibility
} from '../actioncreators/RecordViewActionCreator';
import {
  updateBasketStatus,
  updateFavoritesStatus
} from '../actioncreators/UserActionCreator';
import RecordUI from '../components/RecordUI';

/** View Controller for record page */
class RecordController extends WdkViewController {

  getStoreName() {
    return 'RecordViewStore';
  }

  getActionCreators() {
    return {
      toggleSection,
      updateNavigationQuery,
      updateAllFieldVisibility,
      updateNavigationSubcategoryVisibility,
      updateNavigationVisibility,
      updateBasketStatus,
      updateFavoritesStatus
    };
  }

  isRenderDataLoaded(state) {
    return state.record != null && !state.isLoading;
  }

  getTitle(state) {
    return (state.recordClass == null || state.record == null ? "Loading..." :
      state.recordClass.displayName + ' ' + state.record.displayName);
  }

  componentDidMount() {
    this.loadData(this.props);
  }

  componentWillReceiveProps(nextProps) {
    // We need to do this to ignore hash changes.
    if (this.props.location.pathname !== nextProps.location.pathname) {
      this.loadData(nextProps);
    }
  }

  loadData(props) {
    let { recordClass, splat } = props.params;
    this.props.dispatchAction(loadRecordData(recordClass, splat.split('/')));
  }

  renderError(state) {
    if (state.error) {
      return (
        <div style={{padding: '1.5em', fontSize: '2em', color: 'darkred', textAlign: 'center'}}>
          The requested record could not be loaded.
        </div>
      );
    }
  }

  renderRecord(state, eventHandlers) {
    let { user, record, recordClass, inBasket, inFavorites,
      loadingBasketStatus, loadingFavoritesStatus } = state;
    let title = recordClass.displayName + ' ' + record.displayName;
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
        {...state}
        {...eventHandlers}
        headerActions={headerActions}
      />
    );
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

RecordController.propTypes = {
  stores: PropTypes.object.isRequired,
  dispatchAction: PropTypes.func.isRequired
}

export default wrappable(RecordController);
