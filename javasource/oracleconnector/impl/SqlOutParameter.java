package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by ako on 18-11-2016.
 */
public class SqlOutParameter extends SqlParameter {

    private String stringValue = null;
    private String sqlTypeName = null;
    private String entityName = null;
    private List<IMendixObject> objectListResult;
    private Date dateTimeValue;
    private BigDecimal decimalValue;
    private Long longValue;
    private IMendixObject objectValue;


    /**
     *
     * @param typeName - type of out parameter: STRING_TYPE, DATE_TYPE, etc. @see SqlParameter
     * @param entityName - result entity name
     * @param sqlTypeName - name of sql user defined type
     */
    public SqlOutParameter(String typeName, String entityName, String sqlTypeName) {
        setParameterType(typeName);
        this.entityName = entityName;
        this.sqlTypeName = sqlTypeName;
    }

    public SqlOutParameter(String typeName, String entityName) {
        setParameterType(typeName);
        this.entityName = entityName;
    }

    public SqlOutParameter(String typeName) {
        setParameterType(typeName);
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.stringValue = stringValue;
    }


    public String getSqlTypeName() {
        return sqlTypeName;
    }


    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }


    public void setObjectListResult(List<IMendixObject> objectListResult) {
        this.objectListResult = objectListResult;
    }

    public List<IMendixObject> getCursorList() {
        return this.objectListResult;
    }

    public void setDateTimeValue(Date dateValue) {
        this.dateTimeValue = dateValue;
    }


    public void setLongValue(Long aLong) {
        this.longValue = aLong;
    }

    public void setDecimalValue(BigDecimal decimalValue) {
        this.decimalValue = decimalValue;
    }

    public IMendixObject getObjectValue() {

        return this.objectValue;
    }
    public Date getDateTimeValue() {
        return dateTimeValue;
    }

    public BigDecimal getDecimalValue() {
        return decimalValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public void setObjectValue(IMendixObject objectValue) {
        this.objectValue = objectValue;
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {

    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {

    }

    @Override
    public <T> T getResultValue(Class<T> type) throws SQLException {
        return null;
    }
}