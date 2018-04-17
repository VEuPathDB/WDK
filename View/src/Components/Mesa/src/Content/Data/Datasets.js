const TableData = [
  {
    dataSet: 'Aligned genomic sequence reads - strain IQ07',
    organism: ['<i>P. vivax</i> Sal-1'],
    category: 'Genetic variation',
    released: 'PlasmoDB 10.0 / 25-SEP-13',
    summary: 'Whole genome resequencing data from P. vivax strain IQ7 were used to call SNPs and determine CNV at the gene and chromosome level.',
    contact: 'Elizabeth Winzeler',
    contactInstitution: 'The Scripps Research Institute',
    description: `
      <p>Whole genome resequencing data from P. vivax strain IQ7 were analyzed for single nucleotide polymorphisms and copy number variation using Plasmodium vivax Sal1 as a reference. </p>

      <p>SNP analysis</p>
      <p>Reads are aligned to the reference genome using Bowtie 2 and realigned around indels using GATK. SNPs, indels and concensus sequence are generated with VARSCAN and stored in the database.</p>

      <p>CNV analysis</p>
      <p>The median read depth is set to the organism's ploidy and each chromosome's median read depth is normalized to this value. Contigs that are not assigned to chromosomes are excluded from this analysis. Gene copy number is similarly calculated using a normalized read depth for each gene. To compare the number of genes in the resequenced genome to the reference genome, genes are grouped into clusters that are inferred to have originated by tandem duplication.</p>
    `,
    releasePolicy: 'If you wish to publish whole genome or large-scale analyses, please contact the primary investigator or use the published version in the PlasmoDB version 5.3 download folder.',
    publications: [
      { id: '22721170' }
    ]
  },
  {
    dataSet: 'Protein targets of serum antibodies in response to infection',
    organism: ['<i>P. falciparum</i> 3D7'],
    category: 'Immunology',
    released: 'PlasmoDB 11.0 / 03-FEB-14',
    summary: 'Protein targets of antibodies in response to P. falciparum infection were determined by protein microarray hybridized with plasma collected from human malaria patients.',
    contact: 'Peter Crompton',
    contactInstitution: 'National Institutes of Health, USA',
    description: `<p>Plasma collected from human subjects before and after the 6-month Mali malaria season (220 individuals) was hybridized to protein microarrays containing ~23% of the P. falciparum proteome, negative and positive controls and an IgG standard curve. Antibody binding to the protein microarray was detected by streptavidin-conjugated SureLight P-3. Antibody profiles for each patient were generated and significant differential antibody reactivity was calculated.</p>`,
    releasePolicy: null,
    publications: [
      { id: '20351286' }
    ]
  },
  {
    dataSet: 'Schizont enriched EST library',
    organism: ['<i>P. falciparum</i> 3D7'],
    category: 'Transcriptomics',
    released: null,
    summary: 'P. falciparum FcB1 strain, Erythrocytic stages, schizont/merozoite stage EST library, from GenoScope.',
    contact: 'Isabelle Florent',
    contactInstitution: 'Museum National d Histoire Naturelle',
    description: `<p><i>P. falciparum</i> FcB1 strain, Erythrocytic stages, schizont/merozoite stage EST library, from GenoScope.</p>`,
    releasePolicy: null,
    publications: [
      { id: '15287595' },
      { id: '19454033' }
    ]
  },
  {
    dataSet: 'Aligned genomic sequence reads - symptomatic malaria isolates',
    organism: ['<i>P. falciparum</i> 3D7'],
    category: 'Genetic variation',
    description: `
      <p>Whole genome sequencing of isolates collected from symptomatic malaria patients. SNP calls based on whole genome Illumina sequencing used to analyze the role of variant antigen switching in the development of symptomatic malaria.</p>
      GSC PI: Joana Silva<br>
      External PIs: Chris Plowe and Mark Travassos<br>
      Collaborators: Mahamadou Thera and Drissa Coulibaly
    `,
    released: 'PlasmoDB 13.0 / 14-JAN-15',
    summary: 'Whole genome sequencing of isolates collected from symptomatic malaria patients.',
    contact: 'Christopher V. Plowe',
    releasePolicy: null,
    publications: [],
    contactInstitution: 'Howard Hughes Medical Institute/Center for Vaccine Development, University of Maryland, School of Medicine'
  },
  {
    dataSet: 'Nucleosome position during intra-erythrocytic development',
    organism: ['<i>P. falciparum</i> 3D7'],
    category: 'ChIP chip',
    description: `<p>A high-density oligonucleotide microarray to map the position and enrichment of nucleosomes across the entire genome of P. falciparum (3D7) at three time points of the intra-erythrocytic developmental cycle using an unmodified histone H4 antibody for chromatin immunoprecipitation of nucleosome-bound DNA. Additionally, trophozoites were assessed with histone H3K9 acetylation (H3K9ac) antibodies. Parasites were synchronized twice with 5% sorbitol. Rings = 12 hrs; Trophozoites = 30 hrs; Schizont = 42 hrs. Raw data available in <a>geo</a>.</p>`,
    released: null,
    summary: 'A high-density oligonucleotide microarray to map the position and enrichment of nucleosomes across the entire genome of P. falciparum (3D7) at three time points of the intra-erythrocytic developmental cycle using an unmodified histone H4 antibody for chromatin immunoprecipitation of nucleosome-bound DNA.',
    releasePolicy: null,
    publications: [
      { id: '20015349' }
    ],
    contact: 'Elizabeth Winzeler',
    contactInstitution: 'The Scripps Research Institute'
  },
  {
    dataSet: 'Pfal3D7 real-time transcription and decay',
    organism: ['<i>P. falciparum</i> 3D7'],
    category: 'DNA Microarray',
    description: `
      <p>A 48-hour timecourse experiment of highly synchronized P. falciparum 3D7 expressing FCU-GFP in which individual hourly time points were exposed to 40um 4-thiouracil for 10min immediately followed by total RNA extraction. Total RNA from each timepoint was biotinylated via a thiol-group on any transcript that incorporated a thiol-modified dUTP during this time. Biotinylated transcripts were separated from total RNA by Streptavidin magnetic beads resulting in the labeled mRNA sample (red). Unbound mRNA (unlabeled, blue) comprises the remainder of mRNA pool. Prior to biotinylation, total RNA from each sample was set aside and utilized to determine the global total mRNA abundance. For each hourly time point, the three samples (labeled, unlabeled, and total) were analyzed using whole genome Agilent DNA microarrays (Kafsack et al., 2012) resulting in a total of 144 individual microarrays. The resulting intensity values for each gene were utilized to computationally determine the relative contribution of transcription (labeled) and stability/decay (unlabeled) to the total mRNA abundance measured.</p>
    `,
    released: 'PlasmoDB 26 / 10-OCT-15',
    summary: 'Biosynthetic pyrimidine labeling was used for the calculation of real-time, whole-genome analysis of transcription and stability throughout asexual development of P. falciparum 3D7 strain.',
    releasePolicy: null,
    publications: [],
    contact: 'Manuel Llinas',
    contactInstitution: 'Pennsylvania State University'
  },
  {
    dataSet: 'Malaria Literature Database',
    organism: [
      'P. <i>berghei</i> ANKA',
      'P. <i>chabaudi</i> chabaudi',
      'P. <i>falciparum</i> 3D7',
      'P. <i>yoelii</i> yoelii 17XNL'
    ],
    category: 'Link outs',
    description: ``,
    released: '',
    summary: '',
    releasePolicy: null,
    publications: [],
    contact: 'Elizabeth Winzeler',
    contactInstitution: 'The Scripps Research Institute'
  }
];

export default TableData;
