# opal-legacy-db-stub
This is the stub test service for replicating the legacy GoB DB in environments where it is not available.

## Developement

### 'Mocking' an XML Legacy Response

In order to mock most legacy responses, you'll need to create 2 files:
1. A 'mapping' file to describe the incoming HTTP request to match, with a description of the response.
You'll create this within a **subfolder** within the [./wiremock/mappings/legacy]() folder
2. A file that the contains the 'raw' XML response to the request.
You'll create this within a **subfolder** within the [./wiremock/__files/legacy]() folder.

#### Example Mapping File
Found in [./wiremock/mappings/legacy/BusinessUnit/getBusinessUnit.json]()
```json
{
  "request": {
    "method": "POST",
    "urlPathPattern": "/opal",
    "queryParameters": {
      "actionType": {
        "equalTo": "getBusinessUnit"
      }
    }
  },
  "response": {
    "status": 200,
    "headers": {
      "Content-Type": "application/xml"
    },
    "bodyFileName": "legacy/BusinessUnit/getBusinessUnit.xml"
  }
}
```
There are usually just 2 important things to change in your copy of one of these files:
1. In the top 'request' half, you'll need to change the 'actionType' to match that from the fines service.
E.g. change "getBusinessUnit" to "LIBRA.of_create_defendant_account"
2. In the bottom 'response' half, you'll need to change the 'bodyFileName' to a 'shortened' link to your
'raw' XML mock response.

#### Example 'Raw' XML Response File
Found in [./wiremock/__files/legacy/BusinessUnit/getBusinessUnit.xml]()
```xml
<businessUnitEntity>
  <businessUnitId>1</businessUnitId>
  <businessUnitName>Example Business Unit</businessUnitName>
  <businessUnitCode>BU01</businessUnitCode>
  <businessUnitType>Type1</businessUnitType>
  <accountNumberPrefix>AN</accountNumberPrefix>
  <parentBusinessUnit>
    <businessUnitId>2</businessUnitId>
    <businessUnitName>Parent Business Unit</businessUnitName>
    <businessUnitCode>PBU</businessUnitCode>
    <businessUnitType>Type2</businessUnitType>
    <accountNumberPrefix>PAN</accountNumberPrefix>
  </parentBusinessUnit>
  <opalDomain>Example Domain</opalDomain>
</businessUnitEntity>
```

### 'Other' Development
It's not expected that this Legacy DB Stub service will be needed beyond just supplying raw XML responses
to incoming POST HTTP requests as described above. Wiremock does provide the capability to create
more 'involved' responses depending upon the request, and some tests were done in this regard when the
repository was original created. If interested, run this service and see http://localhost:4553/ as a
starting point.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/opal-legacy-db-stub` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `4553` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:4553/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```

There is no need to remove postgres and java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
