import React from 'react';
import PropTypes from 'prop-types';

import HeadingRow from '../Ui/HeadingRow';
import DataRowList from '../Ui/DataRowList';
import { makeClassifier, combineWidths } from '../Utils/Utils';

const dataTableClass = makeClassifier('DataTable');

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
    const { rows, filteredRows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, filteredRows, options, columns, actions, uiState, eventHandlers };

    const { tableBodyMaxHeight } = options ? options : {};
    const tableBodyStyle = { maxHeight: tableBodyMaxHeight };
    const useStickyLayout = this.shouldUseStickyHeader();
    const cumulativeWidth = combineWidths(columns.map(col => col.width));
    const widthLayer = !useStickyLayout
      ? null : {
        minWidth: cumulativeWidth,
        maxWidth: cumulativeWidth,
        width: cumulativeWidth
      };

    return useStickyLayout ? (
      <div className={dataTableClass('Sticky')} style={widthLayer}>
        <div className={dataTableClass('Header')} style={widthLayer}>
          <table cellSpacing={0} cellPadding={0} style={widthLayer}>
            <thead>
              <HeadingRow {...props} />
            </thead>
          </table>
        </div>
        <div className={dataTableClass('Body')} style={Object.assign(tableBodyStyle, widthLayer)}>
          <table cellSpacing={0} cellPadding={0} style={widthLayer}>
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
      <div className="MesaComponent">
        <div className={dataTableClass()}>
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
