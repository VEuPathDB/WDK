import {Component} from 'react';
import {wrappable} from '../utils/componentUtils';
import {setActiveRecord, updateSectionCollapsed} from '../actioncreators/RecordViewActionCreator';
import {loadCurrentUser} from '../actioncreators/UserActionCreator';
import {loadBasketStatus, updateBasketStatus} from '../actioncreators/BasketActionCreator';
import {loadFavoritesStatus, updateFavoritesStatus} from '../actioncreators/FavoritesActionCreator';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';

/** View Controller for record page */
class RecordController extends Component {

  constructor(props) {
    super(props);
    let { dispatchAction } = props;
    this.state = this.getStateFromStores();
    this.toggleSection = (sectionName, isCollapsed) => {
      return dispatchAction(updateSectionCollapsed(sectionName, isCollapsed));
    };
  }

  getStateFromStores() {
    let recordView = this.props.stores.RecordViewStore.getState();
    let user = this.props.stores.UserStore.getState().user;
    let record = recordView.record;
    let basketEntry = record && this.props.stores.BasketStore.getEntry(record);
    let favoritesEntry = record && this.props.stores.FavoritesStore.getEntry(record);
    return { recordView, basketEntry, favoritesEntry, user };
  }

  componentDidMount() {
    this.storeSubscriptions = [
      this.props.stores.RecordViewStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.UserStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.BasketStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.FavoritesStore.addListener(() => this.setState(this.getStateFromStores()))
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
      let record = props.stores.RecordViewStore.getState().record;
      dispatchAction(loadBasketStatus(record));
      dispatchAction(loadFavoritesStatus(record));
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
    let { recordView, basketEntry, favoritesEntry, user } = this.state;
    let { router, dispatchAction } = this.props;
    if (recordView.record != null) {
      let title = recordView.recordClass.displayName + ' ' +
        recordView.record.displayName;
      let loadingClassName = 'fa fa-circle-o-notch fa-spin';
      let isInBasket = basketEntry && basketEntry.isInBasket;
      let basketLoading = basketEntry && basketEntry.isLoading;
      let isInFavorites = favoritesEntry && favoritesEntry.isInFavorites;
      let favoritesLoading = favoritesEntry && favoritesEntry.isLoading;
      let headerActions = (user.isGuest ? [
        {
          label: 'Login for basket and favorites',
          className: 'open-dialog-login-form',
          iconClassName: 'fa fa-lg fa-sign-in',
          onClick(event) {
            event.preventDefault();
            console.warn('Replace the className based dialog opening with something else.');
          }
        }
      ] : [
        {
          label: isInBasket ? 'Remove from basket' : 'Add to basket',
          iconClassName: basketLoading ? loadingClassName : 'fa fa-shopping-basket',
          onClick(event) {
            event.preventDefault();
            dispatchAction(updateBasketStatus(recordView.record, !isInBasket));
          }
        },
        {
          label: isInFavorites ? 'Remove from favorites' : 'Add to favorites',
          iconClassName: favoritesLoading ? loadingClassName : 'fa fa-lg fa-star',
          onClick(event) {
            event.preventDefault();
            dispatchAction(updateFavoritesStatus(recordView.record, !isInFavorites));
          }
        },
      ])
      .concat({
        label: 'Download ' + recordView.recordClass.displayName,
        iconClassName: 'fa fa-lg fa-download',
        href: '/record/' + recordView.recordClass.urlSegment + '/download/' +
          recordView.record.id.map(pk => pk.value).join('/')
      });

      return (
        <Doc title={title}>
          <RecordUI
            {...recordView}
            toggleSection={this.toggleSection}
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

export default wrappable(RecordController);
