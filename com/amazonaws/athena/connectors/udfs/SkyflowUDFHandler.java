/*-
 * #%L
 * athena-udfs
 * %%
 * Copyright (C) 2019 Amazon Web Services
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.amazonaws.athena.connectors.udfs;

import com.amazonaws.athena.connector.lambda.handlers.UserDefinedFunctionHandler;
import com.amazonaws.athena.connector.lambda.security.CachableSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClient;
import com.skyflow.common.utils.HttpUtility;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class SkyflowUDFHandler
        extends UserDefinedFunctionHandler
{
    private static final String SOURCE_TYPE = "athena_common_udfs";
    private final CachableSecretsManager cachableSecretsManager;
    Boolean logLevel = Boolean.valueOf(System.getenv("LOG_LEVEL"));

    public SkyflowUDFHandler()
    {
        this(new CachableSecretsManager(AWSSecretsManagerClient.builder().build()));
    }
    
    SkyflowUDFHandler(CachableSecretsManager cachableSecretsManager)
    {
        super(SOURCE_TYPE);
        this.cachableSecretsManager = cachableSecretsManager;
    }
   public String detokenize(String input, String role, String redactionParam)
    {
        String response = null;
        try {
            String vaultID = System.getenv("VAULT_ID");
            String vaultURL = System.getenv("VAULT_URL");
            String secretName = System.getenv("SECRET");
            String secretString = cachableSecretsManager.getSecret(secretName);
            String redaction = "DEFAULT";
            if (redactionParam.length() > 0) {
                redaction = redactionParam;
            }
            String endPointURL = vaultURL + "/v1/vaults/" + vaultID + "/detokenize";
            JSONParser parser = new JSONParser();
            JSONObject secrets = (JSONObject) parser.parse(secretString);
            String apiKey = (String) secrets.get(role);
            JSONObject bodyJson = new JSONObject();
            JSONArray detokenizationParameters = new JSONArray();
            JSONObject parameterObject = new JSONObject();
            parameterObject.put("token", input);
            parameterObject.put("redaction", redaction);
            detokenizationParameters.add(parameterObject);
            bodyJson.put("detokenizationParameters", detokenizationParameters);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            long startTimeD = System.currentTimeMillis();
            String apiResponse = HttpUtility.sendRequest("POST", new URL(endPointURL), bodyJson, headers);
            long endTimeD = System.currentTimeMillis();
            long latency = endTimeD - startTimeD;
            if (logLevel) {
                System.out.println("Skyflow Detokenize Latency " + latency + " ms");
            }
            JSONObject responseJson = (JSONObject) parser.parse(apiResponse);
            JSONArray responseRecords = (JSONArray) responseJson.get("records");
            JSONObject responseObject = (JSONObject) responseRecords.get(0);
            response = (String) responseObject.get("value");
        } 
        catch (Exception e) {
            System.out.println("Error" + e);
            response = "error";
        }
        return response;
    }

    public String getrecord(String skyflowId, String tableName, String role, String redactionParam)
    {
        String response = null;
        try {
            String vaultID = System.getenv("VAULT_ID");
            String vaultURL = System.getenv("VAULT_URL");
            String secretName = System.getenv("SECRET");
            String secretString = cachableSecretsManager.getSecret(secretName);
            String redaction = "DEFAULT";
            if (redactionParam.length() > 0) {
                redaction = redactionParam;
            }
            JSONParser parser = new JSONParser();
            JSONObject secrets = (JSONObject) parser.parse(secretString);
            String apiKey = (String) secrets.get(role);
            String url = vaultURL + "/v1/vaults/" + vaultID + "/" + tableName + "?skyflow_ids=" + skyflowId + "&redaction=" + redaction;
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey);
            long startTime = System.currentTimeMillis();
            String apiResponse = HttpUtility.sendRequest("GET", new URL(url), null, headers);
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;
            if (logLevel) {
                System.out.println("Skyflow GetBySkyflowId Latency " + latency + " ms");
            }
            JSONArray responseRecords = (JSONArray) ((JSONObject) (new JSONParser().parse(apiResponse))).get("records");
            response = responseRecords.get(0).toString();
        } 
        catch (Exception e) {
            System.out.println("Error" + e);
            response = "error";
        }
        return response;
    }
}
