import React, { Component, MouseEvent,  StatelessComponent } from 'react';
import { wrappable } from '../utils/componentUtils';
import { UserDataset , UserDatasetMeta } from '../utils/WdkModel';
import { bytesToHuman } from '../utils/Converters';
import Dialog from './Dialog';
import UserDatasetDetailForm from './UserDatasetDetailForm';
import UserDatasetSharing from './UserDatasetSharing';

type Props = {
  userDataset: UserDataset;
  isOwner: boolean;
  updateUserDatasetItem: (id: number, details: UserDatasetMeta) => void;
};

type State = {
  edit?: 'details' | 'shares';
};

const todo = (e: any) => {
  e.preventDefault();
  alert('TODO');
}

const makeClassName = (element?: string, modifier?: string) =>
  'wdk-UserDataset' +
  (element ? `${element}` : ``) +
  (modifier ? `__${modifier}` : ``);

const displayDate = (time: number) =>
  new Date(time).toLocaleDateString();

const tooltipDate = (time: number) =>
  new Date(time).toString();

const OverviewItem: StatelessComponent<{prompt: string}> = props =>
  <div className={makeClassName('OverviewItem')}>
    <div className={makeClassName('OverviewItemPrompt')}>{props.prompt}:&nbsp;</div>
    <div className={makeClassName('OverviewItemText')}>{props.children}</div>
  </div>

const SectionItem: StatelessComponent<{heading: string}> = props =>
  <div className={makeClassName('Section')}>
    <h2 className={makeClassName('SectionHeading')}>{props.heading}</h2>
    {props.children}
  </div>

class UserDatasetItem extends Component<Props, State> {

  constructor(props: Props) {
    super(props);
    this.state = { edit: undefined };
    this.handleEditDetailsClick = this.handleEditDetailsClick.bind(this);
    this.handleEditSharesClick = this.handleEditSharesClick.bind(this);
    this.handleDialogClose = this.handleDialogClose.bind(this);
    this.handleDetailFormSubmit = this.handleDetailFormSubmit.bind(this);
  }

  handleEditDetailsClick(event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    this.setState({ edit: 'details' });
  }

  handleEditSharesClick(event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    this.setState({ edit: 'shares' });
  }

  handleDialogClose() {
    this.setState({ edit: undefined });
  }

  handleDetailFormSubmit(meta: UserDatasetMeta) {
    this.props.updateUserDatasetItem(this.props.userDataset.id, meta);
    this.setState({ edit: undefined });
  }

  render() {
    let { userDataset, isOwner } = this.props;
    let { edit } = this.state;
    return (
      <div className={makeClassName()}>

        {isOwner ? (
          <div className={makeClassName('Actions')}>
            <div className={makeClassName('ActionItem')}>
              <a href="#" onClick={this.handleEditDetailsClick}>
                Edit details <i className="fa fa-pencil"/>
              </a>
            </div>
            <div className={makeClassName('ActionItem')}>
              <a href="#" onClick={this.handleEditSharesClick}>
                Manage sharing ({userDataset.sharedWith.length}) <i className="fa fa-share-alt"/>
              </a>
            </div>
          </div>
        ) : (
          <div className={makeClassName('SharedNote')}>
            shared from {userDataset.owner} <i className="fa fa-share-alt"/>
          </div>
        )}

        <Dialog
          title="Edit Details"
          open={edit === 'details'}
          modal={true}
          onClose={this.handleDialogClose}
          width={700}
          className={makeClassName('EditDialog')}
        >
          <UserDatasetDetailForm details={userDataset.meta} onSubmit={this.handleDetailFormSubmit}/>
        </Dialog>

        <Dialog
          title="Manage Sharing"
          open={edit === 'shares'}
          modal={true}
          onClose={this.handleDialogClose}
          width={500}
          className={makeClassName('EditDialog')}
        >
          <UserDatasetSharing shares={userDataset.sharedWith}/>
        </Dialog>

        <h1 className={makeClassName('Heading')}>
          User Data Set: {userDataset.meta.name}
        </h1>

        <div className={makeClassName('Overview')}>
          <OverviewItem prompt="Summary">
            {userDataset.meta.summary}
          </OverviewItem>

          <OverviewItem prompt="Identifier">
            {userDataset.id}
          </OverviewItem>

          <OverviewItem prompt="Version">
            {userDataset.type.name} {userDataset.type.version}
          </OverviewItem>

          <OverviewItem prompt="Dependencies">
            {userDataset.dependencies.map(d => `${d.resourceDisplayName} ${d.resourceVersion}`).join(', ')}
          </OverviewItem>

          <OverviewItem prompt="Created">
            <span title={tooltipDate(userDataset.created)}>{displayDate(userDataset.created)}</span>
          </OverviewItem>

          <OverviewItem prompt="Modified">
            <span title={tooltipDate(userDataset.modified)}>{displayDate(userDataset.modified)}</span>
          </OverviewItem>

          <OverviewItem prompt="Size">
            <span title={`${userDataset.percentQuotaUsed}% of quota`}>{bytesToHuman(userDataset.size)}</span>
          </OverviewItem>
        </div>

        <SectionItem heading="Description">
          <div>{userDataset.meta.description}</div>
        </SectionItem>

        <SectionItem heading="Data Files">
          <ul>
            {userDataset.datafiles.map(datafile =>
              <li key={datafile.name}>
                <a href="#dl" title="Download file" onClick={todo}>
                  {datafile.name} ({bytesToHuman(datafile.size)}) <i className="fa fa-download"/>
                </a>
              </li>
            )}
          </ul>
        </SectionItem>

        <SectionItem heading="Projects">
          <ul>
            {userDataset.projects.map(project =>
              <li key={project}>{project}</li>
            )}
          </ul>
        </SectionItem>

      </div>
    );
  }
}

export default wrappable(UserDatasetItem);
