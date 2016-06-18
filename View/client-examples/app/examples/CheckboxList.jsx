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
      value: [ 'A', 'D' ]
    }
  }

  toggleItem(value) {
    this.setState({ value }, () => console.log('value', this.state.value));
  }

  render() {
    return (
      <div>
        <h3>Uncontrolled CheckboxList</h3>
        <em>This behavior is no longer supported by CheckboxList. It will most
        likely be replaced by a decorator component.</em>
        <CheckboxList
          name="example1"
          onChange={console.log.bind(console, 'value')}
          items={items}
          defaultValue={['A', 'B']}
        />

        <h3>Controlled CheckboxList</h3>
        <CheckboxList
          name="example2"
          onChange={(value) => this.toggleItem(value)}
          items={items}
          value={this.state.value}
        />
      </div>
    );
  }
}
