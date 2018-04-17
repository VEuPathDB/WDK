import React from 'react';

import Store from 'Mesa/State/Store';
import Templates from 'Mesa/Templates';

class Row extends React.Component {
  constructor (props) {
    super(props);
    this.getRenderer = this.getRenderer.bind(this);
    this.makeCell = this.makeCell.bind(this);
  }

  getRenderer (column, row) {
    const { key } = column;
    if (!key) return null;

    if ('renderCell' in column) return column.renderCell(key, row[key], row);
    switch (column.type) {
      case 'html':
        return Templates.htmlCell(column, row);
      case 'text':
      default:
        return Templates.cell(column, row);
    };
  }

  makeCell (column, row) {
    let { style, width } = column;
    let { columnWidths } = Store.getState();
    if (column.key in columnWidths) width = columnWidths[column.key] + 'px'
    let content = this.getRenderer(column, row);
    let _style = Object.assign({}, (style ? style : {}), (width ? { width } : {}));
    let isHidden = Store.getState().hiddenColumns.includes(column);

    return isHidden ? null : (
      <td key={column.key} style={_style}>
        {content}
      </td>
    );
  }

  render () {
    const { row, columns } = this.props;
    const cellList = columns.map((col, idx) => this.makeCell(col, row));

    return (
      <tr className="Row">
        {cellList}
      </tr>
    );
  }
};

export default Row;
