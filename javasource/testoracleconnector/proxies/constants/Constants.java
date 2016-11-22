// This file was generated by Mendix Modeler 6.10.
//
// WARNING: Code you write here will be lost the next time you deploy the project.

package testoracleconnector.proxies.constants;

import com.mendix.core.Core;

public class Constants
{
	// These are the constants for the TestOracleConnector module

	public static java.lang.String getORACLE_JDBC_URL()
	{
		return (java.lang.String)Core.getConfiguration().getConstantValue("TestOracleConnector.ORACLE_JDBC_URL");
	}

	public static java.lang.String getORACLE_PASSWORD()
	{
		return (java.lang.String)Core.getConfiguration().getConstantValue("TestOracleConnector.ORACLE_PASSWORD");
	}

	public static java.lang.String getORACLE_USERNAME()
	{
		return (java.lang.String)Core.getConfiguration().getConstantValue("TestOracleConnector.ORACLE_USERNAME");
	}
}