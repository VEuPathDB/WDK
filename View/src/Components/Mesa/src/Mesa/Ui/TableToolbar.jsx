import React from 'react';

import Icon from 'Mesa/Components/Icon';
import TableSearch from 'Mesa/Ui/TableSearch';
import ColumnEditor from 'Mesa/Ui/ColumnEditor';
import RowUtils from 'Mesa/Utils/RowUtils';
import RowCounter from 'Mesa/Ui/RowCounter';

class TableToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { dispatch, state, filteredRows, children } = this.props;
    const { rows, columns, options, ui } = state;
    const { pagination } = ui;

    const hiddenRowCount = rows.length - filteredRows.length;
    const columnsAreHideable = columns.some(column => column.hideable);

    const [ first, last, total ] = [1,2,3];

    return (
      <div className="Toolbar TableToolbar">
        {options.title && (
          <h1 className="TableToolbar-Title">{options.title}</h1>
        )}
        {options.search && (
          <TableSearch
            state={state}
            dispatch={dispatch}
          />
        )}
        <div className="TableToolbar-Info">
          <RowCounter state={state} filteredRows={filteredRows} />
        </div>
        {children && (
          <div className="TableToolbar-Children">
            {children}
          </div>
        )}
        {options.editableColumns && columnsAreHideable && (
          <ColumnEditor state={state} dispatch={dispatch}>
            <button>
              <Icon fa={'columns'} />
              <span>Add/Remove Columns</span>
            </button>
          </ColumnEditor>
        )}
      </div>
    );
  }
};

export default TableToolbar;
