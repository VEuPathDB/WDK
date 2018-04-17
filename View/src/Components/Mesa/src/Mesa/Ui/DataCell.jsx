import React from 'react';

import Templates from 'Mesa/Templates';

class DataCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    const { column, row, state, inline } = this.props;
    const { key } = column;

    if ('renderCell' in column) return column.renderCell(key, row[key], row);

    switch (column.type) {
      case 'number':
        return Templates.numberCell(column, row);
      case 'html':
        if (inline) return Templates.cell(column, row);
        return Templates.htmlCell(column, row);
      case 'text':
      default:
        return Templates.cell(column, row);
    };
  }

  render () {
    let { column, row, state, inline } = this.props;
    let { style, width } = column;
    let { options } = state;
    let content = this.renderContent();

    let whiteSpace = !inline ? {} : {
      textOverflow: 'ellipsis',
      overflow: 'hidden',
      maxWidth: options.inlineMaxWidth ? options.inlineMaxWidth : '20vw',
      maxHeight: options.inlineMaxHeight ? options.inlineMaxHeight : '2em',
    };

    width = (typeof width === 'number' ? width + 'px' : width);
    width = width ? { width } : {};

    const cellStyle = Object.assign({}, style, width, whiteSpace);

    return column.hidden ? null : (
      <td key={column.key} style={cellStyle}>
        {content}
      </td>
    );
  }
};

export default DataCell;
