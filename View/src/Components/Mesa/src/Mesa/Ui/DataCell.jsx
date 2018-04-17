import React from 'react';

import Templates from 'Mesa/Templates';

class DataCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    const { column, row } = this.props;
    const { key } = column;

    if ('renderCell' in column) return column.renderCell(key, row[key], row);
    switch (column.type) {
      case 'html':
        return Templates.htmlCell(column, row);
      case 'text':
      default:
        return Templates.cell(column, row);
    };
  }

  render () {
    let { column, row } = this.props;
    let { style, width } = column;
    let content = this.renderContent();
    style = style || {};
    width = (typeof width === 'number')
      ? width + 'px'
      : (typeof width === 'string')
        ? width
        : null;
    const cellStyle = Object.assign({}, style, (width ? { width } : {}));

    return column.hidden ? null : (
      <td key={column.key} style={cellStyle}>
        {content}
      </td>
    );
  }
};

export default DataCell;
