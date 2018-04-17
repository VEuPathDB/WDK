import React from 'react';
import Icon from './Icon';
import Tooltip from './Tooltip';
import Events from '../Utils/Events';

class HelpTrigger extends React.Component {
  constructor (props) {
    super(props);
    this.state = { position: {} };
    this.updateOffset = this.updateOffset.bind(this);
    this.componentDidMount = this.componentDidMount.bind(this);
    this.componentWillUnmount = this.componentWillUnmount.bind(this);
  }

  componentDidMount () {
    this.updateOffset();
    this.listeners = {
      scroll: Events.add('scroll', this.updateOffset),
      resize: Events.add('resize', this.updateOffset),
      MesaScroll: Events.add('MesaScroll', this.updateOffset),
      MesaReflow: Events.add('MesaReflow', this.updateOffset)
    };
  }

  componentWillUnmount () {
    Object.values(this.listeners).forEach(listenerId => Events.remove(listenerId));
  }

  updateOffset () {
    const { element } = this;
    if (!element) return;
    const offset = Tooltip.getOffset(element);
    const { top, left, height } = offset;
    const position = { left, top: top + height };
    this.setState({ position });
  }

  render () {
    const { position } = this.state;
    const { children } = this.props;

    return (
      <Tooltip
        corner="top-left"
        position={position}
        className="Trigger HelpTrigger"
        content={children}>
        <div ref={(el) => this.element = el}>
          <Icon fa="question-circle" />
        </div>
      </Tooltip>
    );
  }
};

export default HelpTrigger;
