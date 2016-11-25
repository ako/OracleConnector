package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlInParameterString extends SqlInParameter {

    private String stringValue = null;
    private String stringResult = null;

    public SqlInParameterString(int index, String parameterDirection, String stringValue) {
        super();
        setParameterType(super.STRING_TYPE);
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setStringValue(stringValue);
    }

    public SqlInParameterString(int index, String parameterDirection) {
        setParameterDirection(parameterDirection);
        setParameterIndex(index);
    }


    public String getStringResult() {
        return stringResult;
    }

    public void setStringResult(String stringResult) {
        this.stringResult = stringResult;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }

    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
            callableStatement.setString(getParameterIndex(), getStringValue());
        } else {
            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.VARCHAR);
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
            String stringValue = callableStatement.getString(getParameterIndex());
            setStringResult(stringValue);
            logNode.info("String value = " + stringValue);
        }
    }

    @Override
    public String getResultValue(Class type) throws SQLException {
        logNode.info("getResultValue");
        return getStringResult();
    }
}
