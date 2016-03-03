// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import { wrapActions } from '../utils/actionHelpers';
import Doc from './Doc';
import Loading from './Loading';
import StepDownloadFormPage from './StepDownloadFormPage';
import * as StepDownloadFormViewActionCreator from '../actioncreators/StepDownloadFormViewActionCreator';
import * as UserActionCreator from '../actioncreators/UserActionCreator';

let StepDownloadFormController = React.createClass({

  loadUserState() {
    this.setState(Object.assign({}, this.state, { userData: this.userStore.getState() }));
  },

  loadViewState() {
    this.setState(Object.assign({}, this.state, { viewData: this.store.getState() }));
  },

  componentWillMount() {

    // get actions for this view
    this.viewActions = wrapActions(this.props.dispatchAction, StepDownloadFormViewActionCreator);
    this.userActions = wrapActions(this.props.dispatchAction, UserActionCreator);

    // get the user store, load data from it, and subscribe
    this.userStore = this.props.stores.UserStore;
    this.userStoreSubscription = this.userStore.addListener(this.loadUserState);
    this.loadUserState();

    // get the view store, load data from it, and subscribe
    this.store = this.props.stores.StepDownloadFormViewStore;
    this.storeSubscription = this.store.addListener(this.loadViewState);
    this.loadViewState();
  },

  componentDidMount() {
    // initialize user store if it isn't already
    if (this.state.userData.user == null) {
      this.userActions.loadCurrentUser();
    }

    // must reinitialize with every new props
    let params = this.props.params;
    if ('stepId' in params) {
      this.viewActions.loadPageDataFromStepId(params.stepId);
    }
    else if ('recordClass' in params) {
      this.viewActions.loadPageDataFromRecord(params.recordClass, params.splat.split('/').join(','));
    }
    else {
      console.error("Neither stepId nor recordClass param passed to StepDownloadFormController component");
    }
  },

  componentWillUnmount() {
    this.viewActions.unloadPageData();
    this.userStoreSubscription.remove();
    this.storeSubscription.remove();
  },

  isStateIncomplete(state) {
    return (this.state == null ||
      this.state.userData.isLoading || this.state.userData.user == null ||
      this.state.viewData.isLoading || this.state.viewData.step == null);
  },

  render() {

    let title = "Download Records";

    if (this.isStateIncomplete(this.state)) {
      return ( <Doc title={title}><Loading/></Doc> );
    }

    let submitForm = () => {
      let { step, selectedReporter, formState } = this.state.viewData;
      this.viewActions.submitForm(step, selectedReporter, formState);
    };

    // build props object to pass to form component
    let formProps = {
      step: this.state.viewData.step,
      summaryView: this.props.query.summaryView,
      question: this.state.viewData.question,
      recordClass: this.state.viewData.recordClass,
      availableReporters: this.state.viewData.availableReporters,
      user: this.state.userData.user,
      preferences: this.state.userData.preferences,
      ontology: this.state.viewData.ontology,
      selectedReporter: this.state.viewData.selectedReporter,
      onReporterChange: this.viewActions.selectReporter,
      formState: this.state.viewData.formState,
      formUiState: this.state.viewData.formUiState,
      onFormChange: this.viewActions.updateFormState,
      onFormUiChange: this.viewActions.updateFormUiState,
      onSubmit: submitForm
    };

    // render form
    title = title + ": " + this.state.viewData.step.displayName;
    return ( <Doc title={title}><StepDownloadFormPage {...formProps}/></Doc> );
  }
});

export default wrappable(StepDownloadFormController);
