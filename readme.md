# Athena + Skyflow Integration

This repository demonstrates how to integrate Athena and Skyflow to securely query sensitive data stored in Amazon S3.

## Features

- Securely query sensitive data stored in Amazon S3 using Athena
- Leverage Skyflow's data privacy platform to protect sensitive data
- Seamlessly integrate Athena and Skyflow workflows

## Prerequisites

- AWS account with access to Athena and S3
- Skyflow account and API credentials
- Java programming language (as this is the only supported SDK)

## Setup

1. **Create a secret to store credentials:**
    - Use the AWS Secrets Manager SDK for Java to create a new secret with name `skyflow-creds`.
    - Store your Skyflow service account credentials (e.g., credentials.json) in the secret as Key-Value pair (`ROLE` : `API_KEY`).
    - Note the secret name, as you'll need it later.

2. **Publish the UDF into Serverless and deploy a Lambda using it:**
    - Copy the detokenize method and paste it in the AthenUDFHandler.java class.
    - Follow the instructions in the [Athena documentation](https://docs.aws.amazon.com/athena/latest/ug/querying-udf.html#udf-creating-and-deploying) to create and deploy a User-Defined Function (UDF).
    
3. **Add vault details as Lambda configuration variables:**
    - In the AWS Lambda console, select the lambda that was created in the previous step.
    - Under the "Configuration" tab, add the following environment variables:
        - `VAULT_ID`: Your Skyflow vault ID
        - `VAULT_URL`: Your Skyflow vault URL
        - `SECRET_NAME`: Name of the secret created in the first step

