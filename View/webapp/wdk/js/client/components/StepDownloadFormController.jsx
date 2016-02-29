// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import { wrapActions } from '../utils/actionHelpers';
import Doc from './Doc';
import Loading from './Loading';
import StepDownloadFormPage from './StepDownloadFormPage';
import * as StepDownloadFormViewActionCreator from '../actioncreators/StepDownloadFormViewActionCreator';
import * as UserActionCreator from '../actioncreators/UserActionCreator';

// What are parameters to this page?
//  1. Step ID (path param)
//
// What data does this page need?
//  1. Step by passed ID (question name, then params, filters, etc. for eventual form submission)
//  2. Question of the step (+ record class)
//  3. User preferences (by question) for attribs, tab selected, maybe other data
//        (e.g. transcript view vs gene view) from results page
//  4. Possible formats/reporters for this recordclass
//  5. RecordClass-specific ontology (from which Reporter-specific ontology subsets for attributes and tables can be extracted)
//
// What component tree will be here?
//  (header/footer still in pageFrame)
//  - Primary download form
//     - Drop down for reporter types
//       - React's <form>
//         - WDK "standard" reporter form (contains <div> with form elements)
//         - Reporter-specific form components (can maybe merge some of these together)
//         - WDK provides submit button
//
// Details:
//    <ReporterForm/>



//      a. What to display for each reporter
//           i. handled by custom reporter form components
//      b. Possible attributes and tables
//           i. Fetched from a combination of record class and question, organization provided by ontology
//           ii. Attribute and table trees are components included in custom reporter form components
//      c. 

let StepDownloadFormController = React.createClass({

  loadUserState() {
    this.setState(Object.assign({}, this.state, { userData: this.userStore.getState() }));
  },

  loadViewState() {
    this.setState(Object.assign({}, this.state, { viewData: this.store.getState() }));
  },

  componentWillMount() {

    // get actions for this view
    this.actions = wrapActions(this.props.dispatchAction, StepDownloadFormViewActionCreator);
    this.userActions = wrapActions(this.props.dispatchAction, UserActionCreator);

    // get the user store, load data from it, and subscribe
    this.userStore = this.props.stores.UserStore;
    this.userStoreSubscription = this.userStore.addListener(this.loadUserState);
    this.loadUserState();

    // get the view store, load data from it, and subscribe
    this.store = this.props.stores.StepDownloadFormViewStore;
    this.storeSubscription = this.store.addListener(this.loadViewState);
    this.loadViewState();

    // Bind methods of `this.formEvents` to `this`. When they are called by
    // child elements, any reference to `this` in the methods will refer to
    // this component.
    for (let key in this.formEvents) {
      this.formEvents[key] = this.formEvents[key].bind(this);
    }
  },

  componentDidMount() {
    // initialize user store if it isn't already
    if (this.state.userData.user == null) {
      this.userActions.loadCurrentUser();
    }

    // must reinitialize with every new props
    let params = this.props.params;
    if ('stepId' in params) {
      this.actions.loadPageData(params.stepId);
    }
    else if ('recordClass' in params) {
      this.actions.loadPageDataFromRecord(params.recordClass, params.splat.split('/').join(','));
    }
    else {
      console.error("Neither stepId nor recordClass param passed to StepDownloadFormController component");
    }
  },

  componentWillUnmount() {
    this.actions.unloadPageData();
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

    // build props object to pass to form component
    let formProps = {
      step: this.state.viewData.step,
      summaryView: this.props.query.summaryView,
      question: this.state.viewData.question,
      recordClass: this.state.viewData.recordClass,
      user: this.state.userData.user,
      preferences: this.state.userData.preferences,
      ontology: this.state.viewData.ontology,
      selectedReporter: this.state.viewData.selectedReporter,
      onReporterChange: this.formEvents.changeReporter,
      formState: this.state.viewData.formState,
      formUiState: this.state.viewData.formUiState,
      initializeFormState: this.formEvents.initializeFormState,
      onFormChange: this.formEvents.changeFormState,
      onFormUiChange: this.formEvents.changeFormUiState,
      onSubmit: this.formEvents.submitForm
    };

    // render form
    title = title + ": " + this.state.viewData.step.displayName;
    return ( <Doc title={title}><StepDownloadFormPage {...formProps}/></Doc> );
  },
  
  formEvents: {

    changeReporter(newReporterName) {
      this.actions.selectReporter(newReporterName);
    },

    initializeFormState(newFormState, newFormUiState) {
      setTimeout(() => { this.actions.updateFormState(newFormState); }, 0);
      if (newFormUiState !== undefined) {
        setTimeout(() => { this.actions.updateFormUiState(newFormUiState); }, 0);
      }
    },

    changeFormState(newFormState) {
      this.actions.updateFormState(newFormState);
    },

    changeFormUiState(newFormUiState) {
      this.actions.updateFormUiState(newFormUiState);
    },

    submitForm() {
      let { step, selectedReporter, formState } = this.state.viewData;
      this.actions.submitForm(step, selectedReporter, formState);
    }
  }
});

export default wrappable(StepDownloadFormController);
