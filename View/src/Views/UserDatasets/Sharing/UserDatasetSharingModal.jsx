import React from 'react';

import './UserDatasetSharingModal.scss';
import Icon from 'Components/Icon/IconAlt';
import Modal from 'Components/Overlays/Modal';
import TextBox from 'Components/InputControls/TextBox';
import { wrappable } from 'Utils/ComponentUtils';

const isValidEmail = (email) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);

class UserDatasetSharingModal extends React.Component {
  constructor (props) {
    super(props);
    this.state = {
      recipients: [],
      recipientInput: null
    };
    this.renderShareItem = this.renderShareItem.bind(this);
    this.renderShareList = this.renderShareList.bind(this);
    this.renderDatasetList = this.renderDatasetList.bind(this);
    this.renderDatasetItem = this.renderDatasetItem.bind(this);

    this.handleTextChange = this.handleTextChange.bind(this);
    this.handleRecipientAdd = this.handleRecipientAdd.bind(this);
  }

  handleTextChange (recipientInput = null) {
    this.setState({ recipientInput });
  }

  handleRecipientAdd () {
    const { recipientInput, recipients } = this.state;
    if (!isValidEmail(recipientInput))
      return alert('Please enter a valid email to share with.');
    this.setState({
      recipientInput: null,
      recipients: [ ...recipients, recipientInput ]
    });
  }

  renderEmptyState () {
    return (
      <div>
        Not Shared Yet
      </div>
    )
  }

  renderShareItem (share) {
    const { user, time, userDisplayName } = share;
    return (
      <div key={user}>
        Shared to {userDisplayName} at {time}
      </div>
    );
  }

  renderDatasetItem (dataset) {
    const { shares, id, meta } = dataset;
    const EmptyState = this.renderEmptyState;
    const { name, summary } = meta;
    return (
      <div key={id}>
        <h3>{name}</h3>
        <small>Shares:</small>
        {Array.isArray(shares) && shares.length
          ? <ShareList shares={shares}/>
          : <EmptyState/>
        }
      </div>
    )
  }

  renderRecipientItem (recipient, index) {
    return (
      <div key={index}>
        {recipient}
      </div>
    );
  }

  renderRecipientList ({ recipients }) {
    return !Array.isArray(recipients) || !recipients.length
      ? null
      : recipients.map(this.renderRecipientItem);
  }

  renderShareList ({ shares }) {
    return !Array.isArray(shares) || !shares.length
      ? null
      : shares.map(this.renderShareItem);
  }

  renderDatasetList ({ datasets }) {
    return !Array.isArray(datasets) || !datasets.length
      ? null
      : datasets.map(this.renderDatasetItem)
  }

  render () {
    const { recipientInput, recipients } = this.state;
    const { datasets, onClose } = this.props;
    const RecipientList = this.renderRecipientList;
    const DatasetList = this.renderDatasetList;
    return (
      <Modal className="UserDataset-SharingModal">
        <Icon
          fa="times"
          className="SharingModal-Close"
          onClick={() => typeof onClose === 'function' ? onClose() : null}
        />
        Share with:
        <TextBox
          onChange={this.handleTextChange}
          value={recipientInput ? recipientInput : ''}
        />
        <button onClick={this.handleRecipientAdd}>
          Add
        </button>
        <RecipientList recipients={recipients}/>
        <hr />
        <DatasetList datasets={datasets}/>
      </Modal>
    )
  }
};

export default wrappable(UserDatasetSharingModal);
