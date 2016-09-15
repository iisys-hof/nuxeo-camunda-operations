package de.hofuniversity.iisys.nuxeo.camunda.operations;

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

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

@Operation(id = "Camunda.CompleteTask", category = "CamundaWorkflows", label = "Complete Task", description = "Complete a workflow task in Camunda")
public class CompleteTaskOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;

	@Context
    protected CoreSession fSession;
	
	@Param(name="taskId")
	protected String fTaskId;

	@Param(name="camundaParams", required=false)
	protected HashMap<String, Object> fCamundaParams;
	
	public CompleteTaskOperation()
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
			//TODO: pass variables from document metadata?
			//set parameters
			JSONObject parameters = new JSONObject();
			
			JSONObject variables = new JSONObject();
			
			//read all additional parameters
			if(fCamundaParams != null && !fCamundaParams.isEmpty())
			{
				for(Entry<String, Object> entry : fCamundaParams.entrySet())
				{
					JSONObject addVar = new JSONObject();
					
					//TODO: support for other types
					addVar.put("type", "String");
					addVar.put("value", entry.getValue());
					
					variables.put(entry.getKey(), addVar);
				}
			}

			parameters.put("variables", variables);
			
			fUtil.completeTask(fTaskId, parameters);

			//TODO: refresh tasks?
		}
		catch(Exception e)
		{
			fLogger.error("failed to advance complete task", e);
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
