import React from 'react';
import PropTypes from 'prop-types';

import HeadingRow from '../Ui/HeadingRow';
import DataRowList from '../Ui/DataRowList';

class DataTable extends React.PureComponent {
  constructor (props) {
    super(props);
    this.generateLayout = this.generateLayout.bind(this);
    this.shouldUseStickyHeader = this.shouldUseStickyHeader.bind(this);
  }

  shouldUseStickyHeader () {
    const { columns, options } = this.props;
    if (!options || !options.useStickyHeader) return false;
    const hasWidthProperty = ({ width }) => typeof width === 'string';
    if (columns.every(hasWidthProperty)) return true;
    console.error(`
      "useStickyHeader" enabled but not all columns have explicit widths (required).
      Use a CSS width (e.g. "250px" or "30%") as each column's .width property.
    `);
    return false;
  }

  generateLayout () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, options, columns, actions, uiState, eventHandlers };

    const { tableBodyMaxHeight } = options ? options : {};
    const tableBodyStyle = { maxHeight: tableBodyMaxHeight };

    return this.shouldUseStickyHeader() ? (
      <div className="DataTable-Sticky">
        <div className="DataTable-Header">
          <table cellSpacing={0} cellPadding={0}>
            <thead>
              <HeadingRow {...props} />
            </thead>
          </table>
        </div>
        <div className="DataTable-Body" style={tableBodyStyle}>
          <table cellSpacing={0} cellPadding={0}>
            <DataRowList {...props} />
          </table>
        </div>
      </div>
    ) : (
      <table cellSpacing="0" cellPadding="0">
        <thead>
          <HeadingRow {...props} />
        </thead>
        <DataRowList {...props} />
      </table>
    );
  }

  render () {
    const Layout = this.generateLayout;

    return (
      <div className="Mesa">
        <div className="DataTable">
          <Layout />
        </div>
      </div>
    );
  }
};

DataTable.propTypes = {
  rows: PropTypes.array,
  columns: PropTypes.array,
  options: PropTypes.object,
  actions: PropTypes.arrayOf(PropTypes.shape({
    element: PropTypes.oneOfType([ PropTypes.func, PropTypes.node, PropTypes.element ]),
    handler: PropTypes.func,
    callback: PropTypes.func
  })),
  uiState: PropTypes.object,
  eventHandlers: PropTypes.objectOf(PropTypes.func)
};

export default DataTable;
