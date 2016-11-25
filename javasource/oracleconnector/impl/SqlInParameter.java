package oracleconnector.impl;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.logging.impl.LogNode;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.util.Date;

/**
 * Created by ako on 18-11-2016.
 */
public abstract class SqlInParameter extends SqlParameter {
    public String getSqlType() {
        return sqlType;
    }

    public void setSqlType(String sqlType) {
        this.sqlType = sqlType;
    }

    private String sqlType;
    private BigDecimal decimalValue = null;
    private Date dateTimeValue = null;
    private Boolean booleanValue = null;
    private Long longValue = null;

    public SqlInParameter() {
    }

    public IMendixObject getMxObjectValue() {
        return mxObjectValue;
    }

    public void setMxObjectValue(IMendixObject mxObjectValue) {
        this.mxObjectValue = mxObjectValue;
    }

    private IMendixObject mxObjectValue = null;

    public SqlInParameter(BigDecimal decimalValue) {
        setParameterType(super.DECIMAL_TYPE);
        this.decimalValue = decimalValue;
    }

    /*
    public SqlInParameter(String stringValue) {
        setParameterType(super.STRING_TYPE);
        this.stringValue = stringValue;
    }
*/
    public SqlInParameter(Date dateTimeValue) {
        setParameterType(super.DATE_TIME_TYPE);
        this.dateTimeValue = dateTimeValue;
    }

    public SqlInParameter(Boolean booleanValue) {
        setParameterType(super.BOOLEAN_TYPE);
        this.booleanValue = booleanValue;
    }

    public SqlInParameter(Long longValue) {
        setParameterType(super.LONG_TYPE);
        this.longValue = longValue;
    }

    public SqlInParameter(IMendixObject objectValue, String sqlType) {
        setParameterType(super.OBJECT_TYPE);
        this.mxObjectValue = objectValue;
        this.sqlType = sqlType;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setLongValue(Long longValue) {
        this.longValue = longValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public Date getDateTimeValue() {
        return dateTimeValue;
    }

    public void setDateTimeValue(Date dateTimeValue) {
        this.dateTimeValue = dateTimeValue;
    }


    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }





}
