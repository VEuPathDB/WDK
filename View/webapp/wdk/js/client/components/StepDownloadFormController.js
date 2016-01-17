// Import modules
import React from 'react';
import { wrappable } from '../utils/componentUtils';
import Loading from './Loading';
import StepDownloadForm from './StepDownloadForm';

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

  componentWillMount() {

    // load actions for this view
    this.actions = this.props.actionCreators.StepDownloadFormViewActionCreator;
    this.userActions = this.props.actionCreators.UserActionCreator;

    // get the user store, subscribe and initialize if needed
    this.userStore = this.props.stores.UserStore;
    this.userStoreSubscription = this.userStore.addListener(() => {
      this.setState(Object.assign({}, this.state, { userData: this.userStore.getState() }));
    });
    // initialize user store if it isn't already
    let userState = this.userStore.getState();
    if (userState.user == null) {
      this.userActions.loadCurrentUser();
    }
    else {
      this.setState(Object.assign({}, this.state, { userData: this.userStore.getState() }));
    }

    // get view store and subscribe; must reinitialize with every new props
    this.store = this.props.stores.StepDownloadFormViewStore;
    this.storeSubscription = this.store.addListener(() => {
      this.setState(Object.assign({}, this.state, { viewData: this.store.getState() }));
    });

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

    if (this.state == null ||
        this.state.userData == null || this.state.userData.isLoading ||
        this.state.viewData == null || this.state.viewData.isLoading) {
      return ( <Loading/> );
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
      onFormChange: this.formEvents.changeForm,
    };

    // render form
    return ( <StepDownloadForm {...formProps}/> );
  },
  
  formEvents: {

    changeReporter(newReporterName) {
      this.actions.selectReporter(newReporterName);
    },

    changeForm(newFormState) {
      this.actions.updateFormState(newFormState);
    }
  }
});

// Export the React Component class we just created.
export default wrappable(StepDownloadFormController);
