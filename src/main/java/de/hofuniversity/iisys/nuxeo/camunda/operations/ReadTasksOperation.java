package de.hofuniversity.iisys.nuxeo.camunda.operations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

@Operation(id = "Camunda.ReadTasks", category = "CamundaWorkflows", label = "Read Camunda Tasks", description = "Reads active tasks for current workflows in Camunda")
public class ReadTasksOperation
{
	//TODO: authentication
	
	private final Logger fLogger = Logger.getLogger(this.getClass());

	private final CamundaUtil fUtil;
	
	@Context
    protected CoreSession fSession;
	
	public ReadTasksOperation()
	{
		CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		fUtil = new CamundaUtil(camundaUrl);
	}
	
	@OperationMethod
	public DocumentModel run(DocumentModel doc) throws Exception
	{
		try
		{
			//read process IDs available
			String[] processIds =
				(String[]) doc.getProperty("extWorkflows", "workflowId");
			
			//collect active task IDs for all workflows
			if(processIds != null && processIds.length > 0)
			{
				List<String> taskIds = new ArrayList<String>();
				
				for(String processId : processIds)
				{
					List<String> urlParams = new ArrayList<String>();
					urlParams.add("processInstanceId=" + processId);
					JSONArray tasks = fUtil.getTasks(urlParams);
					
					if(tasks.length() > 0)
					{
						for(int i = 0; i < tasks.length(); ++i)
						{
							JSONObject task = tasks.getJSONObject(i);
							taskIds.add(task.getString("id"));
						}
					}
				}
				
				//store
				String[] newTasks = taskIds.toArray(new String[taskIds.size()]);
				
				//only swap and store if changed
				if(!Arrays.equals((String[])
					doc.getProperty("extWorkflows", "taskId"), newTasks))
				{
					doc.setProperty("extWorkflows", "taskId", newTasks);
					fSession.saveDocument(doc);
				}
			}
		}
		catch(Exception e)
		{
			fLogger.error("failed to read tasks", e);
		}
		
		return doc;
	}
}
