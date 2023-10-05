---
title: "Configuration"
date: 2022-12-12T11:35:40-05:00
weight: 1
---
## Overwatch Deployment Configuration

### How it works
Overwatch deployment is driven by a configuration file which will ultimately be loaded into the deployment as 
a csv format. This csv file will contain all the necessary details to perform the deployment. Since CSVs are a bit 
cantankerous we've offered two different methods for building the configuration file. If you're good at VSCode or 
similar text editor and want to edit the CSV directly feel free to do so.

**IF YOU INTEND TO USE EXCEL (RECOMMENDED)** note that Excel may alter several pieces of the CSV; thus we recommend you **complete 
all your edits in the .xlsx file** and then Save As a .csv extension. Before you upload the file spot-check the CSV 
to ensure everything looks correct. Rest assured though, there are many 
[validations]({{%relref "DeployOverwatch/ConfigureOverwatch/Validation.md"%}}) that will alert you if there are 
any issues.

**IF YOU INTEND TO USE CSV** do not open or edit the CSV in Excel unless you're skilled with editing CSVs in Excel. 
Excel may attempt to auto-format certain fields and can break the required format, especially workspace_id and date 
fields.

Regardless of the method you choose, **DO NOT DELETE FIELDS** you don't think you need. If the field doesn't pertain 
to you just leave it blank to avoid any strange issues when parsing the CSV.

### Building Your Config

Please use the template for your version. If you are upgrading from 071x to 072x the upgrade script in the change log
will upgrade the script for you.
* 0.7.2.x+ ([.XLSX](/assets/DeployOverwatch/072x_overwatch_deployment_config_template.xlsx) |
  [.CSV](/assets/DeployOverwatch/072x_overwatch_deployment_config_template.csv))
  * Two column names were changed in 072x to make the scope a little broader
    * "etl_storage_prefix" --> "storage_prefix"
    * "auditlogprefix_source_aws" --> "auditlogprefix_source_path"
* 0.7.1.x ([.XLSX](/assets/DeployOverwatch/071x_overwatch_deployment_config_template.xlsx) |
  [.CSV](/assets/DeployOverwatch/071x_overwatch_deployment_config_template.csv))

**To Download** Right-click the file type you want and click "Save link as..." or "Save target as..."

### Column description
Below are the full details of each column in the config **for 0.7.2.x**. For exact/complete configuration options by 
version please see [Configuration Details By Version]({{%relref "DeployOverwatch/ConfigureOverwatch/ConfigDetailsByVersion.md"%}})

| Column                     | Type    | IsRequired         | Description                                                                                                                                                                                                                                                                                                               |
|:---------------------------|:--------|:-------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| workspace_name             | String  | True               | Name of the workspace.                                                                                                                                                                                                                                                                                                    |
| workspace_id               | String  | True               | Id of the workspace. **MUST BE VALUE AFTER THE o=** in the URL bar. To ensure you get the right value, run the following on the target workspace. `Initializer.getOrgId`                                                                                                                                                  |
| workspace_url              | String  | True               | URL of the workspace. Should be in format of *https://\*.com* or *https://\*.net*. Don't include anything after the .com or .net suffix                                                                                                                                                                                   |
| api_url                    | String  | True               | API URL for the Workspace (execute in scala `dbutils.notebook.getContext().apiUrl.get` **ON THE TARGET WORKSPACE NOT DEPLOYMENT WORKSPACE** to get the API URL for the workspace. NOTE: Workspace_URL and API_URL can be different for a workspace but may be the same even for multiple workspaces).                     |
| cloud                      | String  | True               | Cloud provider (Azure or AWS).                                                                                                                                                                                                                                                                                            |
| primordial_date            | String  | True               | The date from which Overwatch will capture the details. The **format** should be **yyyy-MM-dd** ex: 2022-05-20 == May 20 2022                                                                                                                                                                                             |
| storage_prefix	            | String  | True               | **CASE SENSITIVE - Lower Case** The location on which Overwatch will store the data. You can think of this as the Overwatch working directory. dbfs:/mnt/path/... or abfss://container@myStorageAccount.dfs.core.windows.net/... or s3://myBucket/...   or gs://myBucket/...                                              |
| etl_database_name          | String  | True               | The name of the ETL data base for Overwatch (i.e. overwatch_etl or custom)                                                                                                                                                                                                                                                |
| consumer_database_name     | String  | True               | The name of the Consumer database for Overwatch. (i.e. overwatch or custom)                                                                                                                                                                                                                                               |
| secret_scope	              | String  | True               | Name of the secret scope. This must be created on the workspace which the Overwatch job will execute.                                                                                                                                                                                                                     |
| secret_key_dbpat	          | String  | True               | This will contain the PAT token of the workspace. The key should be present in the secret_scope and should start with dapi.                                                                                                                                                                                               |
| auditlogprefix_source_path | String  | True (only for **AWS/GCP**) | Location of auditlog (**AWS/GCP Only**). The contents under this directory must have the folders with the date partitions like date=2022-12-01                                                                                                                                                                            |
| interactive_dbu_price	     | Double  | True               | Contract (or list) Price for interactive DBUs. The provided template has the list prices by default.                                                                                                                                                                                                                      |
| automated_dbu_price	       | Double  | True               | Contract (or list) Price for automated DBUs. The provided template has the list prices by default.                                                                                                                                                                                                                        |
| sql_compute_dbu_price      | Double  | True               | Contract (or list) Price for DBSQL DBUs. This should be the closest average price across your DBSQL Skus (classic / Pro / Serverless) for now. See [Custom Costs]({{%relref "DeployOverwatch/ConfigureOverwatch/CustomCosts.md"%}}) for more details. The provided template has the DBSQL Classic list prices by default. |
| jobs_light_dbu_price	      | Double  | True               | Contract (or list) Price for interactive DBUs. The provided template has the list prices by default.                                                                                                                                                                                                                      |
| max_days                   | Integer | True               | This is the max incrementals days that will be loaded. Usually only relevant for historical loading and rebuilds. Recommendation == 30                                                                                                                                                                                    |
| excluded_scopes	           | String  | False              | [Scopes]({{%relref "DataEngineer/Modules.md"%}}/#scopes) that should not be excluded from the pipelines. Since this is a CSV, it's critical that these are **colon delimited**. Leave blank if you'd like to load all overwatch scopes.                                                                                   |
| active                     | Boolean | True               | Whether or not the workspace should be validated / deployed.                                                                                                                                                                                                                                                              |
| proxy_host	                | String  | False              | Proxy url for the workspace.                                                                                                                                                                                                                                                                                              |
| proxy_port	                | String  | False              | Proxy port for the workspace                                                                                                                                                                                                                                                                                              |
| proxy_user_name	           | String  | False              | Proxy user name for the workspace.                                                                                                                                                                                                                                                                                        |
| proxy_password_scope	      | String  | False              | Scope which contains the proxy password key.                                                                                                                                                                                                                                                                              |
| proxy_password_key         | String  | False              | Key which contains proxy password.                                                                                                                                                                                                                                                                                        |
| success_batch_size	        | Integer | False              | API Tunable - Indicates the size of the buffer on filling of which the result will be written to a temp location. This is used to tune performance in certain circumstances. Leave default except for special circumstances. Default == 200                                                                               |
| error_batch_size	          | Integer | False              | API Tunable - Indicates the size of the error writer buffer containing API call errors. This is used to tune performance in certain circumstances. Leave default except for special circumstances. Default == 500                                                                                                         |
| enable_unsafe_SSL	         | Boolean | False              | API Tunable - Enables unsafe SSL. Default == False                                                                                                                                                                                                                                                                        |
| thread_pool_size	          | Integer | False              | API Tunable - Max number of API calls Overwatch is allowed to make in parallel. Default == 4. Increase for faster bronze but if workspace is busy, risks API endpoint saturation. Overwatch will detect saturation and back-off when detected but for safety never go over 8 without testing.                             |
| api_waiting_time	          | Long    | False              | API Tunable - Overwatch makes async api calls in parallel, api_waiting_time signifies the max wait time in case of no response received from the api call. Default = 300000(5 minutes)                                                                                                                                    |
| mount_mapping_path         | String  | False              | Path to local CSV holding details of all mounts on remote workspaces (only necessary for remote workspaces with >50 mounts) **[click here for more details]({{%relref "DataEngineer/AdvancedTopics"%}}/#exception---remote-workspaces-with-50-mounts)**                                                                   |
| temp_dir_path              | String  | False              | Custom temporary working directory, directory gets cleaned up after each run.                                                                                                                                                                                                                                             |

#### Azure Event Hub Specific Configurations
When configuring the Azure EH configurations users can use EITHER a **shared access key** OR **AAD SP as of 072x** 
to authenticate to the EH. Below are the required configurations for each auth method. One of the options for Azure 
deployments must be used as EH is required for Azure.

**Shared Access Key Requirements**
Review [Authorizing Access Via SAS Policy]({{%relref "DeployOverwatch/CloudInfra/Azure.md"%}}/#step-21-authorizing-access-via-sas-policy) 
for more details.

| Column        | Type   | IsRequired       | Description                                                                                                                                                                                                                                                   |
|:--------------|:-------|:-----------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| eh_name       | String | True (**AZURE**) | Event hub name (**Azure Only**) The event hub will contain the audit logs of the workspace                                                                                                                                                                    |
| eh_scope_key	 | String | True (**AZURE**) | Name of the key in the <secret_scope> that holds the connection string to the Event Hub WITH THE SHARED ACCESS KEY IN IT -- See [EH Configuration]({{%relref "DeployOverwatch/CloudInfra/Azure.md"%}}/#step-21-authorizing-access-via-sas-policy) for details |

**AAD Requirements**

Review [Authorizing Access Via AAD SPN]({{%relref "DeployOverwatch/CloudInfra/Azure.md"%}}/#step-22-authorizing-access-via-aad-spn) 
for more details.

Ensure the dependent library for AAD Auth is attached `com.microsoft.azure:msal4j:1.10.1` 

| Column                 | Type   | IsRequired       | Description                                                                                                |
|:-----------------------|:-------|:-----------------|:-----------------------------------------------------------------------------------------------------------|
| eh_name                | String | True (**AZURE**) | Event hub name The event hub will contain the audit logs of the workspace                                  |
| eh_conn_string         | String | True (**AZURE**) | Event hub connection string without shared access key. ex: "Endpoint=sb://evhub-ns.servicebus.windows.net" |
| aad_tenant_id          | String | True (**AZURE**) | Tenant ID for Service principle.                                                                           |
| aad_client_id          | String | True (**AZURE**) | Client ID for Service principle.                                                                           |
| aad_client_secret_key  | String | True (**AZURE**) | Name of the key in the <secret_scope> that holds the SPN secret for the Service principle.                 |
| aad_authority_endpoint | String | True (**AZURE**) | Endpoint of the authority. Default value is "https://login.microsoftonline.com/"                           |
