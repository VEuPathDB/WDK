import ReactDOM from 'react-dom';
import React from 'react';

export default class AccordionButton extends React.Component {

  constructor(props) {
    super(props);
    this.state = {
      "id": props.id
    };
    // hard bind the handleExpansion functions to the Accordion Button object
    this.handleExpansion = this.handleExpansion.bind(this);
  }

  handleExpansion() {
    this.props.toggleExpansion(this.state.id);
  }

  render() {
    let handleExpansion = this.handleExpansion;
    let icon = this.props.icon;
    let id = this.props.id;

    return (
      <span className="wdk-CheckboxTree-accordionButton" onClick={handleExpansion}>
        {icon}
      </span>
    )
  }
}