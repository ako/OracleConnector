package oracleconnector.impl;

import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.ArrayDescriptor;
import oracle.sql.StructDescriptor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by ako on 26-11-2016.
 */
public class SqlParameterObjectArray extends SqlParameter {
    private String sqlTypeName = null;
    private String entityName = null;
    private List<IMendixObject> objectArrayValue = null;
    private List<IMendixObject> objectArrayResult = null;

    public SqlParameterObjectArray(int index, String parameterDirection, List<IMendixObject> objectArrayValue, String sqlType) {
        super();
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setObjectArrayValue(objectArrayValue);
        setSqlTypeName(sqlType);
    }

    public SqlParameterObjectArray(int index, String parameterDirection, String entity, String sqlType) {
        setParameterIndex(index);
        setParameterDirection(parameterDirection);
        setSqlTypeName(sqlType);
        setEntityName(entity);
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        OracleCallableStatement ocall = callableStatement.unwrap(OracleCallableStatement.class);
        if (isInParameter()) {
            StructDescriptor structdesc = StructDescriptor.createDescriptor
                    (this.getSqlTypeName(), (OracleConnection) callableStatement.getConnection().unwrap(OracleConnection.class));
            Object[] obj1 = {"user1", 1, new Date()};
            Object[] obj2 = {"user2", 2, new Date()};
            Object[] objs = {obj1,obj2};
            //new oracle.sql.ARRAY()
            ocall.setPlsqlIndexTable(getParameterIndex(), objs, 2, 2, OracleTypes.STRUCT, 0);
        } else {
            ocall.registerIndexTableOutParameter(1, 30, OracleTypes.VARCHAR, 2000);
        }

    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {

    }

    @Override
    public <T> T getResultValue(Class<T> type) throws SQLException {
        return null;
    }

    public String getSqlTypeName() {
        return sqlTypeName;
    }

    public void setSqlTypeName(String sqlTypeName) {
        this.sqlTypeName = sqlTypeName;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public List<IMendixObject> getObjectArrayValue() {
        return objectArrayValue;
    }

    public void setObjectArrayValue(List<IMendixObject> objectArrayValue) {
        this.objectArrayValue = objectArrayValue;
    }

    public List<IMendixObject> getObjectArrayResult() {
        return objectArrayResult;
    }

    public void setObjectArrayResult(List<IMendixObject> objectArrayResult) {
        this.objectArrayResult = objectArrayResult;
    }
}
