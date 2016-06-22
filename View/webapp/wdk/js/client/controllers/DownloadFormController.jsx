import { wrappable } from '../utils/componentUtils';
import WdkViewController from './WdkViewController';
import * as DownloadFormActionCreator from '../actioncreators/DownloadFormActionCreator';
import DownloadFormContainer from '../components/DownloadFormContainer';

class DownloadFormController extends WdkViewController {

  getStoreName() {
    return "DownloadFormStore";
  }

  getActionCreators() {
    return DownloadFormActionCreator;
  }

  isRenderDataLoaded(state) {
    return (state.step != null && !state.isLoading);
  }

  getTitle(state) {
    return (!this.isRenderDataLoaded(state) ? "Loading..." :
      "Download: " + state.step.displayName);
  }

  renderView(state, eventHandlers) {
    // build props object to pass to form component
    let formProps = Object.assign({}, state, eventHandlers, {
      // passing summary view in case reporters handle view links differently
      summaryView: this.props.location.query.summaryView
    });
    return ( <DownloadFormContainer {...formProps}/> );
  }

  componentDidMount() {
    this.handleIncomingProps(this.props);
  }

  componentWillReceiveProps(nextProps) {
    this.handleIncomingProps(nextProps);
  }

  handleIncomingProps(props) {
    // must reinitialize with every new props
    let { params, dispatchAction } = props;
    if ('stepId' in params) {
      dispatchAction(DownloadFormActionCreator.loadPageDataFromStepId(params.stepId));
    }
    else if ('recordClass' in params) {
      dispatchAction(DownloadFormActionCreator.loadPageDataFromRecord(
          params.recordClass, params.splat.split('/').join(',')));
    }
    else {
      console.error("Neither stepId nor recordClass param was passed " +
          "to StepDownloadFormController component");
    }
  }
}

export default wrappable(DownloadFormController);
