package system;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;

import com.mendix.core.Core;
import com.mendix.core.component.LocalComponent;
import com.mendix.core.component.MxRuntime;
import com.mendix.integration.Integration;

@Component(immediate = true, properties = {"event.topics:String=com/mendix/events/model/loaded"})
public class UserActionsRegistrar implements EventHandler
{
	private MxRuntime mxRuntime;
	private LocalComponent component;
	private Integration integration;
	
	@Reference
	public void setMxRuntime(MxRuntime runtime)
	{
		mxRuntime = runtime;
		mxRuntime.bundleComponentLoaded();
	}
	
	@Reference
	public void setIntegration(Integration integration)
	{
		this.integration = integration;
	}
	
	@Override
	public void handleEvent(Event event)
	{
		if (event.getTopic().equals(com.mendix.core.event.EventConstants.ModelLoadedTopic()))        
		{
			component = mxRuntime.getMainComponent();
			Core.initialize(component, integration);   
			component.actionRegistry().registerUserAction(appcloudservices.actions.GenerateRandomPassword.class);
			component.actionRegistry().registerUserAction(appcloudservices.actions.LogOutUser.class);
			component.actionRegistry().registerUserAction(appcloudservices.actions.StartSignOnServlet.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.ExecuteQuery.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.ExecuteStatement.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetDbmsOutput.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterBoolean.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterDate.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterDecimal.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterLong.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterObject.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterRefCursor.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.GetOutParameterString.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.PlsqlCall.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.ResetParameters.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterBoolean.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterDateTime.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterDecimal.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterLong.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterObject.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetInParameterString.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersBoolean.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersDate.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersDecimal.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersLong.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersObject.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersRefCursor.class);
			component.actionRegistry().registerUserAction(oracleconnector.actions.SetOutParametersString.class);
			component.actionRegistry().registerUserAction(system.actions.VerifyPassword.class);
		}
	}
}