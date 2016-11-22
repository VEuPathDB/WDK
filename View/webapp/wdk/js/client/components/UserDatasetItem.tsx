import React, { StatelessComponent } from 'react';
import { Link } from 'react-router';
import { wrappable } from '../utils/componentUtils';
import { UserDataset } from '../utils/WdkModel';
import { bytesToHuman } from '../utils/Converters';

type Props = {
  userDataset: UserDataset
};

const makeClassName = (element?: string, modifier?: string) =>
  'wdk-UserDataset' +
  (element ? `${element}` : ``) +
  (modifier ? `__${modifier}` : ``);

const displayDate = (time: number) =>
  new Date(time).toLocaleDateString();

const tooltipDate = (time: number) =>
  new Date(time).toString();

const OverViewItem: StatelessComponent<{prompt: string}> = props =>
  <div className={makeClassName('OverviewItem')}>
    <div className={makeClassName('OverviewItemPrompt')}>{props.prompt}:&nbsp;</div>
    <div className={makeClassName('OverviewItemText')}>{props.children}</div>
  </div>

const SectionItem: StatelessComponent<{heading: string}> = props =>
  <div>
    <h2 className={makeClassName('SectionHeading')}>{props.heading}</h2>
    {props.children}
  </div>

const UserDatasetItem: StatelessComponent<Props> = ({userDataset}) =>
  <div className={makeClassName()}>
    <div className={makeClassName('Crumbs')}>
      <Link to="workspace/datasets">User Data Sets</Link> &raquo; {userDataset.id}
    </div>

    <h1 className={makeClassName('Heading')}>
      {userDataset.meta.name}
    </h1>

    <p className={makeClassName('Summary')}>{userDataset.meta.summary}</p>

    <div>
      <OverViewItem prompt="Type">
        {userDataset.type.name} {userDataset.type.version}
      </OverViewItem>

      <OverViewItem prompt="Dependencies">
        {userDataset.dependencies.map(d => `${d.resourceDisplayName} ${d.resourceVersion}`).join(', ')}
      </OverViewItem>

      <OverViewItem prompt="Created">
        <span title={tooltipDate(userDataset.created)}>{displayDate(userDataset.created)}</span>
      </OverViewItem>

      <OverViewItem prompt="Modified">
        <span title={tooltipDate(userDataset.modified)}>{displayDate(userDataset.modified)}</span>
      </OverViewItem>

      <OverViewItem prompt="Size">
        <span title={`${userDataset.percentQuotaUsed}% of quota`}>{bytesToHuman(userDataset.size)}</span>
      </OverViewItem>
    </div>

    <SectionItem heading="Description">
      <div>{userDataset.type.name} {userDataset.meta.description}</div>
    </SectionItem>

    <SectionItem heading="Data Files">
      <ul>
        {userDataset.datafiles.map(datafile =>
          <li key={datafile.name}>{datafile.name} ({bytesToHuman(datafile.size)})</li>
        )}
      </ul>
    </SectionItem>

    {userDataset.sharedWith.length > 0 &&
      <SectionItem heading="Shared With">
        <ul>
          {userDataset.sharedWith.map(share =>
            <li>{share.emailName}</li>
          )}
        </ul>
      </SectionItem>
    }

    <SectionItem heading="Projects">
      <ul>
        {userDataset.projects.map(project =>
          <li key={project}>{project}</li>
        )}
      </ul>
    </SectionItem>

  </div>

export default wrappable(UserDatasetItem);
