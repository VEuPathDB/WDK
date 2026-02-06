# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

The Strategies Website Development Kit (WDK) is a Java-based search platform and REST API for accessing data stored in relational databases. It provides an XML-configurable framework for exposing parameterized queries that can be saved, edited, shared, and combined into complex search strategies.

## Build System

This project uses both Apache Ant and Maven for building:

### Primary Build System (Ant)
- **Build command**: `ant WDK-Installation`
- **Web installation**: `ant WDK-WebInstallation` 
- **Main build file**: `build.xml` (root level)
- Depends on external projects: FgpUtil and WSF

### Maven Structure
- **Root POM**: `pom.xml` (multi-module project)
- **Modules**: 
  - `Model/` - Core WDK model and framework (`wdk-model` artifact)
  - `Service/` - REST API service layer (`wdk-service` artifact)
- **Maven build**: `mvn clean install` (for individual modules)
- **Test**: `mvn test` (runs JUnit tests)

## Architecture

### Two-Module Structure
1. **Model Module** (`Model/`):
   - Core WDK framework and data model
   - Query processing and record management
   - XML configuration parsing
   - Database connectivity
   - Analysis plugins and testing framework

2. **Service Module** (`Service/`):
   - REST API endpoints
   - Jersey-based web services
   - JSON schema validation
   - Client-facing web interface

### Key Packages
- `org.gusdb.wdk.model.*` - Core model classes, queries, records
- `org.gusdb.wdk.controller.*` - Web application controllers
- `org.gusdb.wdk.service.*` - REST API endpoints  
- `org.gusdb.wdk.model.test.*` - Testing framework

## Testing

### Test Commands
- **Sanity Tests**: `Model/bin/wdkSanityTest`
- **Unit Tests**: `Model/bin/wdkUnitTest` 
- **Regression Tests**: `Model/bin/wdkRegressionTest`
- **Stress Tests**: `Model/bin/wdkStressTest`
- **SQL Munge Tests**: `Model/bin/wdkSqlMungeTest`
- **Service Client Test**: `Model/bin/testWdkServiceClient.sh`

### Test Structure
- JUnit tests in `src/test/java/` directories
- Comprehensive testing framework in `org.gusdb.wdk.model.test.*`
- Sanity testing for XML configurations and queries
- Stress testing for performance evaluation

## Dependencies

### External Dependencies
- FgpUtil library (utilities)
- WSF (Web Service Framework)
- Oracle/PostgreSQL database drivers
- Jersey for REST services
- Jackson for JSON processing
- Apache Commons libraries

### Database Support
- Oracle and PostgreSQL primary support
- Database-specific drivers must be installed separately in webapp server

## Configuration

### XML Configuration
- Model configurations in XML format
- Parameterized queries defined via XML
- Record types and attributes configured declaratively

### Environment Setup
- Requires database connectivity configuration
- Web application deployment to servlet container (Tomcat recommended)
- Symlinked resources for web deployment

## Development Workflow

1. Use Ant for full project builds and installations
2. Use Maven for module-specific development and testing
3. Run appropriate test suites before committing changes
4. XML configurations should be validated using provided tools
5. Database schema changes require coordination with deployment