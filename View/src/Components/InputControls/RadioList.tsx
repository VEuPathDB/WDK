import React from 'react';
import PropTypes from 'prop-types';
import { wrappable, getValueOrDefault } from 'Utils/ComponentUtils';
import HelpIcon from 'Components/Icon/HelpIcon';

const baseClassName = "wdk-RadioList";

type Props = {
  name: string;
  items: Array<{
    name: string;
    display: string;
    value: string;
    description?: string;
  }>;
  value: string;
  onChange: (value: string) => void;
  className?: string;
}

class RadioList extends React.Component<Props> {

  static propTypes = {

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

  constructor(props: Props) {
    super(props);
    this.onChange = this.onChange.bind(this);
  }

  onChange(event: React.ChangeEvent<HTMLInputElement>) {
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
              {' ' + item.display + ' '}
              {item.description != null &&
                <HelpIcon tooltipPosition={{ my: 'center left', at: 'center right' }}>
                  {item.description}
                </HelpIcon>
              }
            </label>
          </li>
        ))}
      </ul>
    );
  }

}

export default wrappable(RadioList)
