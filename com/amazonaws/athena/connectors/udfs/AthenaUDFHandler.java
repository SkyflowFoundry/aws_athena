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

import com.skyflow.common.utils.HttpUtility;
import com.skyflow.entities.ResponseToken;
import com.skyflow.entities.SkyflowConfiguration;
import com.skyflow.entities.TokenProvider;
import com.skyflow.errors.SkyflowException;
import com.skyflow.serviceaccount.util.Token;
import com.skyflow.vault.Skyflow;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AthenaUDFHandler
        extends UserDefinedFunctionHandler
{
  /** Detokenize a string
   *
   *
   *
   * @param input the string to detokenize
   * @param role role
   * @param redactionParam redaction type
   * @return detokenized value
   */

  public String detokenize(String input, String role, String redactionParam) 
  {
            String vaultID = System.getenv("vaultID");
            String vaultURL = System.getenv("vaultURL");
            String secretName = System.getenv("secretName");
      if (!vaultID.isEmpty() && !vaultURL.isEmpty() && !secretName.isEmpty())  {
        try {
            String redaction = "DEFAULT";
            if (redactionParam.length() > 0 && (redactionParam.equals("PLAIN_TEXT") || redactionParam.equals("REDACTED") || redactionParam.equals("MASKED"))) {
                redaction = redactionParam;
            }
            StringBuffer response = null;
            String endPointURL = vaultURL + vaultID;
            /** cachableSecretsManager will be initialized */
            String secretString = cachableSecretsManager.getSecret(secretName);
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
            long startTime = System.currentTimeMillis();
            String apiResponse = HttpUtility.sendRequest("POST", new URL(endPointURL), bodyJson, headers);
            long endTime = System.currentTimeMillis();
            long latency = endTime - startTime;
            System.out.println("Skyflow Latency " + latency + " ms");
            JSONObject responseJson = (JSONObject) parser.parse(apiResponse);
            JSONArray responseRecords = (JSONArray) responseJson.get("records");
            JSONObject responseObject = (JSONObject) responseRecords.get(0);
            return (String) responseObject.get("value");
        }
        catch (Exception e) {
            return "error";
        }
    }
    else {
      System.out.println("Invalid UDF configuration");
      return "error";
    }
  }
}
