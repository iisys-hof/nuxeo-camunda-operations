package de.hofuniversity.iisys.nuxeo.camunda.operations;

import org.apache.log4j.Logger;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

@Operation(id = "Camunda.DeleteWorkflow", category = "CamundaWorkflows", label = "Delete Workflow", description = "Delete a workflow in Camunda")
public class DeleteWorkflowOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;
	
	@Context
    protected CoreSession session;
	
	@Param(name="workflowId")
	protected String fWorkflowId;
	
	public DeleteWorkflowOperation()
	{
		CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		fUtil = new CamundaUtil(camundaUrl);
	}
	
	@OperationMethod
	public DocumentModel run(DocumentModel doc)
	{
		//TODO: possibly read parameters from document metadata?
		
		try
		{
			fUtil.deleteProcessInstance(fWorkflowId);
			
			//TODO: remove workflow from document metadata
		}
		catch(Exception e)
		{
			fLogger.error("failed to delete workflow", e);
		}
		
		return doc;
	}
	

	
	@OperationMethod
	public DocumentModelList run(DocumentModelList list) throws Exception
	{
		DocumentModel doc = list.get(0);
		
		run(doc);
		
		//TODO: pass on multiple document IDs?
		
		return list;
	}
}
