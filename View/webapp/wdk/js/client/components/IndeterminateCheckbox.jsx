import ReactDOM from 'react-dom';
import React from 'react';

/**
 * React Component that provides a 3-state checkbox
 */
export default class IndeterminateCheckbox extends React.Component {

  constructor(props) {
    super(props);

    // hard bind the handleChange functions to the IndeterminateCheckbox object
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    this.setIndeterminate(this.props.indeterminate);
  }

  componentDidUpdate(previousProps) {
    this.setIndeterminate(this.props.indeterminate);
  }

  /**
   * Sets the checkbox to the indeterminate state based on the the provided property.
   * This can only be set via JS.
   * @param indeterminate
   */
  setIndeterminate(indeterminate) {
    const node = ReactDOM.findDOMNode(this);
    node.indeterminate = indeterminate;
  }

  handleChange(e) {
    let selected = e.target.checked;
    this.props.toggleCheckbox(this.props.node, selected);
  }

  render() {
    return (
        <input name={this.props.name} value={this.props.value}
               checked={this.props.checked ? "checked" : ""}
               type="checkbox"
               onChange={this.handleChange} />
    )
  }
}