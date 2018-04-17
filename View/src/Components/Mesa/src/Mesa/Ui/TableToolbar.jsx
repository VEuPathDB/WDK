import React from 'react';

import Icon from 'Mesa/Components/Icon';
import TableSearch from 'Mesa/Ui/TableSearch';
import ColumnEditor from 'Mesa/Ui/ColumnEditor';
import RowUtils from 'Mesa/Utils/RowUtils';

class TableToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { dispatch, state, filteredRows, children, currentPage, pages } = this.props;
    const { rows, columns, options } = state;

    const hiddenRowCount = rows.length - filteredRows.length;
    const columnsAreHideable = columns.some(column => column.hideable);

    const { paginate, rowsPerPage } = options;
    const [ first, last, total ] = RowUtils.getSpanByPage(filteredRows, currentPage, options);

    return (
      <div className="TableToolbar">
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
          {!filteredRows.length
          ? (<p><span className="faded">No results.</span></p>)
          : (<p>Rows {first} to {last} of {total}</p>)
          }
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
              Add/Remove Columns
            </button>
          </ColumnEditor>
        )}
      </div>
    );
  }
};

export default TableToolbar;
