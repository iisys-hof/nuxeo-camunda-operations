# nuxeo-camunda-operations
Camunda Operations for Nuxeo, for Automation Chains etc.

Nuxeo Camunda Operations, like http://explorer.nuxeo.com/nuxeo/site/distribution/current/listOperations

Still lack proper authentication with Camunda - should be solved by SSO.

Operations:
* Camunda.StartWorkflow - Start a workflow in Camunda for a document
* Camunda.ActivateWorkflow - Activate a suspended workflow
* Camunda.SuspendWorkflow - Suspend a workflow
* Camunda.CompleteTask - Complete a task
* Camunda.DeleteWorkflow - Delete a workflow
* Camunda.DeliverMessage - Deliver a message to Camunda
* Camunda.ReadTasks - Read all tasks for currently registered workflows from Camunda and store as metadata

Installation:

1. Import Project into Nuxeo IDE
2. Right Click on Project -> Nuxeo, export jar
3. Place jar in nxserver/plugins/ directory
4. Restart Nuxeo

Configuration file: /src/main/resources/camunda-operations.properties