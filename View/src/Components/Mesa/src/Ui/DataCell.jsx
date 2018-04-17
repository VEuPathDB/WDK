import React from 'react';

import Templates from '../Templates';

class DataCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    const { row, column, rowIndex, columnIndex, inline } = this.props;
    const { key } = column;
    const value = typeof key === 'function' ? key(row) : row[key];
    const cellProps = { key, value, row, column, rowIndex, columnIndex };

    if ('renderCell' in column) {
      return column.renderCell(cellProps);
    }

    switch (column.type.toLowerCase()) {
      case 'link':
        return Templates.linkCell(cellProps);
      case 'number':
        return Templates.numberCell(cellProps);
      case 'html':
        return Templates[inline ? textCell : htmlCell](cellProps);
      case 'text':
      default:
        return Templates.textCell(cellProps);
    };
  }

  render () {
    let { column, row, inline } = this.props;
    let { style, width } = column;
    let content = this.renderContent();

    let whiteSpace = !inline ? {} : {
      textOverflow: 'ellipsis',
      overflow: 'hidden',
      maxWidth: options.inlineMaxWidth ? options.inlineMaxWidth : '20vw',
      maxHeight: options.inlineMaxHeight ? options.inlineMaxHeight : '2em',
    };

    width = (typeof width === 'number' ? width + 'px' : width);
    width = width ? { width, maxWidth: width, minWidth: width } : {};

    const cellStyle = Object.assign({}, style, width, whiteSpace);

    return column.hidden ? null : (
      <td key={column.key} style={cellStyle}>
        {content}
      </td>
    );
  }
};

export default DataCell;
