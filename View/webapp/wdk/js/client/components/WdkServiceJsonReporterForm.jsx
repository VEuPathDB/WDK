import React from 'react';
import * as componentUtil from '../utils/componentUtils';
import * as reporterUtil from '../utils/reporterUtils';

let util = Object.assign({}, componentUtil, reporterUtil);

let WdkServiceJsonReporterForm = props => {
  let { question, recordClass, formState, onFormChange, onSubmit } = props;
  let getUpdateHandler = fieldName => util.getChangeHandler(fieldName, onFormChange, formState);
  return (
    <div>
      {util.getReporterCheckboxList("Choose Attributes", getUpdateHandler('attributes'),
        util.getAllAttributes(recordClass, question, util.isInReport), formState.attributes)}
      {util.getReporterCheckboxList("Choose Tables", getUpdateHandler('tables'),
        util.getAllTables(recordClass, util.isInReport), formState.tables)}
      <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
        <input type="button" value="Submit" onClick={onSubmit}/>
      </div>
    </div>
  );
};

WdkServiceJsonReporterForm.getInitialState = (downloadFormStoreState, userStoreState) => ({
  formState: {
    attributes: util.getAttributeSelections(
        userStoreState.preferences, downloadFormStoreState.question),
    tables: []
  },
  formUiState: null
});

export default util.wrappable(WdkServiceJsonReporterForm);
