import React from 'react';

import DataCell from 'Mesa/Ui/DataCell';
import SelectionCell from 'Mesa/Ui/SelectionCell';

class DataRow extends React.PureComponent {
  constructor (props) {
    super(props);
    this.state = { expanded: false };
    this.expandRow = this.expandRow.bind(this);
    this.collapseRow = this.collapseRow.bind(this);
    this.toggleRow = this.toggleRow.bind(this);
    this.componentWillReceiveProps = this.componentWillReceiveProps.bind(this);
  }

  componentWillReceiveProps (newProps) {
    const { row } = newProps;
    if (row !== this.props.row) {
      this.collapseRow();
    }
  }

  expandRow () {
    if (!this.props.state.options.inline) return;
    this.setState({ expanded: true });
  }

  collapseRow () {
    if (!this.props.state.options.inline) return;
    this.setState({ expanded: false });
  }

  toggleRow () {
    const { row } = this.props;
    if (!this.props.state.options.inline) return;

    const { expanded } = this.state;
    this.setState({ expanded: !expanded });
  }

  render () {
    let { row, state, dispatch } = this.props;
    let { columns, options, actions } = state;

    let { inline } = options;
    let { expanded } = this.state;
    inline = (inline ? !expanded : inline);

    let rowStyle = !inline ? {} : { whiteSpace: 'nowrap' };
    let className = 'DataRow' + (inline ? ' DataRow-Inline' : '');

    return (
      <tr className={className} style={rowStyle} onClick={this.toggleRow}>
        {actions.length
          ? <SelectionCell row={row} state={state} dispatch={dispatch} />
          : null
        }
        {columns.map(column => (
          <DataCell
            column={column}
            row={row}
            state={state}
            inline={inline}
            key={column.key}
          />
        ))}
      </tr>
    )
  }
};

export default DataRow;
