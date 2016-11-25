package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Date;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlParameterDateTime extends SqlParameter {
    private Date dateValue = null;
    private Date dateResult = null;

    public SqlParameterDateTime(int index, String parameterDirection, Date dateTimeValue) {
        super();
        setParameterType(super.DATE_TIME_TYPE);
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setDateValue(dateTimeValue);
    }

    public SqlParameterDateTime(int index, String parameterDirection) {
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
    }

    public Date getDateValue() {
        return dateValue;
    }

    public void setDateValue(Date dateValue) {
        this.dateValue = dateValue;
    }

    public Date getDateResult() {
        return dateResult;
    }

    public void setDateResult(Date dateResult) {
        this.dateResult = dateResult;
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
            callableStatement.setDate(getParameterIndex(), new java.sql.Date(getDateValue().getTime()));
        } else {
            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.DATE);
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
            Date dateValue = callableStatement.getDate(getParameterIndex());
            setDateResult(dateValue);
            logNode.info("Date time value = " + dateValue);
        }
    }

    @Override
    public Date getResultValue(Class type) throws SQLException {
        logNode.info("getResultValue");
        return getDateResult();
    }

}
