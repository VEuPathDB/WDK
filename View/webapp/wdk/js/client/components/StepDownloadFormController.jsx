// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Doc from './Doc';
import Loading from './Loading';
import StepDownloadFormPage from './StepDownloadFormPage';

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

    // load actions for this view
    this.actions = this.props.actionCreators.StepDownloadFormViewActionCreator;
    this.userActions = this.props.actionCreators.UserActionCreator;

    // get the user store, subscribe and initialize if needed
    this.userStore = this.props.stores.UserStore;
    this.userStoreSubscription = this.userStore.addListener(this.loadUserState);

    // initialize user store if it isn't already
    let userState = this.userStore.getState();
    if (userState.user == null) {
      this.userActions.loadCurrentUser();
    }
    else {
      this.loadUserState();
    }

    // get view store and subscribe; must reinitialize with every new props
    this.store = this.props.stores.StepDownloadFormViewStore;
    this.storeSubscription = this.store.addListener(this.loadViewState);

    // initialize page data
    this.reloadPageData(this.props);

    // Bind methods of `this.formEvents` to `this`. When they are called by
    // child elements, any reference to `this` in the methods will refer to
    // this component.
    for (let key in this.formEvents) {
      this.formEvents[key] = this.formEvents[key].bind(this);
    }
  },

  componentWillReceiveProps(nextProps) {
    // reload step each time this page is displayed
    this.reloadPageData(nextProps);
  },

  // reloads step and associated question and recordClass
  reloadPageData(props) {
    this.actions.reloadData(props.params.stepId);
  },

  componentWillUnmount() {
    this.userStoreSubscription.remove();
    this.storeSubscription.remove();
  },
  
  render() {

    let title = "Download Step Result";

    if (this.state == null ||
        this.state.userData == null || this.state.userData.isLoading ||
        this.state.viewData == null || this.state.viewData.isLoading) {
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
      selectedReporter: this.state.viewData.selectedReporter,
      onReporterChange: this.formEvents.changeReporter,
      formState: this.state.viewData.formState,
      formUiState: this.state.viewData.formUiState,
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

// Export the React Component class we just created.
export default wrappable(StepDownloadFormController);
