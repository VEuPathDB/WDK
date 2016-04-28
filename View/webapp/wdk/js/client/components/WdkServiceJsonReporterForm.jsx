import { getChangeHandler, wrappable } from '../utils/componentUtils';
import { getAttributeTree, getTableTree, getAttributeSelections } from '../utils/reporterUtils';
import { getAllLeafIds } from '../utils/CategoryUtils';
import CategoriesCheckboxTree from './CategoriesCheckboxTree';
import ReporterSortMessage from './ReporterSortMessage';

let WdkServiceJsonReporterForm = props => {
  let { scope, question, recordClass, summaryView, ontology, formState, formUiState, onFormChange, onFormUiChange, onSubmit } = props;
  let getUpdateHandler = fieldName => getChangeHandler(fieldName, onFormChange, formState);
  let getUiUpdateHandler = fieldName => getChangeHandler(fieldName, onFormUiChange, formUiState);
  return (
    <div>
      <ReporterSortMessage scope={scope}/>
      <CategoriesCheckboxTree
          // title and layout of the tree
          title="Choose Attributes"
          searchBoxPlaceholder="Search Attributes..."
          tree={getAttributeTree(ontology, recordClass, question)}

          // state of the tree
          selectedLeaves={formState.attributes}
          expandedBranches={formUiState.expandedAttributeNodes}
          searchTerm={formUiState.attributeSearchText}

          // change handlers for each state element controlled by the tree
          onChange={getUpdateHandler('attributes')}
          onUiChange={getUiUpdateHandler('expandedAttributeNodes')}
          onSearchTermChange={getUiUpdateHandler('attributeSearchText')}
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

WdkServiceJsonReporterForm.getInitialState = (downloadFormStoreState, userStoreState) => {
  let { scope, question, recordClass, ontology } = downloadFormStoreState;
  // select all attribs and tables for record page, else column user prefs and no tables
  let attribs = (scope === 'results' ?
      getAttributeSelections(userStoreState.preferences, question) :
      getAllLeafIds(getAttributeTree(ontology, recordClass, question)));
  let tables = (scope === 'results' ? [] :
      getAllLeafIds(getTableTree(ontology, recordClass)));
  return {
    formState: {
      attributes: attribs,
      tables:tables
    },
    formUiState: {
      expandedAttributeNodes: null,
      attributeSearchText: "",
      expandedTableNodes: null,
      tableSearchText: ""
    }
  };
}

export default WdkServiceJsonReporterForm;
