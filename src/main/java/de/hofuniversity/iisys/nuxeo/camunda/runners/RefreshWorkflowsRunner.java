package de.hofuniversity.iisys.nuxeo.camunda.runners;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;

import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaConfig;
import de.hofuniversity.iisys.nuxeo.camunda.util.CamundaUtil;

public class RefreshWorkflowsRunner extends UnrestrictedSessionRunner
{
	private final Logger fLogger = Logger.getLogger(this.getClass());
	
	private final CamundaUtil fUtil;
	
	private final boolean fClearStale;
	
	private final DocumentModel fDoc;

	public RefreshWorkflowsRunner(CoreSession session, DocumentModel doc)
	{
		super(session);
		
		CamundaConfig config = CamundaConfig.getInstance();
		String camundaUrl = config.getConfiguration().get(CamundaConfig.CAMUNDA_URL);
		
		String staleString = config.getConfiguration().get(
				CamundaConfig.CLEAR_STALE_WFS);
		fClearStale = Boolean.parseBoolean(staleString);
		
		fUtil = new CamundaUtil(camundaUrl);
		
		fDoc = doc;
	}

	@Override
	public void run() throws ClientException
	{
		try
		{
			boolean changed = false;
			
			//read process IDs available
			String[] processIds =
				(String[]) fDoc.getProperty("extWorkflows", "workflowId");

			if(processIds != null && processIds.length > 0)
			{
				Set<String> staleProcesses = new HashSet<String>();
				
				//check for stale workflows
				for(String processId : processIds)
				{
					try
					{
						fUtil.getProcessInstance(processId);
					}
					catch(FileNotFoundException e)
					{
						staleProcesses.add(processId);
					}
				}
				
				//remove stale IDs from array
				if(fClearStale && !staleProcesses.isEmpty())
				{
					int index = 0;
					String[] newIds = new String[processIds.length - staleProcesses.size()];
					
					for(String procId : processIds)
					{
						if(!staleProcesses.contains(procId))
						{
							newIds[index++] = procId;
						}
					}
					
					fDoc.setProperty("extWorkflows", "workflowId", newIds);
					processIds = newIds;
					
					changed = true;
				}
				
				//collect active task IDs for all workflows
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
					fDoc.getProperty("extWorkflows", "taskId"), newTasks))
				{
					fDoc.setProperty("extWorkflows", "taskId", newTasks);
					changed = true;
				}
				
				if(changed)
				{
					session.saveDocument(fDoc);
				}
				session.save();
			}
		}
		catch(Exception e)
		{
			fLogger.error("failed to refresh workflows", e);
		}
	}

}
