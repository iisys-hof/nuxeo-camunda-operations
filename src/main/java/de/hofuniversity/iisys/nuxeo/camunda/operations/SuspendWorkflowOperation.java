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

@Operation(id = "Camunda.SuspendWorkflow", category = "CamundaWorkflows", label = "Suspend Workflow", description = "Suspend a workflow in Camunda")
public class SuspendWorkflowOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;
	
	@Context
    protected CoreSession fSession;
	
	@Param(name="workflowId")
	protected String fWorkflowId;
	
	public SuspendWorkflowOperation()
	{
		CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		fUtil = new CamundaUtil(camundaUrl);
	}
	
	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws Exception
	{
		//TODO: possibly read parameters from document metadata?
		
		try
		{
			fUtil.suspendProcessById(fWorkflowId);
		}
		catch(Exception e)
		{
			fLogger.error("failed to suspend workflow", e);
			throw e;
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
