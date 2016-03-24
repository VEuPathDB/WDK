import {Component} from 'react';
import {wrappable} from '../utils/componentUtils';
import {setActiveRecord, updateSectionCollapsed} from '../actioncreators/RecordViewActionCreator';
import {loadCurrentUser} from '../actioncreators/UserActionCreator';
import {loadBasketStatus, updateBasketStatus} from '../actioncreators/BasketActionCreator';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';

class RecordController extends Component {

  constructor(props) {
    super(props);
    let { dispatchAction } = props;
    this.state = this.getStateFromStores();

    this.toggleSection = (sectionName, isCollapsed) => {
      return dispatchAction(updateSectionCollapsed(sectionName, isCollapsed));
    };

    this.updateBasketStatus = (status) => {
      let { record } = this.state.recordView;
      return dispatchAction(updateBasketStatus(record, status));
    };
  }

  getStateFromStores() {
    let recordView = this.props.stores.RecordViewStore.getState();
    let user = this.props.stores.UserStore.getState().user;
    let record = recordView.record;
    let basketEntry = record && this.props.stores.BasketStore.getEntry(record);
    return { recordView, basketEntry, user };
  }

  componentDidMount() {
    this.storeSubscriptions = [
      this.props.stores.RecordViewStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.UserStore.addListener(() => this.setState(this.getStateFromStores())),
      this.props.stores.BasketStore.addListener(() => this.setState(this.getStateFromStores()))
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
    let { recordView, basketEntry, user } = this.state;
    if (recordView.record != null) {
      let title = this.state.recordView.recordClass.displayName + ' ' +
        recordView.record.displayName;

      return (
        <Doc title={title}>
          <RecordUI
            {...recordView}
            user={user}
            basketEntry={basketEntry}
            updateBasketStatus={this.updateBasketStatus}
            toggleSection={this.toggleSection}
            router={this.props.router}
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
