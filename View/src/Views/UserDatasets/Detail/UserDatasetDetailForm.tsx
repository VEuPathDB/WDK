import React, { Component, FormEvent } from 'react';
import { UserDataset } from 'Utils/WdkModel';

type Details = {
  name?: string;
  summary?: string;
  description?: string;
};

type Props = {
  details: Details;
  onSubmit: (details: Details) => void;
}

type State = Details;

const makeClassName = (element?: string, modifier?: string) =>
  'wdk-UserDatasetDetailForm' +
  (element ? `${element}` : ``) +
  (modifier ? `__${modifier}` : ``);

class UserDatasetDetailForm extends Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = props.details;
    this.handleSubmit = this.handleSubmit.bind(this);
    this.handleNameChange = this.handleNameChange.bind(this);
    this.handleSummaryChange = this.handleSummaryChange.bind(this);
    this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
  }

  handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    this.props.onSubmit(this.state);
  }

  handleNameChange(event: FormEvent<HTMLInputElement>) {
    const input = event.target as HTMLInputElement;
    this.setState({ name: input.value });
  }

  handleSummaryChange(event: FormEvent<HTMLInputElement>) {
    const input = event.target as HTMLInputElement;
    this.setState({ summary: input.value });
  }

  handleDescriptionChange(event: FormEvent<HTMLTextAreaElement>) {
    const textarea = event.target as HTMLInputElement;
    this.setState({ description: textarea.value });
  }

  render() {
    const { name, summary, description } = this.state;
    return (
      <div className={makeClassName('Container')}>
        <form onSubmit={this.handleSubmit}>
          <div className={makeClassName('Group')}>
            <label className={makeClassName('Label')} htmlFor="udsname">Name</label>
            <input
              id="udsname"
              className={makeClassName('Input')}
              type="text"
              value={name}
              onChange={this.handleNameChange}
            />
          </div>

          <div className={makeClassName('Group')}>
            <label className={makeClassName('Label')} htmlFor="udssummary">Summary</label>
            <input
              id="udssummary"
              className={makeClassName('Input')}
              type="text"
              value={summary}
              onChange={this.handleSummaryChange}
            />
          </div>

          <div className={makeClassName('Group')}>
            <label className={makeClassName('Label')} htmlFor="udsdescription">Description</label>
            <textarea
              id="udsdescription"
              className={makeClassName('Input')}
              rows={10}
              value={description}
              onChange={this.handleDescriptionChange}
            />
          </div>

          <div className={makeClassName('Group')}>
            <button type="submit">Update details</button>
          </div>
        </form>
      </div>
    );
  }

}

export default UserDatasetDetailForm;
