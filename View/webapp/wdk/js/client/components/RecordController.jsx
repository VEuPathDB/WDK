import {Component, PropTypes} from 'react';
import {wrappable} from '../utils/componentUtils';
import {wrapActions} from '../utils/actionHelpers';
import {
  setActiveRecord,
  updateSectionVisibility as toggleSection,
  updateNavigationQuery,
  updateAllFieldVisibility,
  updateNavigationSubcategoryVisibility,
  updateNavigationVisibility
} from '../actioncreators/RecordViewActionCreator';
import {
  loadCurrentUser,
  loadBasketStatus,
  loadFavoritesStatus,
  updateBasketStatus,
  updateFavoritesStatus
} from '../actioncreators/UserActionCreator';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';

/** View Controller for record page */
class RecordController extends Component {

  constructor(props) {
    super(props);
    this.state = this.getStateFromStores();
    this.actions = wrapActions(this.props.dispatchAction, {
      toggleSection,
      updateNavigationQuery,
      updateAllFieldVisibility,
      updateNavigationSubcategoryVisibility,
      updateNavigationVisibility
    });
  }

  getStateFromStores() {
    let recordView = this.props.stores.RecordViewStore.getState();
    let user = this.props.stores.UserStore.getState().user;
    return { recordView, user };
  }

  componentDidMount() {
    this.storeSubscriptions = [
      this.props.stores.RecordViewStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.UserStore.addListener(() => this.setState(this.getStateFromStores()))
    ];
    this.loadData(this.props);
  }

  componentWillUnmount() {
    this.storeSubscriptions.forEach(s => s.remove());
  }

  componentWillReceiveProps(nextProps) {
    // We need to do this to ignore hash changes.
    // Seems like there is a better way to do this.
    if (this.props.location.pathname !== nextProps.location.pathname) {
      this.loadData(nextProps);
    }
  }

  loadData(props) {
    let { dispatchAction } = props;
    let { recordClass, splat } = props.params;
    if (this.state.user == null) {
      dispatchAction(loadCurrentUser());
    }
    dispatchAction(setActiveRecord(recordClass, splat.split('/')))
    .then(() => {
      let { record, recordClass } = this.props.stores.RecordViewStore.getState();
      let { user } = this.props.stores.UserStore.getState();
      if (recordClass.useBasket) {
        dispatchAction(loadBasketStatus(user, record));
        dispatchAction(loadFavoritesStatus(user, record));
      }
    });
  }

  renderLoading() {
    if (this.state.recordView.isLoading) {
      return (
        <Loading/>
      );
    }
  }

  renderError() {
    if (this.state.recordView.error) {
      return (
        <div style={{padding: '1.5em', fontSize: '2em', color: 'darkred', textAlign: 'center'}}>
          The requested record could not be loaded.
        </div>
      );
    }
  }

  renderRecord() {
    let { recordView, user } = this.state;
    let { dispatchAction } = this.props;
    if (recordView.record != null && !recordView.isLoading) {
      let title = recordView.recordClass.displayName + ' ' +
        recordView.record.displayName;
      let loadingClassName = 'fa fa-circle-o-notch fa-spin';
      let headerActions = [];
      if (recordView.recordClass.useBasket) {
        headerActions.push({
          label: recordView.inBasket ? 'Remove from basket' : 'Add to basket',
          iconClassName: recordView.loadingBasketStatus ? loadingClassName : 'fa fa-shopping-basket',
          onClick(event) {
            event.preventDefault();
            dispatchAction(updateBasketStatus(user, recordView.record, !recordView.inBasket));
          }
        });
      }
      headerActions.push({
        label: recordView.inFavorites ? 'Remove from favorites' : 'Add to favorites',
        iconClassName: recordView.loadingFavoritesStatus ? loadingClassName : 'fa fa-lg fa-star',
        onClick(event) {
          event.preventDefault();
          dispatchAction(updateFavoritesStatus(user, recordView.record, !recordView.inFavorites));
        }
      },
      {
        label: 'Download ' + recordView.recordClass.displayName,
        iconClassName: 'fa fa-lg fa-download',
        href: '/record/' + recordView.recordClass.urlSegment + '/download/' +
          recordView.record.id.map(pk => pk.value).join('/')
      });

      return (
        <Doc title={title}>
          <RecordUI
            {...recordView}
            {...this.actions}
            headerActions={headerActions}
          />
        </Doc>
      );
    }
  }

  render() {
    return (
      <div>
        {this.renderLoading()}
        {this.renderError()}
        {this.renderRecord()}
      </div>
    );
  }

}

RecordController.propTypes = {
  stores: PropTypes.object.isRequired,
  dispatchAction: PropTypes.func.isRequired
}

export default wrappable(RecordController);
