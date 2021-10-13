# WDK Technical Documentation

## What is WDK?

WDK is a search and access platform for data stored in an RDBMS.  You define the record types to be searched, the parameterized SQL queries used to return a set of record IDs, the columns (aka attributes) that can be joined to the result and displayed, and much more.  WDK supports a variety of parameter types, including enum params whose vocabularies are also defined by configured queries, string params, range params, and more.  Dataset parameters allow the user to upload a set of IDs from which a result can be derived.  Users can join the results of two queries using set operations like union, intersect, minus, and even custom joining of results.  A user's search history, including these result trees (strategies), is saved off, editable later, sharable, etc.

## Configuration

Configuration is handled through one or more XML files, referred to as the WDK Model.  These files define:

- The data types available for search
- Available attributes and data tables associated with records of those types
- Available searches and associated SQL queries for those types
- Parameters for each search, used to configure the associated query
- Transformation queries (converting one data type to another)
- Output format and data aggregation/statistics plugins
- Result analysis plugins
- Available result filters
- etc.

See [Configuring the WDK](configuring-the-wdk.html) for more details, or the [WDK Model XML RelaxNG (RNG) Schema](https://github.com/VEuPathDB/WDK/blob/master/Model/lib/rng/wdkModel.rng) for all available options.

On startup, WDK reads and processes these XML files into an in-memory data structure, which it uses to deliver, via REST service, metadata about the configured data types and their properties.  Incoming HTTP requests to query, process, combine, and analyze data reference the in-memory model to inform how to perform the requested actions.

A secondary XML file (model-config.xml) defines:

- The databases WDK will use, namely:
  - AppDb: application domain data
  - UserDb: saved user data (search history, bookmarked records, analysis configurations, user preferences, etc.)
  - AccountDb: user identity information (soon to be deprecated by full OpenIDConnect compliance and external service calls)
- Query performance monitoring config
- User authentication scheme
- etc.

## Record Classes

The record class is the fundamental record type in WDK.  You can think of this as an "object" in OOP-speak.  Record instances are defined by a one- or more- column primary key, which must be unique.  Record classes have attributes (fields) associated with them, 

