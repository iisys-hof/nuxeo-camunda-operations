package de.hofuniversity.iisys.nuxeo.camunda.operations;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

@Operation(id = "Camunda.DeliverMessage", category = "CamundaWorkflows", label = "Deliver Message", description = "Deliver a message to Camunda")
public class DeliverMessageOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;

	@Context
    protected CoreSession fSession;
	
	@Param(name="workflowId")
	protected String fWorkflowId;
	
	@Param(name="messageName")
	protected String fMessageName;
	
	public DeliverMessageOperation()
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
			JSONObject parameters = new JSONObject();
			
			parameters.put("messageName", fMessageName);
			
			//TODO: how to handle on the other end?
			JSONObject correlationKeys = new JSONObject();
			correlationKeys.put("workflowId", fWorkflowId);
			parameters.put("correlationKeys", correlationKeys);
			
			//TODO: set any workflow variables?
			
			parameters.put("all", false);
			
			
			fUtil.deliverMessage(parameters);
		}
		catch(Exception e)
		{
			fLogger.error("failed to deliver message", e);
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
