import React from 'react';
import PropTypes from 'prop-types';
import { wrappable, getValueOrDefault } from '../utils/componentUtils';
import Tooltip from './Tooltip';

const baseClassName = "wdk-RadioList";
const helpClassName = baseClassName + 'InfoIcon';

class RadioList extends React.Component {

  constructor(props) {
    super(props);
    this.onChange = this.onChange.bind(this);
  }

  onChange(event) {
    // only call change function passed in if value is indeed changing
    if (event.target.value !== this.props.value) {
      this.props.onChange(event.target.value);
    }
  }

  render() {
    let className = baseClassName + " " + getValueOrDefault(this.props, "className", "");
    return (
      <ul className={className}>
        {this.props.items.map(item => (
          <li key={item.value}>
            <label>
              <input type="radio"
                name={this.props.name}
                value={item.value}
                checked={item.value === this.props.value}
                onChange={this.onChange}/>
              {' ' + item.display}
              {item.description != null && 
                <Tooltip content={item.description}>
                  <i className={"fa fa-question-circle " + helpClassName}/>
                </Tooltip>
              }
            </label>
          </li>
        ))}
      </ul>
    );
  }

}

RadioList.propTypes = {

  /** Value to use for "name" attribute of radio form input elements **/
  name: PropTypes.string,

  /** Array of items to display in the list **/
  items: PropTypes.array.isRequired,

  /** Value of the radio input element that should be checked **/
  value: PropTypes.string,

  /**
   * Callback function that will be called when user changes selected value.
   * The new (string) value of the selected button will be passed to this
   * function.
   */
  onChange: PropTypes.func,

  /**
   * CSS class name that will be applied to the parent <li> element of this
   * radio list.
   */
  className: PropTypes.string
};

export default wrappable(RadioList)
