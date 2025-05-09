# Skyflow UDF for AWS Athena

This project contains the source code for the Skyflow User-Defined Functions (UDF) for AWS Athena.

## Project Structure

- `src/main/java/com/amazonaws/athena/connectors/udfs/SkyflowUDFHandler.java`: The main UDF handler class
- `src/main/resources/log4j2.xml`: Log4j2 configuration file
- `pom.xml`: Maven project configuration file

## Building the Project

To build the project, you need to have Maven installed. Then run:

```bash
cd skyflow-udf-project
mvn clean package
```

This will create a JAR file in the `target` directory named `skyflowudf.jar`. You can then copy this JAR file to the parent directory if needed.

## Deploying the UDF

After building the project, you can deploy the UDF to AWS Lambda by following the instructions in the main README.md file in the root directory of this repository.

## Dependencies

The project depends on the following libraries:

- AWS Athena Federation SDK
- SLF4J
- Log4j2
- Skyflow Java SDK
- JSON Simple

All dependencies are managed by Maven and included in the final JAR file.
