package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlInParameterLong extends SqlParameter {

    private Long longValue = null;
    private Long longResult = null;

    public SqlInParameterLong(int index, String parameterDirection, Long longValue) {
        setParameterType(super.LONG_TYPE);
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setLongValue(longValue);
    }

    public SqlInParameterLong(int index, String parameterDirection) {
        setParameterDirection(parameterDirection);
        setParameterIndex(index);
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
            callableStatement.setLong(getParameterIndex(), getLongValue());
        } else {
            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.NUMERIC);
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
            Long longValue = callableStatement.getLong(getParameterIndex());
            setLongResult(longValue);
            logNode.info("Long value = " + longValue);
        }
    }

    @Override
    public Long getResultValue(Class type) throws SQLException {
        logNode.info("getResultValue");
        return getLongResult();
    }
    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Long getLongResult() {
        return longResult;
    }

    public void setLongResult(Long longResult) {
        this.longResult = longResult;
    }

}
