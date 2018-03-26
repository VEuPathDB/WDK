import React, { Component, MouseEvent,  StatelessComponent } from 'react';
import Link from 'Components/Link/Link';
import url from 'url';
import { wrappable } from 'Utils/ComponentUtils';
import { Question, UserDataset , UserDatasetMeta } from 'Utils/WdkModel';
import { bytesToHuman } from 'Utils/Converters';
import Dialog from 'Components/Overlays/Dialog';

import NotFound from 'Views/NotFound/NotFound';
import UserDatasetSharing from 'Views/UserDatasets/UserDatasetSharing';
import UserDatasetDetailForm from 'Views/UserDatasets/Detail/UserDatasetDetailForm';

type Props = {
  userDataset: UserDataset;
  isOwner: boolean;
  updateUserDatasetDetail: (userDataset: UserDataset, details: UserDatasetMeta) => void;
  questionMap: { [key: string]: Question };
  getQuestionUrl: (question: Question) => string;
  userDatasetUpdating: boolean;
  updateError?: Error;
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

const normalizeBaseUrl = (baseUrl: string) =>
  /\/$/.test(baseUrl) ? baseUrl : baseUrl + '/';

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

class UserDatasetDetail extends Component<Props, State> {

  constructor (props: Props) {
    super(props);
    this.state = { edit: undefined };
    this.handleEditDetailsClick = this.handleEditDetailsClick.bind(this);
    this.handleEditSharesClick = this.handleEditSharesClick.bind(this);
    this.handleDialogClose = this.handleDialogClose.bind(this);
    this.handleDetailFormSubmit = this.handleDetailFormSubmit.bind(this);
  }

  handleEditDetailsClick (event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    this.setState({ edit: 'details' });
  }

  handleEditSharesClick (event: MouseEvent<HTMLAnchorElement>) {
    event.preventDefault();
    this.setState({ edit: 'shares' });
  }

  handleDialogClose () {
    this.setState({ edit: undefined });
  }

  handleDetailFormSubmit (meta: UserDatasetMeta) {
    this.props.updateUserDatasetDetail(this.props.userDataset, meta);
    this.setState({ edit: undefined });
  }

  render () {
    let { userDataset, isOwner, questionMap, getQuestionUrl , userDatasetUpdating, updateError } = this.props;
    let { edit } = this.state;

    let releventQuestions = userDataset.questions
    .map(name => questionMap[name])
    .filter(q => q)

    if (userDataset == null) {
      return (
        <NotFound>
          <p>
            The User Data Set you requested does not exist, or has been deleted.
            You might be interested in the <Link to="/workspace/datasets">list of User Data Sets</Link> available to you.
          </p>
        </NotFound>
      );
    }

    return (
      <div className={makeClassName()}>

        {userDatasetUpdating &&
          <div style={{ textAlign: 'center'}}>Updating...</div>
        }

        {updateError && <p>Unable to update dataset</p>}

        {isOwner ? (
          <div className={makeClassName('Actions')}>
            <div className={makeClassName('ActionItem')}>
              <a href="#" onClick={this.handleEditDetailsClick}>
                Edit details <i className="fa fa-pencil"/>
              </a>
            </div>
            {userDataset.sharedWith &&
              <div className={makeClassName('ActionItem')}>
                <a href="#" onClick={this.handleEditSharesClick}>
                  Manage sharing ({userDataset.sharedWith.length}) <i className="fa fa-share-alt"/>
                </a>
              </div>
            }
            <div className={makeClassName('ActionItem')}>
              <a href="#" title="Delete this data set and all associated files" onClick={todo}>
                Remove data set <i className="fa fa-trash"/>
              </a>
            </div>
          </div>
        ) : (
          <div className={makeClassName('Actions')}>
            <div className={makeClassName('SharedNote') + ' ' + makeClassName('ActionItem')} title="It is not possible to edit the details of a data set that you do not own.">
              Shared by {userDataset.owner} <i className="fa fa-share-alt"/>
            </div>
            <div className={makeClassName('ActionItem')}>
              <a href="#" title="Delete this data set and all associated files" onClick={todo}>
                Remove data set <i className="fa fa-trash"/>
              </a>
            </div>
          </div>
        )}

        <Dialog
          title="Edit Details"
          open={edit === 'details'}
          modal={true}
          onClose={this.handleDialogClose}
          className={makeClassName('EditDialog')}
        >
          <UserDatasetDetailForm details={userDataset.meta} onSubmit={this.handleDetailFormSubmit}/>
        </Dialog>

        {userDataset.sharedWith &&
          <Dialog
            title="Manage Sharing"
            open={edit === 'shares'}
            modal={true}
            onClose={this.handleDialogClose}
            className={makeClassName('EditDialog')}
          >
            <UserDatasetSharing shares={userDataset.sharedWith}/>
          </Dialog>
        }

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

          {userDataset.percentQuotaUsed && (
            <OverviewItem prompt="Quota Used">
              {userDataset.percentQuotaUsed} %
            </OverviewItem>
          )}

          <OverviewItem prompt="Installed">
            {userDataset.isInstalled ? 'Yes' : 'No'}
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

        {releventQuestions.length && (
          <SectionItem heading="Relevent Questions">
            <ul>
              {releventQuestions.map(question =>
                <li key={question.name}>
                  <a href={getQuestionUrl(question)}>
                    {question.displayName}
                  </a>
                </li>
              )}
            </ul>
          </SectionItem>
        )}

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

export default wrappable(UserDatasetDetail);
