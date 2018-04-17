import React from 'react';

class SelectionCounter extends React.Component {
  constructor (props) {
    super(props);
    this.noun = this.noun.bind(this);
    this.selectAllRows = this.selectAllRows.bind(this);
    this.deselectAllRows = this.deselectAllRows.bind(this);
  }

  noun (size) {
    const { selectedNoun, selectedPluralNoun } = this.props;
    size = (typeof size === 'number' ? size : size.length);
    return (!selectedNoun && !selectedPluralNoun)
      ? 'row' + (size === 1 ? '' : 's')
      : size === 1
        ? (selectedNoun || 'row')
        : (selectedPluralNoun || 'rows');
  }

  selectAllRows () {
    const { rows, selection, onRowSelect } = this.props;
    const unselectedRows = rows.map(row => !selection.includes(row));
    unselectedRows.forEach(row => onRowSelect(row));
  }

  deselectAllRows () {
    const { selection, onRowDeselect } = this.props;
    selection.forEach(row => onRowDeselect(row));
  }

  render () {
    const { rows, selection } = this.props;
    if (!selection || !selection.length) return null;
    const allSelected = rows.every(row => selection.includes(row));

    return (
      <div className="SelectionCounter">
        {/* {allSelected ? 'All ' : ''} */}
        <b>{selection.length} </b>
        {this.noun(selection)}
        {allSelected ? ' are' : ''} selected.
        <br />
        <a onClick={this.deselectAllRows}>Clear selection.</a>
      </div>
    );
  }
};

export default SelectionCounter;
