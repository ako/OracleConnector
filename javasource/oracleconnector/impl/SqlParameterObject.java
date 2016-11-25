package oracleconnector.impl;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import oracle.jdbc.driver.OracleConnection;
import oracle.sql.StructDescriptor;

import java.sql.CallableStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ako on 23-11-2016.
 */
public class SqlParameterObject extends SqlParameter {

    private String sqlTypeName = null;
    private String entityName = null;
    private IMendixObject objectValue;


    public SqlParameterObject(int index, String directionIn, IMendixObject objectValue, String sqlType) {
        super();
        setParameterIndex(index);
        setParameterDirection(directionIn);
        setObjectValue(objectValue);
        setSqlTypeName(sqlType);
    }

    public SqlParameterObject(int index, String directionOut, String resultEntity, String sqlTypeName) {
        super();
        setParameterIndex(index);
        setParameterDirection(directionOut);
        setEntityName(resultEntity);
        setSqlTypeName(sqlTypeName);
    }

    @Override
    public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException {
        logNode.info("prepareCall");
        if (isInParameter()) {
//                        // Define a struct
            IMendixObject mxObj = this.getObjectValue();
            String sqlType = getSqlTypeName();
            logNode.info(String.format("Creating struct for %s, %s", mxObj, sqlType));
            List<? extends IMendixObjectMember<?>> objPrimitives = mxObj.getPrimitives(context);
//

            StructDescriptor structdesc = StructDescriptor.createDescriptor
                    (this.getSqlTypeName(), (OracleConnection) callableStatement.getConnection().unwrap(OracleConnection.class));
            int structColumnCount = structdesc.getMetaData().getColumnCount();
            int attributeCount = objPrimitives.size();
            if (attributeCount != structColumnCount) {
                logNode.warn(String.format("SQL object and entity have different attribute counts, %d != %d", structColumnCount, attributeCount));
            }
            logNode.info(String.format("attribute count %d", structColumnCount));
//
            Object[] attrVals = new Object[attributeCount];
            Object[] objects = objPrimitives.toArray();
//
            for (int mxa = 0; mxa < structColumnCount; mxa++) {
                //IMendixObjectMember mem = ((IMendixObjectMember) objects[mxa]);
                String colName = structdesc.getMetaData().getColumnName(mxa + 1);
                IMendixObjectMember mem = mxObj.getMember(context, colName);
//
                logNode.info(String.format("getting attribute: %d, %s, %s", mxa,
                        mem.getValue(context).getClass().getName(),
                        mem.getValue(context)));
//
                if (mem.getValue(context) instanceof java.util.Date) {
                    attrVals[mxa] = new java.sql.Date(((Date) mem.getValue(context)).getTime());
//
                } else {
                    attrVals[mxa] = mem.getValue(context);

                }
            }
            logNode.info(String.format("Struct array: %s", Arrays.toString(attrVals)));
            //Struct objStruct = connection.createStruct(sqlType, attrVals);
            //callableStatement.setObject(i, objStruct);
            OracleConnection oraConn = callableStatement.getConnection().unwrap(OracleConnection.class);
            StructDescriptor itemDescriptor =
                    StructDescriptor.createDescriptor(sqlType, oraConn);
            oracle.sql.STRUCT obj = new oracle.sql.STRUCT(itemDescriptor, oraConn, attrVals);
            OracleCallableStatement oraStmt = callableStatement.unwrap(OracleCallableStatement.class);
            oraStmt.setSTRUCT(getParameterIndex(), obj);

        } else {

            callableStatement.registerOutParameter(getParameterIndex(), OracleTypes.STRUCT, this.getSqlTypeName().toUpperCase());
        }
    }

    @Override
    public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException {

        logNode.info("retrieveResult: " + getParameterIndex());
        if (isOutParameter()) {
//
            OracleConnection connection = callableStatement.getConnection().unwrap(OracleConnection.class);
            StructDescriptor structdesc = StructDescriptor.createDescriptor
                    (this.getSqlTypeName(), connection);
            logNode.info(String.format("struct desc attr names: %s, %s", this.getSqlTypeName(), structdesc.getMetaData().getColumnName(1)));
//
//
            oracle.sql.STRUCT sqlObj = (oracle.sql.STRUCT) callableStatement.getObject(getParameterIndex());
//                        //AttributeDescriptor[] names = sqlObj.getDescriptor().getAttributesDescriptor();
            int nameCount = structdesc.getMetaData().getColumnCount();
//                        //logNode.info("names = " + Arrays.toString(names));
            Object[] values = sqlObj.getAttributes();
            logNode.info("Object: " + sqlObj);
            // IMendixObject obj = objectInstantiator.instantiate(context, outPar.getEntityName());
            IMendixObject obj = Core.instantiate(context, getEntityName());
            for (int sn = 0; sn < nameCount; sn++) {
                String name = structdesc.getMetaData().getColumnName(sn + 1);
                logNode.info(String.format("struct attr: %s, %s", name, values[sn]));
                obj.setValue(context, name, values[sn]);
            }
            setObjectValue(obj);


            //   String stringValue = callableStatement.getString(getParameterIndex());
            //   setStringResult(stringValue);
            //    logNode.info("String value = " + stringValue);

        }
    }

    @Override
    public IMendixObject getResultValue(Class type) throws SQLException {
        logNode.info("getResultValue");
        return getObjectValue();
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

    public IMendixObject getObjectValue() {
        return objectValue;
    }

    public void setObjectValue(IMendixObject objectValue) {
        this.objectValue = objectValue;
    }
}
