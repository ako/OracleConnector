package oracleconnector.impl;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.MendixRuntimeException;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlInParameterRefCursor extends SqlParameter {
    private String entityName = null;
    private List<IMendixObject> objectListResult;


    public SqlInParameterRefCursor(int index, String parameterDirection, String entityName) {

        super();
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setEntityName(entityName);
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
            throw new MendixRuntimeException("Ref cursor in parameters are not supported");
        } else {
            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.CURSOR);
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
                                    /*
                         * get cursor value
                         */
            OracleCallableStatement oraCallableStatement = callableStatement.unwrap(OracleCallableStatement.class);
            ResultSet rs = oraCallableStatement.getCursor(getParameterIndex());
            int colCount = rs.getMetaData().getColumnCount();
            java.util.List<IMendixObject> resultList = new ArrayList<IMendixObject>();
            while (rs.next()) {
                logNode.info("Result set record: " + rs);
                //IMendixObject obj = objectInstantiator.instantiate(context, outPar.getEntityName());
                IMendixObject obj = Core.instantiate(context, getEntityName());
                for (int c = 1; c <= colCount; c++) {
                    String colName = rs.getMetaData().getColumnName(c);
                    int colType = rs.getMetaData().getColumnType(c);
                    String colTypeName = rs.getMetaData().getColumnTypeName(c);
                    logNode.info(String.format("cursor col: %d, %s, %d, %s", c, colName, colType, colTypeName));
                    switch (colType) {
                        case OracleTypes.VARCHAR:
                            obj.setValue(context, colName, rs.getString(c));
                            break;
                        case OracleTypes.BIGINT:
                            obj.setValue(context, colName, rs.getLong(c));
                            break;
                        case OracleTypes.INTEGER:
                            obj.setValue(context, colName, rs.getInt(c));
                            break;
                        case OracleTypes.DECIMAL:
                            obj.setValue(context, colName, rs.getBigDecimal(c));
                            break;
                        case OracleTypes.DATE:
                            obj.setValue(context, colName, rs.getDate(c));
                            break;
                        default:
                            obj.setValue(context, colName, rs.getObject(c));
                    }
                }
                resultList.add(obj);
            }
            this.setObjectListResult(resultList);
            rs.close();

        }
    }

    @Override
    public List<IMendixObject> getResultValue(Class type) throws SQLException {
        return this.getObjectListResult();
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<IMendixObject> getObjectListResult() {
        return objectListResult;
    }

    public void setObjectListResult(List<IMendixObject> objectListResult) {
        this.objectListResult = objectListResult;
    }

}
