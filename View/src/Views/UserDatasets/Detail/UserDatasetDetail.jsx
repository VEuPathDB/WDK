import React from 'react';

import './UserDatasetDetail.scss';
import Link from 'Components/Link';
import moment from 'Utils/MomentUtils';
import Icon from 'Components/Icon/IconAlt';
import { bytesToHuman } from 'Utils/Converters';
import { Mesa, MesaState, AnchoredTooltip } from 'mesa';
import SaveableTextEditor from 'Components/InputControls/SaveableTextEditor';
import { textCell, getDownloadUrl, makeClassifier } from 'Views/UserDatasets/UserDatasetUtils';

const classify = makeClassifier('UserDatasetDetail');

class UserDatasetDetail extends React.Component {
  constructor (props) {
    super(props);
    this.state = { sharingModalOpen: false };

    this.onMetaSave = this.onMetaSave.bind(this);
    this.isMyDataset = this.isMyDataset.bind(this);
    this.validateKey = this.validateKey.bind(this);
    this.handleDelete = this.handleDelete.bind(this);

    this.getAttributes = this.getAttributes.bind(this);
    this.renderAttributeList = this.renderAttributeList.bind(this);
    this.renderHeaderSection = this.renderHeaderSection.bind(this);
    this.renderDatasetActions = this.renderDatasetActions.bind(this);

    this.renderDependencySection = this.renderDependencySection.bind(this);
    this.getDependencyTableColumns = this.getDependencyTableColumns.bind(this);

    this.renderFileSection = this.renderFileSection.bind(this);
    this.getFileTableColumns = this.getFileTableColumns.bind(this);

    this.renderDetailsSection = this.renderDetailsSection.bind(this);
  }

  isMyDataset () {
    const { user, userDataset } = this.props;
    return user && user.id && user.id === userDataset.ownerUserId;
  }

  validateKey (key) {
    const META_KEYS = ['name', 'summary', 'description'];
    if (typeof key !== 'string' || !META_KEYS.includes(key))
      throw new TypeError(`Can't edit meta for invalid key: ${JSON.stringify(key)}`);
  }

  onMetaSave (key) {
    this.validateKey(key);
    return (value) => {
      if (typeof value !== 'string')
        throw new TypeError(`onMetaSave: expected input value to be string; got ${typeof value}`);
      const { userDataset, updateUserDatasetDetail } = this.props;
      const meta = { ...userDataset.meta, [key]: value };
      return updateUserDatasetDetail(userDataset, meta);
    };
  }

  handleDelete () {
    const { isOwner, userDataset, removeUserDataset } = this.props;
    const { sharedWith } = userDataset;
    const shares = !Array.isArray(sharedWith) ? 0 : sharedWith.length;
    const message = `Are you sure you want to ${isOwner ? 'delete' : 'remove'} this dataset?` + (
      !isOwner || !shares ? '' : `The ${shares} other${shares === 1 ? '' : 's'} you've shared with will lose access.`
    );

    if (confirm(message)) {
      removeUserDataset(userDataset);
    }
  }

  renderAllDatasetsLink () {
    return (
      <Link className="AllDatasetsLink" to={'/workspace/datasets'}>
        <Icon fa="chevron-left"/>
        &nbsp; All My Datasets
      </Link>
    );
  }

  getAttributes () {
    const { userDataset } = this.props;
    const { onMetaSave } = this;
    const { id, type, meta, projects, size, percentQuotaUsed, created } = userDataset;
    const { display, name, version } = type;
    const isOwner = this.isMyDataset();

    function normalizePercentage (value) {
      const parsed = parseFloat(value);
      return (Math.floor(value * 100)) / 100
    };

    return [
      {
        className: classify('Name'),
        attribute: 'My Dataset',
        value: (
          <SaveableTextEditor
            value={meta.name}
            onSave={this.onMetaSave('name')}
          />
        )
      },
      {
        attribute: 'Description',
        value: (
          <SaveableTextEditor
            value={meta.description}
            multiLine={true}
            onSave={this.onMetaSave('description')}
          />
        )
      },
      { attribute: 'ID', value: id },
      {
        attribute: 'Data Type',
        value: (<span><b>{display}</b> <span className="faded">({name} {version})</span></span>)
      },
      {
        attribute: 'Summary',
        value: (
          <SaveableTextEditor
            multiLine={true}
            value={meta.summary}
            onSave={onMetaSave('summary')}
            emptyText="No Summary."
          />
        )
      },
      { attribute: 'Compatible Projects', value: projects.join(', ') },
      {
        attribute: 'Created',
        value: (
          <AnchoredTooltip content={moment(created).format()}>
            {moment(created).fromNow()}
          </AnchoredTooltip>
        )
      },
      { attribute: 'Dataset Size', value: bytesToHuman(size) },
      (!isOwner ? null : { attribute: 'Quota Usage', value: `${normalizePercentage(percentQuotaUsed)}%` })
    ].filter(attr => attr);
  }

  renderHeaderSection () {
    const { userDataset } = this.props;
    const { meta } = userDataset;
    const attributes = this.getAttributes();

    const AllLink = this.renderAllDatasetsLink;
    const AttributeList = this.renderAttributeList;
    const DatasetActions = this.renderDatasetActions;

    return (
      <section>
        <AllLink />
        <div className={classify('Header')}>
          <div className={classify('Header-Attributes')}>
            <AttributeList />
          </div>
          <div className={classify('Header-Actions')}>
            <DatasetActions/>
          </div>
        </div>
      </section>
    )
  }

  renderAttributeList () {
    const attributes = this.getAttributes();
    return (
      <div className={classify('AttributeList')}>
        {attributes.map(({ attribute, value, className }, index) => (
          <div className={classify('AttributeRow') + (className ? ' ' + className : '')} key={index}>
            <div className={classify('AttributeName')}>
              {typeof attribute === 'string' ? <strong>{attribute}:</strong> : attribute}
            </div>
            <div className={classify('AttributeValue')}>
              {value}
            </div>
          </div>
        ))}
      </div>
    );
  }

  renderDatasetActions () {
    const { userDataset } = this.props;
    const isOwner = this.isMyDataset();
    return (
      <div className={classify('Actions')}>
        <button className="btn btn-error" onClick={this.handleDelete}>
          <Icon fa="trash" className="left-side" />
          {isOwner ? 'Delete' : 'Remove'}
        </button>
        {!isOwner ? null : (
          <button className="btn btn-success">
            <Icon fa="share-alt" className="left-side" />
            Share
          </button>
        )}
      </div>
    );
  }

  renderDetailsSection () {
    const { userDataset } = this.props;
    return (
      <section>
        <details>
          <pre><code>{JSON.stringify(userDataset, null, '  ')}</code></pre>
        </details>
      </section>
    );
  }

  /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

                                    Files Table

   -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

  renderFileSection () {
    const { userDataset, appUrl } = this.props;
    const fileTableState = MesaState.create({
      columns: this.getFileTableColumns({ userDataset, appUrl }),
      rows: userDataset.datafiles
    });

    return (
      <section>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="files-o"/>
          Data Files
        </h3>
        <Mesa state={fileTableState}/>
      </section>
    );
  }

  getFileTableColumns ({ userDataset, appUrl }) {
    const { datafiles, type, ownerUserId, id } = userDataset;
    const trackData = type.data;
    const shouldShowGBrowseTrackUpload = Array.isArray(datafiles)
      && Array.isArray(trackData)
      && datafiles.some(({ name }) => {
        return trackData.find(uploadableFile => uploadableFile.dataFilename === name);
      });

    return [
      {
        key: 'name',
        name: 'File Name',
        renderCell ({ row }) {
          const { name } = row;
          return <code>{name}</code>
        }
      },
      {
        key: 'size',
        name: 'File Size',
        renderCell ({ row }) {
          const { size } = row;
          return bytesToHuman(size);
        }
      },
      {
        key: 'download',
        name: 'Download',
        width: '130px',
        headingStyle: { textAlign: 'center' },
        renderCell ({ row }) {
          const { name } = row;
          const downloadUrl = appUrl + getDownloadUrl(id, name);
          return (
            <a href={downloadUrl} title="Download this file">
              <button className="btn btn-info">
                <Icon fa="save" className="left-side" /> Download
              </button>
            </a>
          )
        }
      }
    ].filter(column => column);
  }


  /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

                                Dependency Table

   -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

  renderDependencySection () {
    const { userDataset } = this.props;

    const depedencyTableState = MesaState.create({
      columns: this.getDependencyTableColumns(userDataset),
      rows: userDataset.dependencies
    });

    return (
      <section>
        <h3 className={classify('SectionTitle')}>
          <Icon fa="puzzle-piece"/>
          Dataset Dependencies &nbsp;
          <AnchoredTooltip content="The data and genomes listed here are requisite for using the data in this user dataset.">
            <div className="HelpTrigger">
              <Icon fa="question-circle"/>
            </div>
          </AnchoredTooltip>
        </h3>
        <div style={{ maxWidth: '600px' }}>
          <Mesa state={depedencyTableState}/>
        </div>
      </section>
    );
  }

  getDependencyTableColumns () {
    return [
      {
        key: 'resourceDisplayName',
        name: 'Resource',
        renderCell ({ row }) {
          const { resourceDisplayName } = row;
          return resourceDisplayName;
        }
      },
      {
        key: 'resourceVersion',
        name: 'Minimum Version'
      }
    ]
  }

  /* -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

                                General Rendering

   -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= */

  getPageSections () {
    return [
      this.renderHeaderSection,
      this.renderDependencySection,
      this.renderFileSection,
      this.renderDetailsSection
    ];
  }

  render () {
    return (
      <div className={classify()}>
        {this.getPageSections().map((Section, key) => <Section key={key}/>)}
      </div>
    )
  }
};

export default UserDatasetDetail;
