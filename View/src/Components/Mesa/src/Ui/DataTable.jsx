import React from 'react';
import PropTypes from 'prop-types';

import HeadingRow from '../Ui/HeadingRow';
import DataRowList from '../Ui/DataRowList';
import { makeClassifier, combineWidths } from '../Utils/Utils';

const dataTableClass = makeClassifier('DataTable');

class DataTable extends React.Component {
  constructor (props) {
    super(props);
    this.shouldUseStickyHeader = this.shouldUseStickyHeader.bind(this);
    this.handleTableBodyScroll = this.handleTableBodyScroll.bind(this);
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

  handleTableBodyScroll (e) {
    const offset = this.bodyNode.scrollLeft;
    this.headerNode.scrollLeft = offset;
  }

  render () {
    const { rows, filteredRows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, filteredRows, options, columns, actions, uiState, eventHandlers };

    if (!this.shouldUseStickyHeader()) {
      return (
        <div className="MesaComponent">
          <div className={dataTableClass()}>
            <table cellSpacing="0" cellPadding="0">
              <thead>
                <HeadingRow {...props} />
              </thead>
              <DataRowList {...props} />
            </table>
          </div>
        </div>
      );
    };

    const { tableBodyMaxHeight } = options ? options : {};
    const cumulativeWidth = combineWidths(columns.map(col => col.width));
    const heightLayer = { maxHeight: tableBodyMaxHeight };
    const widthLayer = { minWidth: cumulativeWidth };

    return (
      <div className="MesaComponent">
        <div className={dataTableClass()}>
          <div className={dataTableClass('Sticky')}>

            <div
              ref={node => this.headerNode = node}
              className={dataTableClass('Header')}
            >
              <table cellSpacing={0} cellPadding={0}>
                <thead>
                  <HeadingRow {...props} />
                </thead>
              </table>
            </div>

            <div
              ref={node => this.bodyNode = node}
              style={heightLayer}
              className={dataTableClass('Body')}
              onScroll={this.handleTableBodyScroll}
            >
              <table cellSpacing={0} cellPadding={0}>
                <DataRowList {...props} />
              </table>
            </div>

          </div>
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
