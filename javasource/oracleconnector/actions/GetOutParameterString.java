// This file was generated by Mendix Modeler.
//
// WARNING: Only the following code will be retained when actions are regenerated:
// - the import list
// - the code between BEGIN USER CODE and END USER CODE
// - the code between BEGIN EXTRA CODE and END EXTRA CODE
// Other code you write will be lost the next time you deploy the project.
// Special characters, e.g., é, ö, à, etc. are supported in comments.

package oracleconnector.actions;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.webui.CustomJavaAction;
import oracleconnector.impl.JdbcConnector;

public class GetOutParameterString extends CustomJavaAction<java.lang.String>
{
	private java.lang.Long ParameterIndex;

	public GetOutParameterString(IContext context, java.lang.Long ParameterIndex)
	{
		super(context);
		this.ParameterIndex = ParameterIndex;
	}

	@Override
	public java.lang.String executeAction() throws Exception
	{
		// BEGIN USER CODE
		//throw new com.mendix.systemwideinterfaces.MendixRuntimeException("Java action was not implemented");
		return connector.getStringParameter(this.ParameterIndex);
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "GetOutParameterString";
	}

	// BEGIN EXTRA CODE
	private final ILogNode logNode = Core.getLogger(this.getClass().getName());
	private final JdbcConnector connector = new JdbcConnector(logNode);
	// END EXTRA CODE
}
