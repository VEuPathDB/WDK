import React, { StatelessComponent } from 'react';
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

const UserDatasetItem: StatelessComponent<Props> = ({userDataset}) =>
  <div className={makeClassName()}>
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
