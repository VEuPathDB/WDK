import { Component } from 'react';
import Doc from './Doc';
import Loading from './Loading';
import RecordUI from './RecordUI';
import { wrappable } from '../utils/componentUtils';

class RecordController extends Component {

  constructor(props) {
    super(props);
    this.recordViewStore = props.stores.RecordViewStore;
    this.userStore = props.stores.UserStore;
    this.recordViewActions = props.actionCreators.RecordViewActionCreator;
    this.userActions = props.actionCreators.UserActionCreator;
    this.state = this.getStateFromStores();
  }

  getStateFromStores() {
    return {
      recordView: this.recordViewStore.getState(),
      user: this.userStore.getState()
    };
  }

  componentDidMount() {
    this.storeSubscriptions = [
      this.recordViewStore.addListener(() => this.setState(this.getStateFromStores())),
      this.userStore.addListener(() => this.setState(this.getStateFromStores()))
    ];
    this.userActions.loadCurrentUser();
    this.fetchRecord(this.props);
  }

  componentWillUnmount() {
    this.storeSubscriptions.forEach(s => s.remove());
  }

  componentWillReceiveProps(nextProps) {
    this.fetchRecord(nextProps);
  }

  fetchRecord(props) {
    let { recordClass, splat } = props.params;
    this.recordViewActions.fetchRecordDetails(recordClass, splat.split('/'));
  }

  renderLoading() {
    if (this.state.recordView.isLoading || this.state.user.isLoading) {
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
    if (recordView.record != null) {
      let title = this.state.recordView.recordClass.displayName + ' ' +
        recordView.record.displayName;

      return (
        <Doc title={title}>
          <RecordUI
            {...recordView}
            {...user}
            recordActions={this.recordViewActions}
            userActions={this.userActions}
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
