# Skyflow Athena Integration

This guide provides step-by-step instructions to integrate Skyflow User-Defined Functions (UDF) with AWS Athena.

## Prerequisites

- Skyflow vault and the necessary credentials (Vault ID, Vault URL, and API keys/access tokens).
- AWS account with access to Athena, Lambda, Secrets Manager, and IAM.

## Step 1: Download the Skyflow UDF Jar File

Obtain the Skyflow UDF Jar file from the Skyflow team.

## Step 2: Create a Secret in AWS Secrets Manager

1. In the AWS Management Console, navigate to the Secrets Manager service.
2. Click "Store a new secret".
3. Choose "Other type of secret" and select "Plaintext".
4. In the "Plaintext" field, enter a JSON object with the following structure:

   ```json
   {
     "your-skyflow-role-here": "your-skyflow-access-token-here"
   }
   ```

   Replace `your-skyflow-role-here` with the Skyflow role you want to use (e.g., "Vault Editor"), and `your-skyflow-access-token-here` with the corresponding access token or API key.

5. Set the secret name to `skyflow-creds`.
6. Complete the secret creation process.

## Step 3: Create an IAM Policy

1. In the AWS Management Console, navigate to the IAM service.
2. Click "Policies" and then "Create policy".
3. In the JSON tab, paste the following policy:

   ```json
   {
     "Version": "2012-10-17",
     "Statement": [
       {
         "Sid": "VisualEditor0",
         "Effect": "Allow",
         "Action": "secretsmanager:GetSecretValue",
         "Resource": "arn:aws:secretsmanager:*:{account-id}:secret:skyflow-creds-*"
       }
     ]
   }
   ```

   Replace `{account-id}` with your AWS account ID.

4. Set the policy name to `GetSkyflowSecretValue`.
5. Create the policy.

## Step 4: Create an IAM Role to Associate with Lambda Function

1. In the IAM service, click "Roles" and then "Create role".
2. Select "AWS service" as the trusted entity and choose "Lambda" as the use case.
3. Attach the following policies to the role:
   - `AWSLambdaBasicExecutionRole`
   - `GetSkyflowSecretValue` (the policy you created in the previous step)
4. Set the role name to `skyflowudfrole`.
5. Create the role.

## Step 5: Create a Lambda Function

1. In the AWS Management Console, navigate to the Lambda service.
2. Click "Create function".
3. Set the function name to `skyflowudf`.
4. Choose "Java 11" as the runtime.
5. Click on "Change default execution role" and under "Execution role", select "Use an existing role" and choose the `skyflowudfrole` role you created in the previous step.
6. Click the "Create function" button.
7. In the "Code" section, upload the Skyflow UDF Jar file you obtained earlier.
8. Click on "Edit" under the "Runtime settings" section and in the "Handler" field, enter `com.amazonaws.athena.connectors.udfs.SkyflowUDFHandler`.
9. In the "Environment variables" section under the configuration tab, add the following variables:

   - `VAULT_ID`: Your Skyflow vault ID
   - `VAULT_URL`: Your Skyflow vault URL
   - `SECRET`: "skyflow-creds" (the secret name you created in Step 2)
   - `LOG_LEVEL`: "false" (set to "true" if you want to log API latency)

   Note: All variable names should be uppercase (CAPS)

10. Save the Lambda function.

## Step 6: Use the Skyflow UDF in Athena

In the Athena console, you can now use the Skyflow UDF in your queries. Here are some examples:

### Retrieving a Record Using the `getrecord` External Function

1. Retrieving a record using the getrecord external function:

```sql
USING EXTERNAL FUNCTION getrecord(id varchar, tablename varchar, role varchar, redaction varchar)
RETURNS VARCHAR lambda 'skyflowudf'
SELECT getrecord(skyflow_id, 'aadhaar', 'administrator', 'REDACTED')
FROM aadhaar
LIMIT 10;
```

This query uses the getrecord external function to retrieve a record from the aadhaar table. The function takes the following parameters:
- id: The Skyflow ID of the record to retrieve
- tablename: The name of the table in Skyflow vault, the record is in
- role: The Skyflow role of the user requesting the data
- redaction: The redaction type to apply to the retrieved data
The function returns the retrieved data as a VARCHAR.

2. Detokenizing columns using the detokenize external function:
```sql
USING EXTERNAL FUNCTION detokenize(col varchar, skyflowrole varchar, redaction varchar)
RETURNS VARCHAR lambda 'skyflowudf' SELECT
detokenize(name, 'administrator', 'PLAIN_TEXT') as name,
detokenize(gender, 'financer', 'DEFAULT') as gender,
country,
skyflow_id
FROM itr
LIMIT 50;
```
This query uses the detokenize external function to detokenize the name and gender columns from the itr table. The function takes the following parameters:

- col: The column with tokens that need to be detokenized
- skyflowrole: The Skyflow role of the user requesting the data
- redaction: The redaction type to apply to the detokenized data
  The function returns the detokenized data as a VARCHAR. The query selects the detokenized name and gender columns, along with the country and skyflow_id columns, and limits the results to 50 rows.
  Note that both examples use the USING EXTERNAL FUNCTION syntax to call the Skyflow-provided functions getrecord and detokenize. These functions are defined as AWS Lambda functions that are integrated with the Athena environment.

## Additional Recommendations

- Athena processes queries by assigning resources based on the overall service load and the number of incoming requests. Your queries may be temporarily queued before they run. You can purchase dedicated capacity for your queries using the Capacity Reservation feature.
- There is no defined limit for parallel Lambda requests; it will depend on the records that can be processed in a single go with Athena, and how much the Lambda function can handle.

### References

- [Athena Capacity Management Requirements](https://docs.aws.amazon.com/athena/latest/ug/manage-capacity.html)
- [Athena Service Limits](https://docs.aws.amazon.com/athena/latest/ug/service-limits.html)
- [Querying with UDFs in Athena](https://docs.aws.amazon.com/athena/latest/ug/athena-udfs.html)
- [Lambda Concurrency](https://docs.aws.amazon.com/lambda/latest/dg/gettingstarted-limits.html)
