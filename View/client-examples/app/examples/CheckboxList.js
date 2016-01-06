import React from 'react';
import CheckboxList from 'wdk-client/components/CheckboxList';

let items = [
  { value: 'A', display: 'A' },
  { value: 'B', display: 'B' },
  { value: 'C', display: 'C' },
  { value: 'D', display: 'D' },
  { value: 'E', display: 'E' },
  { value: 'F', display: 'F' },
];

let selectedItems = items.filter(item => item.value === 'A' || item.value === 'D');

export class Example extends React.Component {

  constructor() {
    super(...arguments);
    this.state = {
      selectedItems: selectedItems.slice()
    }
  }

  toggleItem(event, item) {
    let selectedItems = event.target.checked
      ? [ ...this.state.selectedItems, item ]
      : this.state.selectedItems.filter(i => i !== item);
    this.setState({ selectedItems }, () => console.log(this.state));
  }

  selectAll() {
    this.setState({
      selectedItems: items.slice()
    }, () => console.log(this.state))
  }

  clearAll() {
    this.setState({
      selectedItems: []
    }, () => console.log(this.state));
  }

  render() {
    return (
      <div>
        <h2>Uncontrolled CheckboxList</h2>
        <CheckboxList
          name="example1"
          onChange={(e, item) => console.log(e.target.checked, item.value)}
          items={items}
          defaultSelectedItems={selectedItems}
        />

        <h2>Controlled CheckboxList</h2>
        <CheckboxList
          name="example2"
          onChange={(event, item) => this.toggleItem(event, item)}
          onSelectAll={() => this.selectAll()}
          onClearAll={() => this.clearAll()}
          items={items}
          selectedItems={this.state.selectedItems}
        />
      </div>
    );
  }
}
