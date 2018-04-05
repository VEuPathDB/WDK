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
    this.renderRecipientItem = this.renderRecipientItem.bind(this);
    this.renderRecipientList = this.renderRecipientList.bind(this);

    this.handleTextChange = this.handleTextChange.bind(this);
    this.handleRecipientAdd = this.handleRecipientAdd.bind(this);
    this.isMyDataset = this.isMyDataset.bind(this);
    this.disqualifyRecipient = this.disqualifyRecipient.bind(this);
  }

  isMyDataset (dataset) {
    const { user } = this.props;
    return dataset && dataset.ownerUserId && dataset.ownerUserId === user.id;
  }

  handleTextChange (recipientInput = null) {
    this.setState({ recipientInput });
  }

  verifyRecipient (recipientEmail) {
    if (typeof recipientEmail !== 'string' || !recipientEmail.length)
      throw new TypeError(`verifyRecipient: bad email received (${recipientEmail})`);

    fetch('/fungidb.austinjb/service/user-id-query', {
      method: 'POST',
      body: JSON.stringify({ emails: [ recipientEmail ]}),
      headers: new Headers({ 'Content-Type': 'application/json' }),
      credentials: 'include'
    })
      .then(response => response.json())
      .then(({ results }) => {
        if (!Array.isArray(results))
          throw new TypeError(`verifyRecipient: received malformed repsonse from service. [${results}]`);

        const recipientResult = results.find(result => Object.keys(result).includes(recipientEmail));
        if (!results.length || !recipientResult) this.disqualifyRecipient(recipientEmail);
        const uid = recipientResult[recipientEmail];
        this.acceptRecipient(recipientEmail, uid);
      })
      .catch(err => {
        console.error('checked if email ' + recipientEmail + ' exists.', err);
      });
  }

  acceptRecipient (recipientEmail, id) {
    const { recipients } = this.state;
    const acceptedRecipient = {
      id,
      verified: true,
      email: recipientEmail
    };
    this.setState({
      recipients: recipients.map(recipient => {
        return recipient.email === recipientEmail
          ? acceptedRecipient
          : recipient
      })
    });
  }

  disqualifyRecipient (recipientEmail) {
    const { recipients } = this.state;
    const disqualifiedRecipient = {
      email: recipientEmail,
      verified: false,
      id: null
    };
    this.setState({
      recipients: recipients.map(recipient => {
        return recipient.email === recipientEmail
          ? disqualifiedRecipient
          : recipient
      })
    });
  }

  handleRecipientAdd () {
    const { recipientInput, recipients } = this.state;
    if (!isValidEmail(recipientInput))
      return alert('Please enter a valid email to share with.');
    this.setState({
      recipientInput: null,
      recipients: [
        ...recipients,
        {
          email: recipientInput,
          verified: null,
          id: null
        }
      ]
    }, () => this.verifyRecipient(recipientInput));
  }

  renderEmptyState () {
    return (
      <i className="faded">This dataset hasn't been shared yet.</i>
    );
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
    const isOwner = this.isMyDataset(dataset);
    return (
      <div key={id} className="UserDatasetSharing-Dataset">
        <div className="UserDatasetSharing-Dataset-Icon">
          <Icon fa={isOwner ? 'table' : 'times-circle'} />
        </div>
        <div className="UserDatasetSharing-Dataset-Details">
          <h3>{name}</h3>
          {!isOwner
            ? <i className="faded">This dataset has been shared with you. Only the owner can share it.</i>
            : Array.isArray(shares) && shares.length
              ? <ShareList shares={shares}/>
              : <EmptyState/>
          }
        </div>
      </div>
    )
  }

  renderRecipientItem (recipient, index) {
    const { email, verified, id } = recipient;
    const invalid = verified === false;
    return (
      <div key={index} className={'UserDatasetSharing-Recipient' + (invalid ? ' invalid' : '')}>
        <div className="UserDatasetSharing-Recipient-Icon">
          <Icon fa={verified === null ? 'circle-o-notch fa-spin' : verified ? 'user-circle' : 'user-times'}/>
        </div>
        <div className="UserDatasetSharing-Recipient-Details">
          <h3>{email}</h3>
          {!id ? null : <b>{id}</b>}
          {!invalid ? null : <span className="warning">
            This email is not associated with a EuPathDB account.<br/>
            <b>{email}</b> will not receive this dataset.</span>}
        </div>
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
    const validRecipients = recipients.filter(({ verified }) => verified);
    const RecipientList = this.renderRecipientList;
    const DatasetList = this.renderDatasetList;
    return (
      <Modal className="UserDataset-SharingModal">
        <Icon
          fa="window-close"
          className="SharingModal-Close"
          onClick={() => typeof onClose === 'function' ? onClose() : null}
        />

        <h2 className="UserDatasetSharing-SectionName">Share These Datasets:</h2>
        <DatasetList datasets={datasets}/>

        <h2 className="UserDatasetSharing-SectionName">With The Following Collaborators:</h2>
        <fieldset>
          <TextBox
            placeholder="name@example.com"
            onChange={this.handleTextChange}
            value={recipientInput ? recipientInput : ''}
          />
          <button className="btn slim btn-slim" title="Share with this email address" onClick={this.handleRecipientAdd}>
            <Icon fa="plus-square"/>
          </button>
        </fieldset>
        <RecipientList recipients={recipients}/>

        <div className="UserDatasetSharing-Buttons">
          <button className="btn btn-info" disabled={!validRecipients.length || !datasets.length}>
            Share Datasets with {validRecipients.length} user{validRecipients.length === 1 ? '' : 's'} <Icon fa="share right-side"/>
          </button>
        </div>

      </Modal>
    )
  }
};

export default wrappable(UserDatasetSharingModal);
