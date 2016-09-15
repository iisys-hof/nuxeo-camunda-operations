package de.hofuniversity.iisys.nuxeo.camunda.operations;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;

import de.hofuniversity.iisys.nuxeo.camunda.runners.AddWorkflowRunner;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;
import de.hofuniversity.iisys.nuxeo.camunda.util.IisysConstants;

@Operation(id = "Camunda.StartWorkflow", category = "CamundaWorkflows", label = "Start Camunda Workflow", description = "Start a new workflow in Camunda")
public class StartWorkflowOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;

	private final boolean fBlockConcurrWorkflows;
	
	@Context
    protected CoreSession fSession;
	
	/**
	 * Camunda Workflow ID
	 */
	@Param(name="workflowId")
	protected String fWorkflowId;
	
	/**
	 * Nuxeo workflow ID
	 */
	@Param(name="workflowInstanceId", required=false)
	protected String fWorkflowInstanceId;

	@Param(name="camundaParams", required=false)
	protected HashMap<String, Object> fCamundaParams;
	
	
	public StartWorkflowOperation()
	{
		CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		
		String blockConcWfsString = config.getConfiguration().get(
				CamundaConfig.BLOCK_CONC_WORKFLOWS);
		fBlockConcurrWorkflows = Boolean.parseBoolean(blockConcWfsString);
		
		fUtil = new CamundaUtil(camundaUrl);
	}
	
	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws Exception
	{
		try
		{
			String[] currentWorfklows = (String[])
					doc.getProperty("extWorkflows", "workflowId");
			
			if(fBlockConcurrWorkflows
				&& currentWorfklows.length > 0)
			{
				throw new Exception("start blocked - multiple workflows disabled");
			}
			
			//start process
			//set parameters
			JSONObject parameters = new JSONObject();
			
			JSONObject variables = new JSONObject();
			
			
			//read all additional parameters
			if(fCamundaParams != null && !fCamundaParams.isEmpty())
			{
				String key = null;
				for(Entry<String, Object> entry : fCamundaParams.entrySet())
				{
					key = entry.getKey();
					
					//previously "documentId"
					if(!IisysConstants.DOCUMENT_ID.equals(key)
						&& !"workflowId".equals(key))
					{
						JSONObject addVar = new JSONObject();
						
						//TODO: support for other types
						addVar.put("type", "String");
						addVar.put("value", entry.getValue());
						
						variables.put(key, addVar);
					}
				}
			}
			
			JSONObject docVar = new JSONObject();
			docVar.put("type", "String");
			docVar.put("value", doc.getId());
			//previously "documentId"
			variables.put(IisysConstants.DOCUMENT_ID, docVar);
			
			docVar = new JSONObject();
			docVar.put("type", "String");
			docVar.put("value", fWorkflowInstanceId);
			variables.put(IisysConstants.WORKFLOW_ID, docVar);
			
			// add principal ID as the workflow's initiator
			Principal principal = fSession.getPrincipal();
			if(principal != null)
			{
				docVar = new JSONObject();
				docVar.put("type", "String");
				docVar.put("value", principal.getName());
				variables.put(IisysConstants.WORKFLOW_INITIATOR, docVar);
			}
			
			parameters.put("variables", variables);
			
			//start workflow
			JSONObject response = fUtil.startProcessInstance(fWorkflowId,
					parameters);
			String processId = null;
			String[] taskIds = null;
			
			if(response.has("id"))
			{
				processId = response.getString("id");
				
				// manipulate document with system permissions
				new AddWorkflowRunner(fSession, doc, currentWorfklows,
						processId, taskIds).runUnrestricted();
			}
			
			//TODO: update IDs and status from here or from camunda workflow service task?
			
			fLogger.info("Nuxeo Workflow ID: " + fWorkflowInstanceId);
		}
		catch(Exception e)
		{
			fLogger.error("failed to start workflow", e);
			throw e;
		}
		
		fSession.save();
		
		return doc;
	}
	
	public void setWorkflowId(String id)
	{
		fWorkflowId = id;
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
