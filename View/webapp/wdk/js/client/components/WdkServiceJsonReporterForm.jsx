import { getChangeHandler, wrappable } from '../utils/componentUtils';
import { getAttributeTree, getTableTree, getAttributeSelections } from '../utils/reporterUtils';
import CategoriesCheckboxTree from './CategoriesCheckboxTree';

let WdkServiceJsonReporterForm = props => {
  let { question, recordClass, summaryView, ontology, formState, formUiState, onFormChange, onFormUiChange, onSubmit } = props;
  let getUpdateHandler = fieldName => getChangeHandler(fieldName, onFormChange, formState);
  let getUiUpdateHandler = fieldName => getChangeHandler(fieldName, onFormUiChange, formUiState);
  return (
    <div>
      <CategoriesCheckboxTree
          // title and layout of the tree
          title="Choose Attributes"
          searchBoxPlaceholder="Search Attributes..."
          tree={getAttributeTree(ontology, recordClass, question)}

          // state of the tree
          selectedLeaves={formState.attributes}
          expandedBranches={formUiState.expandedAttributeNodes}
          searchText={formUiState.attributeSearchText}

          // change handlers for each state element controlled by the tree
          onChange={getUpdateHandler('attributes')}
          onUiChange={getUiUpdateHandler('expandedAttributeNodes')}
          onSearchTextChange={getUiUpdateHandler('attributeSearchText')}
      />

      <CategoriesCheckboxTree
          // title and layout of the tree
          title="Choose Tables"
          searchBoxPlaceholder="Search Tables..."
          tree={getTableTree(ontology, recordClass)}

          // state of the tree
          selectedLeaves={formState.tables}
          expandedBranches={formUiState.expandedTableNodes}
          searchText={formUiState.tableSearchText}

          // change handlers for each state element controlled by the tree
          onChange={getUpdateHandler('tables')}
          onUiChange={getUiUpdateHandler('expandedTableNodes')}
          onSearchTextChange={getUiUpdateHandler('tableSearchText')}
      />

      <div style={{width:'30em',textAlign:'center', margin:'0.6em 0'}}>
        <input type="button" value="Submit" onClick={onSubmit}/>
      </div>
    </div>
  );
};

WdkServiceJsonReporterForm.getInitialState = (downloadFormStoreState, userStoreState) => ({
  formState: {
    attributes: getAttributeSelections(
        userStoreState.preferences, downloadFormStoreState.question),
    tables: []
  },
  formUiState: {
    expandedAttributeNodes: null,
    attributeSearchText: "",
    expandedTableNodes: null,
    tableSearchText: ""
  }
});

export default WdkServiceJsonReporterForm;
