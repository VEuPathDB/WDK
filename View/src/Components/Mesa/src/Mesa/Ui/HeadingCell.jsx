import React from 'react';

import Templates from 'Mesa/Templates';
import ColumnSorter from 'Mesa/Ui/ColumnSorter';
import ColumnFilter from 'Mesa/Ui/ColumnFilter';
import { toggleSortOrder, sortByColumn } from 'Mesa/State/Actions';

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    const { column } = this.props;
    if ('renderHeading' in column) return column.renderHeading(column);
    return Templates.heading(column);
  }

  handleSortClick () {
    const { column, state, dispatch } = this.props;
    const { sort } = state.ui;
    const currentlySorting = sort.byColumn === column;
    dispatch(currentlySorting ? toggleSortOrder() : sortByColumn(column));
  }

  defuseSortClick ({ children }) {
    const style = { display: 'inline-block' };
    return (
      <div onClick={(e) => e.stopPropagation()} style={style}>
        {children}
      </div>
    );
  }

  render () {
    const { column, state, dispatch } = this.props;
    const content = this.renderContent();
    const DefuseSortClick = this.defuseSortClick;
    const { headingStyle } = column;

    return column.hidden ? null : (
      <th
        key={column.key}
        ref={el => this.element = el}
        style={headingStyle}
        onClick={e => column.sortable ? this.handleSortClick() : null}
      >
        {column.sortable && (
          <ColumnSorter
            column={column}
            state={state}
          />
        )}
        {content}
        {column.filterable && (
          <DefuseSortClick>
            <ColumnFilter
              column={column}
              state={state}
              dispatch={dispatch}
            />
          </DefuseSortClick>
        )}
      </th>
    )
  }
};

export default HeadingCell;
