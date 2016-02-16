import React from 'react';
import * as componentUtil from '../utils/componentUtils';
import * as reporterUtil from '../utils/reporterUtils';

let util = Object.assign({}, componentUtil, reporterUtil);

let WdkServiceJsonReporterForm = React.createClass({

  componentDidMount() {
    let { formState, preferences, question, initializeFormState } = this.props;
    initializeFormState(this.discoverFormState(formState, preferences, question));
  },

  discoverFormState(formState, preferences, question) {
    let currentAttributes = (formState == null ? undefined : formState.attributes);
    let currentTables = (formState == null ? undefined : formState.tables);
    return {
      attributes: util.getAttributeSelections(currentAttributes, preferences, question),
      tables: util.getTableSelections(currentTables)
    };
  },

  // returns a handler function that will update the form state 
  getUpdateHandler(fieldName) {
    return util.getChangeHandler(fieldName, this.props.onFormChange, this.props.formState);
  },

  render() {
    let { question, recordClass, preferences, formState, onSubmit } = this.props;
    let realFormState = this.discoverFormState(formState, preferences, question);
    return (
      <div>
        {util.getReporterCheckboxList("Choose Attributes", this.getUpdateHandler('attributes'),
          util.getAllAttributes(recordClass, question, util.isInReport), realFormState.attributes)}
        {util.getReporterCheckboxList("Choose Tables", this.getUpdateHandler('tables'),
          util.getAllTables(recordClass, util.isInReport), realFormState.tables)}
        <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
          <input type="button" value="Submit" onClick={onSubmit}/>
        </div>
      </div>
    );
  }
});

export default util.wrappable(WdkServiceJsonReporterForm);
