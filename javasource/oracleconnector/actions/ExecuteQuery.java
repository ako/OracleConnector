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
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.webui.CustomJavaAction;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oracleconnector.impl.JdbcConnector;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ExecuteQuery extends CustomJavaAction<java.util.List<IMendixObject>>
{
	private java.lang.String jdbcUrl;
	private java.lang.String userName;
	private java.lang.String password;
	private java.lang.String sql;
	private java.lang.String resultObject;

	public ExecuteQuery(IContext context, java.lang.String jdbcUrl, java.lang.String userName, java.lang.String password, java.lang.String sql, java.lang.String resultObject)
	{
		super(context);
		this.jdbcUrl = jdbcUrl;
		this.userName = userName;
		this.password = password;
		this.sql = sql;
		this.resultObject = resultObject;
	}

	@Override
	public java.util.List<IMendixObject> executeAction() throws Exception
	{
		// BEGIN USER CODE
		IMetaObject metaObject = Core.getMetaObject(this.resultObject);
		Stream<IMendixObject> resultStream = connector.executeQuery(
				this.jdbcUrl, this.userName, this.password, metaObject, this.sql, this.getContext());
		List<IMendixObject> resultList = resultStream.collect(Collectors.toList());
		logNode.trace(String.format("Result list count: %d", resultList.size()));

		return resultList;
		// END USER CODE
	}

	/**
	 * Returns a string representation of this action
	 */
	@Override
	public java.lang.String toString()
	{
		return "ExecuteQuery";
	}

	// BEGIN EXTRA CODE
	private final ILogNode logNode = Core.getLogger(JdbcConnector.LOGNAME);
	private final JdbcConnector connector = new JdbcConnector(logNode);
	// END EXTRA CODE
}
