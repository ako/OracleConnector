package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import oracle.jdbc.OracleTypes;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlInParameterDecimal extends SqlParameter {
    private BigDecimal decimalValue = null;
    private BigDecimal decimalResult = null;

    public SqlInParameterDecimal(int index , String parameterDirection, BigDecimal decimalValue) {
        super();
        setParameterType(super.DECIMAL_TYPE);
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setDecimalValue(decimalValue);
    }

    public SqlInParameterDecimal(int index, String parameterDirection) {
        setParameterDirection(parameterDirection);
        setParameterIndex(index);
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
            callableStatement.setBigDecimal(getParameterIndex(), getDecimalValue());
        } else {
            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.DECIMAL);
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
            BigDecimal decimalValue = callableStatement.getBigDecimal(getParameterIndex());
            setDecimalResult(decimalValue);
            logNode.info("Decimal value = " + decimalValue);
        }
    }

    @Override
    public BigDecimal getResultValue(Class type) throws SQLException {
        logNode.info("getResultValue");
        return getDecimalResult();
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public BigDecimal getDecimalResult() {
        return decimalResult;
    }

    public void setDecimalResult(BigDecimal decimalResult) {
        this.decimalResult = decimalResult;
    }
}
