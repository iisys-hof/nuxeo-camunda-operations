package de.hofuniversity.iisys.nuxeo.camunda.operations;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import de.hofuniversity.iisys.nuxeo.camunda.runners.RefreshWorkflowsRunner;

@Operation(id = "Camunda.RefreshWorkflows", category = "CamundaWorkflows", label = "Refresh Camunda Workflows", description = "Deletes stale Camunda Workflows and Tasks and adds new tasks")
public class RefreshWorkflowsOperation
{
	//TODO: authentication
	
	@Context
    protected CoreSession fSession;
	
	public RefreshWorkflowsOperation()
	{
		
	}
	
	@OperationMethod
	public DocumentModel run(DocumentModel doc)
	{
		new RefreshWorkflowsRunner(fSession, doc);
		
		fSession.save();
		
		return doc;
	}
}
