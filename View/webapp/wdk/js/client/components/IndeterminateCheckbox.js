import ReactDOM from 'react-dom';
import React from 'react';

export default class IndeterminateCheckbox extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      "id": props.id,
      "indeterminate": props.indeterminate,
      "checked": props.checked
    };
    // hard bind the handleChange functions to the IndeterminateCheckbox object
    this.handleChange = this.handleChange.bind(this);
  }

  componentDidMount() {
    if(this.props.indeterminate === true) {
      this._setIndeterminate(true);
    }
  }

  componentDidUpdate(previousProps) {
    this._setIndeterminate(this.props.indeterminate);
  }

  _setIndeterminate(indeterminate) {
    const node = ReactDOM.findDOMNode(this);
    node.indeterminate = indeterminate;
  }

  handleChange() {
    this.props.toggleCheckbox(this.state.id);
  }

  render() {
    const { indeterminate, type, ...props } = this.props;
    let handleChange = this.handleChange;
    let icon = this.props.icon;
    let id = this.props.id;
    return (
        <input type="checkbox" onChange={handleChange} {...props} />
    )
  }
}