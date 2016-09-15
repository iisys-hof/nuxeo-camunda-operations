package de.hofuniversity.iisys.nuxeo.camunda.runners;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

public class AddWorkflowRunner extends UnrestrictedSessionRunner
{
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;

	private final boolean fConcurrWorkflows;
	
	private final DocumentModel fDoc;

	private final String[] fCurrentWorkflows;
	private final String fProcessId;
	private String[] fTaskIds;
	
	public AddWorkflowRunner(CoreSession session, DocumentModel doc,
			String[] currentWorkflows, String processId, String[] taskIds)
	{
        super(session);
        
        CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		
		String concWfsString = config.getConfiguration().get(
				CamundaConfig.CONC_WORKFLOWS);
		fConcurrWorkflows = Boolean.parseBoolean(concWfsString);
		
		fUtil = new CamundaUtil(camundaUrl);
        
        fDoc = doc;
        
        fCurrentWorkflows = currentWorkflows;
        fProcessId = processId;
        fTaskIds = taskIds;
    }

	@Override
	public void run() throws ClientException
	{
		//save ID and status in document
		String[] value = null;
		
		try
		{
			if(fConcurrWorkflows)
			{
				value = Arrays.copyOf(fCurrentWorkflows,
						fCurrentWorkflows.length + 1);
				value[fCurrentWorkflows.length] = fProcessId;
			}
			else
			{
				value = new String[1];
				value[0] = fProcessId;
			}
			
			fDoc.setProperty("extWorkflows", "workflowId", value);
			
			//acquire and store active task IDs
			List<String> urlParams = new ArrayList<String>();
			urlParams.add("processInstanceId=" + fProcessId);
			JSONArray tasks = fUtil.getTasks(urlParams);
			
			if(tasks.length() > 0)
			{
				int offset = 0;
				
				if(fConcurrWorkflows)
				{
					String[] currentTasks = (String[])
						fDoc.getProperty("extWorkflows", "taskId");
					offset = currentTasks.length;
					
					fTaskIds = Arrays.copyOf(currentTasks,
							tasks.length() + currentTasks.length);
				}
				else
				{
					fTaskIds = new String[tasks.length()];
				}
				
				for(int i = 0; i < tasks.length(); ++i)
				{
					JSONObject task = tasks.getJSONObject(i);
					fTaskIds[i + offset] = task.getString("id");
				}
				
				fDoc.setProperty("extWorkflows", "taskId", fTaskIds);
			}
			
			// session from superclass
			session.saveDocument(fDoc);
			session.save();
			
	
			fLogger.info("Camunda Workflow ID: " + fProcessId);
			if(fTaskIds != null)
			{
				String idString = "";
				for(String id : fTaskIds)
				{
					idString += id + " ";
				}
				
				fLogger.info("Camunda Tasks IDs: " + idString);
			}
		}
		catch(Exception e)
		{
			throw new ClientException(e);
		}
	}
}
