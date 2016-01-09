// Import modules
import React from 'react';
import update from 'react-addons-update';
import Router from 'react-router';
import { wrappable } from '../utils/componentUtils';

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
  render() {
    var text = "Download Step " + this.props.params.stepId + " (passed from view: " + this.props.query.summaryView + ")";
    return ( <div>{text}</div> );
  }
});

// Export the React Component class we just created.
export default wrappable(StepDownloadFormController);
