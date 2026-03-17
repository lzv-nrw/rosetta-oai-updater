# Rosetta OAI Updater

This tool compares OAI-PMH harvested metadata with existing Intellectual Entities (IEs) in Rosetta.
It generates updateMD XML files for use with the [Update Metadata Job](https://developers.exlibrisgroup.com/rosetta/integrations/metadataupdatejob/) in Rosetta only when the OAI-PMH record datestamp is more recent than the corresponding IE creation date.

## How it works

1. Call OAI-PMH `ListIdentifiers` to retrieve identifiers and datestamps
2. For each identifier, query Solr for the corresponding Intellectual Entity (IE)
3. Compare the OAI record datestamp with the IE creation date
4. If the OAI record is more recent:
   - Retrieve the full metadata record via OAI-PMH `GetRecord`
   - Transform the metadata using the configured XSLT
   - Generate an updateMD XML file compatible with the Rosetta Update Metadata Job
5. Store the generated XML file in the configured `resultPath`

## Installation

### Prerequisites
- Java Development Kit (JDK) 11+
- Apache Maven 3.8+
- Git 2.0+ recommended

### Build

```bash
# Clone the repository
git clone https://github.com/lzv-nrw/rosetta-oai-updater.git
cd rosetta-oai-updater

# Build the fat JAR
mvn clean package
```
This will create a fat JAR named `rosetta-oai-updater.jar` (including all dependencies) in the newly created `target/` directory.

### Run

```
# Run the application
java -jar rosetta-oai-updater.jar
```

## Configuration (config.yml)

The config.yml file defines all connection details and processing configurations for the application. It consists of three main sections: `solr`, `oracle`, and `institutions`. A template file `config.template.yml` exists in the project root. Copy and customize it as needed.

The default is `config.yml` (relative to working directory). This means that config.yml needs to be located in the same directory as the app.


### 1. Solr configuration

This section specifies the Solr endpoint and authentication used to query the index.

```yaml
solr:
  url: http://<solr-host>:<port>/<collection-path>/select
  authHeader: Basic <base64-encoded-credentials>
```

- url: Full Solr query endpoint (including the collection path).
- authHeader: HTTP Authorization header value for Solr access.

### 2. Oracle database configuration

This section defines the database connection parameters.

```yaml
oracle:
  url: jdbc:oracle:thin:@<db-host>:<port>:<sid>
  user: <db-username>
  password: <db-password>
  schemaPrefix: <db-schemaPrefix>
```

- url: JDBC connection string for the Oracle database.
- user: Database username.
- password: Database password.
- schemaPrefix: Prefix of the database schema used in Oracle database, e.g., "V123" for the schema "V123_ABC00"

### 3. Institutions and material flows

The institutions section lists all configured institutions and their related material flows. The institution code must be the same as used in Rosetta. Each institution can have multiple material flows. All configurations should be specified as configured in Rosetta.

```yaml
institutions:
  <institution-code-A>:
    materialFlows:
      - name: <materialflow-name-A>
        baseUrl: <oai-base-url>
        metadataPrefix: <metadata-prefix>
        sets: 
          - <oai-set-name-A>
          - <oai-set-name-B>
        xsltPath: <path-to-xslt-file>
        resultPath: <path-to-output-directory>
      - name: <materialflow-name-B>
        baseUrl: <oai-base-url>
        metadataPrefix: <metadata-prefix>
        sets: []
        xsltPath: <path-to-xslt-file>
        resultPath: <path-to-result-directory>
```

- name: Name of the material flow.
- baseUrl: The base OAI-PMH endpoint URL.
- metadataPrefix: Metadata format used in OAI harvesting.
- sets: List of OAI set names for selective harvesting. If no information about sets is to be specified, an empty list must be passed as follows: `sets: []`.
- xsltPath: Local path to the XSLT transformation file.
- resultPath: Target directory where generated XML files will be stored. Must be writable.

Multiple institutions can be defined under the institutions section, each with its own set of material flows.

### Location of `config.yml`

Set the `config.path` system property for a custom config file location.

```bash
java -Dconfig.path=/path/to/your/config.yml -jar rosetta-oai-updater.jar

```


## Logging

The application writes log files to a `logs/` directory in the working directory.

- The `logs/` directory is created automatically if it does not exist.
- Log files are rotated daily at midnight and written as `app.yyyy-MM-dd.log`.
- A maximum of 30 archived log files is retained; older files are deleted automatically.

Log retention can be adjusted via `<maxHistory>` in `logback.xml` (`src/main/resources/logback.xml`).
