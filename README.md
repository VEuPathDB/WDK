# Strategies Website Development Kit (WDK)

Strategies WDK is a powerful Java-based search platform and REST API to access data stored in a relational database.  When combined with the [WDKClient](https://github.com/VEuPathDB/WDKClient) web client, it provides an online workspace for exposing relational and other data through a pre-configured set of parameterized queries.  These queries can be saved, edited, and shared.  They can also be combined to form a complex search “strategy”, a tree-based flow of search results.

Features include:
- XML configuration of:
    - Record types (Record Classes)
    - Parameterized searches (Questions)
    - Record-based attributes and data tables
- Plugin APIs to support:
    - configurable result filters
    - configurable reports/formats
    - asynchronous result analysis (Step Analysis)
    - non-RDBMS search (Process Queries)
    - result type transformation (e.g. genes to pathways)
- Finding and bookmarking a particular record (Record Pages, Favorites)
- Saving off a set of records (Baskets)
- User login and preference storage
- Widescale result caching
- Extensible coding APIs for customized websites

Full up-to-date technical documentation (in progress) can be found [here](https://veupathdb.github.io/WDK).

Original GUS/WDK project description is [here](https://www.cbil.upenn.edu/node/86).

Legacy documentation can be found [here](https://docs.google.com/document/d/1kmNWkkcInKoxxiuUJqbc4QPWuySQud0OuECJ2TmEMTo/pub).


This system has proved highly successful in enabling sophisticated data-mining of 'omics' data (e.g. genes, SNPs, studies) by a diverse group of end users.  For VEuPathDB, it has primarily been used to mine data in the [GUS Schema](https://github.com/VEuPathDB/GusSchema) (Fischer et al. Database 2011), but has recently been updated so as to provide the ability to browse extensive metadata, e.g. [clinical epidemiology variables](https://clinepidb.org), inspired by the Harvest data discovery platform (Pennington et. al., JAMIA 2014).

Exploiting the GUS/Strategies-WDK system, our group has successfully developed and deployed a system in support of functional genomics data for diverse user communities, including the [NIAID VEuPathDB Bioinformatics Resource Center](http://veupathdb.org), the [NIDDK Beta Cell Biology Consortium Genomics Resource](http://genomics.betacell.org), and the [NIA NIAGADS Genomics database](www.niagads.org/genomics). Although thus far used primarily for functional genomics datasets, our system is inherently generalizable beyond omics data, including clinical records.

Explore [plasmodb.org](https://plasmodb.org), a large scale production site powered by the WDK.
