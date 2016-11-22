package oracleconnector.impl;


import com.mendix.core.objectmanagement.member.MendixHashString;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.IMendixObjectMember;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracle.sql.AttributeDescriptor;
import oracle.sql.StructDescriptor;
import oracleconnector.interfaces.ConnectionManager;
import oracleconnector.interfaces.ObjectInstantiator;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JdbcConnector implements the execute query (and execute statement) functionality, and returns a {@link Stream} of {@link IMendixObject}s.
 */
public class JdbcConnector {
    private final ILogNode logNode;
    private final ObjectInstantiator objectInstantiator;
    private final ConnectionManager connectionManager;

    public JdbcConnector(final ILogNode logNode, final ObjectInstantiator objectInstantiator, final ConnectionManager connectionManager) {
        this.logNode = logNode;
        this.objectInstantiator = objectInstantiator;
        this.connectionManager = connectionManager;
    }

    public JdbcConnector(final ILogNode logNode) {
        this(logNode, new ObjectInstantiatorImpl(), ConnectionManagerSingleton.getInstance());
    }

    public Stream<IMendixObject> executeQuery(final String jdbcUrl, final String userName, final String password, final IMetaObject metaObject, final String sql,
                                              final IContext context) throws SQLException {
        String entityName = metaObject.getName();
        logNode.info("metaObject: " + metaObject);
        logNode.info("entityName: " + entityName);
        Function<Map<String, Optional<Object>>, IMendixObject> toMendixObject = columns -> {
            IMendixObject obj = objectInstantiator.instantiate(context, entityName);
            logNode.info("obj: " + obj);

            BiConsumer<String, Optional<Object>> setMemberValue = (name, value) -> {
                logNode.info(" - metaObject: " + metaObject);
                logNode.info(" - metaPrimitive: " + metaObject.getMetaPrimitive(name));

                try {
                    final PrimitiveType type = metaObject.getMetaPrimitive(name).getType();
                    // convert to suitable value (different for Binary type)
                    Function<Object, Object> toSuitableValue = toSuitableValue(type);
                    // for Boolean type, convert null to false
                    Supplier<Object> defaultValue = () -> type == PrimitiveType.Boolean ? Boolean.FALSE : null;
                    // apply two functions declared above
                    Object convertedValue = value.map(toSuitableValue).orElseGet(defaultValue);
                    // update object with converted value
                    if (type == PrimitiveType.HashString)
                        ((MendixHashString) obj.getMember(context, name)).setInitialHash((String) convertedValue);
                    else
                        obj.setValue(context, name, convertedValue);
                } catch (Exception e) {
                    logNode.warn("Cannot find meta primitive for " + name);
                }
            };

            columns.forEach(setMemberValue);
            logNode.trace("Instantiated object: " + obj);
            return obj;
        };

        return executeQuery(jdbcUrl, userName, password, metaObject, sql).map(toMendixObject);
    }

    private Function<Object, Object> toSuitableValue(final PrimitiveType type) {
        return v -> type == PrimitiveType.Binary ? new ByteArrayInputStream((byte[]) v) : v;
    }

    private Stream<Map<String, Optional<Object>>> executeQuery(final String jdbcUrl, final String userName, final String password, final IMetaObject metaObject, final String sql) throws SQLException {
        logNode.trace(String.format("executeQuery: %s, %s, %s", jdbcUrl, userName, sql));

        try (Connection connection = connectionManager.getConnection(jdbcUrl, userName, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            ResultSetReader resultSetReader = new ResultSetReader(resultSet, metaObject);

            return resultSetReader.readAll().stream();
        }
    }

    public long executeStatement(final String jdbcUrl, final String userName, final String password, final String sql) throws SQLException {
        logNode.trace(String.format("executeStatement: %s, %s, %s", jdbcUrl, userName, sql));

        try (Connection connection = connectionManager.getConnection(jdbcUrl, userName, password);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            return preparedStatement.executeUpdate();
        }
    }

    public String getDbmsOutput(String jdbcUrl, String userName, String password) throws SQLException {
        logNode.info(String.format("getDbmsOutput: %s, %s", jdbcUrl, userName));
        String dbmsLines = null;
        String sql =
                "declare \n" +
                        "   TYPE lines_tab_type IS TABLE OF varchar2(2000) INDEX BY BINARY_integer; \n" +
                        "   r_lines lines_tab_type; \n" +
                        "   l_lines dbms_output.chararr; \n" +
                        "   l_num_lines number := 10; \n" +
                        "   r_vlines varchar2(32767) := ''; \n" +
                        "begin \n" +
                        "   dbms_output.enable(10000); \n" +
                        "   dbms_output.get_lines(l_lines, l_num_lines); \n" +
                        "   r_vlines := r_vlines || '...' || l_num_lines;\n " +
                        "   for v_counter in 1..l_num_lines loop \n" +
                        "      r_lines(v_counter) := l_lines(v_counter); \n" +
                        "      r_vlines := r_vlines || '\n ' || v_counter || ', ' || L_lines(v_counter); \n" +
                        "   end loop; \n" +
                        "   :1 := r_lines; \n" +
//                        "   --:1 := 'Fortuin!'; \n" +
//                        "   --:1 := r_vlines; \n" +
                        "end;";
        logNode.info(sql);
        try (
                OracleConnection oConn = connectionManager.getConnection(jdbcUrl, userName, password).unwrap(OracleConnection.class);
                OracleCallableStatement stmt = (OracleCallableStatement) oConn.prepareCall(sql);
        ) {
            stmt.registerIndexTableOutParameter(1, 30, OracleTypes.VARCHAR, 2000);
            stmt.execute();
            String[] lines = (String[]) stmt.getPlsqlIndexTable(1);
            dbmsLines = String.join("\n", lines);
            logNode.info("Lines: " + dbmsLines);
        } catch (Exception e) {
            logNode.info("Dbms output get lines failed, probably no lines to read: " + e.getMessage());
        }
        return dbmsLines;
    }

    public Boolean executeParameterStatement(String jdbcUrl, String userName, String password, String procedure, IContext context) throws SQLException {
        logNode.info(String.format("executeStatement: %s, %s, %s", jdbcUrl, userName, procedure));


        try (Connection connection = connectionManager.getConnection(jdbcUrl, userName, password);
             CallableStatement callableStatement = connection.prepareCall(procedure)) {
            logNode.info("parameter count: " + callableStatement.getParameterMetaData().getParameterCount());
            Map<Integer, SqlParameter> parameters = nextParameters.get();
            int i = 0;
            Iterator<SqlParameter> parIterator = parameters.values().iterator();
            while (parIterator.hasNext()) {
                i++;
                SqlParameter sqlPar = parIterator.next();
                logNode.info(String.format("Setting parameter: %d = %s", i, sqlPar));

                if (sqlPar instanceof SqlInParameter) {
                    /*
                     * Set in parameter
                     */
                    SqlInParameter inPar = (SqlInParameter) sqlPar;
                    if (sqlPar.isStringParameter()) {
                        callableStatement.setString(i, inPar.getStringValue());
                    } else if (sqlPar.isBooleanParameter()) {
                        callableStatement.setBoolean(i, inPar.getBooleanValue());
                    } else if (sqlPar.isDateTimeParameter()) {
                        callableStatement.setDate(i, new java.sql.Date(inPar.getDateTimeValue().getTime()));
                    } else if (sqlPar.isDecimalParameter()) {
                        callableStatement.setBigDecimal(i, inPar.getDecimalValue());
                    } else if (sqlPar.isLongParameter()) {
                        callableStatement.setLong(i, inPar.getLongValue());
                    } else if (sqlPar.isObjectParameter()) {
                        // Define a struct
                        IMendixObject mxObj = inPar.getMxObjectValue();
                        String sqlType = inPar.getSqlType();
                        logNode.info(String.format("Creating struct for %s, %s", mxObj, sqlType));
                        List<? extends IMendixObjectMember<?>> objPrimitives = mxObj.getPrimitives(context);

                        StructDescriptor structdesc = StructDescriptor.createDescriptor
                                (inPar.getSqlType(), (OracleConnection) connection.unwrap(OracleConnection.class));
                        int structColumnCount = structdesc.getMetaData().getColumnCount();
                        int attributeCount = objPrimitives.size();
                        if (attributeCount != structColumnCount) {
                            logNode.warn(String.format("SQL object and entity have different attribute counts, %d != %d", structColumnCount, attributeCount));
                        }
                        logNode.info(String.format("attribute count %d", structColumnCount));

                        Object[] attrVals = new Object[attributeCount];
                        Object[] objects = objPrimitives.toArray();

                        for (int mxa = 0; mxa < structColumnCount; mxa++) {
//                            IMendixObjectMember mem = ((IMendixObjectMember) objects[mxa]);
                            String colName = structdesc.getMetaData().getColumnName(mxa+1);
                            IMendixObjectMember mem = mxObj.getMember(context,colName);

                            logNode.info(String.format("getting attribute: %d, %s, %s", mxa,
                                    mem.getValue(context).getClass().getName(),
                                    mem.getValue(context)));

                            if (mem.getValue(context) instanceof java.util.Date) {
                                attrVals[mxa] = new java.sql.Date(((Date) mem.getValue(context)).getTime());

                            } else {
                                attrVals[mxa] = mem.getValue(context);

                            }
                        }
                        logNode.info(String.format("Struct array: %s", Arrays.toString(attrVals)));
                        //Struct objStruct = connection.createStruct(sqlType, attrVals);
                        //callableStatement.setObject(i, objStruct);
                        OracleConnection oraConn = connection.unwrap(OracleConnection.class);
                        StructDescriptor itemDescriptor =
                                StructDescriptor.createDescriptor(sqlType, oraConn);
                        oracle.sql.STRUCT obj = new oracle.sql.STRUCT(itemDescriptor, oraConn, attrVals);
                        OracleCallableStatement oraStmt = callableStatement.unwrap(OracleCallableStatement.class);
                        oraStmt.setSTRUCT(i, obj);
                    }
                } else {
                    /*
                     * Register out parameter
                     */
                    SqlOutParameter outPar = (SqlOutParameter) sqlPar;
                    if (outPar.isStringParameter()) {
                        callableStatement.registerOutParameter(i, OracleTypes.VARCHAR);
                    } else if (outPar.isBooleanParameter()) {
                        // plsql boolean values are not supported in jdbc
                        callableStatement.registerOutParameter(i, OracleTypes.BOOLEAN);
                    } else if (outPar.isDateTimeParameter()) {
                        callableStatement.registerOutParameter(i, OracleTypes.DATE);
                    } else if (outPar.isDecimalParameter()) {
                        callableStatement.registerOutParameter(i, OracleTypes.DECIMAL);
                    } else if (outPar.isLongParameter()) {
                        callableStatement.registerOutParameter(i, OracleTypes.BIGINT);
                    } else if (outPar.isRefCursorParameter()) {
                        callableStatement.registerOutParameter(i, OracleTypes.CURSOR);
                    } else {
                        callableStatement.registerOutParameter(i, OracleTypes.STRUCT, outPar.getSqlTypeName().toUpperCase());
                    }
                }
            }
            logNode.trace("callableStatement: " + callableStatement.toString());
            callableStatement.executeUpdate();

            int j = 0;
            Iterator<SqlParameter> outIterator = parameters.values().iterator();

            /*
             * Get out parameters
             */
            while (outIterator.hasNext()) {
                j++;
                SqlParameter sqlPar = outIterator.next();
                if (sqlPar instanceof SqlOutParameter) {
                    SqlOutParameter outPar = (SqlOutParameter) sqlPar;
                    logNode.info(String.format("Getting parameter: %s, %s", outPar, outPar.getParameterType()));
                    if (outPar.isStringParameter()) {
                        /*
                         * get string value
                         */
                        String stringValue = callableStatement.getString(j);
                        outPar.setStringValue(stringValue);
                        logNode.info("String value = " + stringValue);
                    } else if (outPar.isDateTimeParameter()) {
                        outPar.setDateTimeValue(callableStatement.getDate(j));
                    } else if (outPar.isLongParameter()) {
                        outPar.setLongValue(callableStatement.getLong(j));
                    } else if (outPar.isDecimalParameter()) {
                        outPar.setDecimalValue(callableStatement.getBigDecimal(j));
                    } else if (outPar.isObjectParameter()) {

                        StructDescriptor structdesc = StructDescriptor.createDescriptor
                                (outPar.getSqlTypeName(), (OracleConnection) connection.unwrap(OracleConnection.class));
                        logNode.info(String.format("struct desc attr names: %s, %s", outPar.getSqlTypeName(), structdesc.getMetaData().getColumnName(1)));


                        oracle.sql.STRUCT sqlObj = (oracle.sql.STRUCT) callableStatement.getObject(j);
                        //AttributeDescriptor[] names = sqlObj.getDescriptor().getAttributesDescriptor();
                        int nameCount = structdesc.getMetaData().getColumnCount();
                        //logNode.info("names = " + Arrays.toString(names));
                        Object[] values = sqlObj.getAttributes();
                        logNode.info("Object: " + sqlObj);
                        IMendixObject obj = objectInstantiator.instantiate(context, outPar.getEntityName());
                        for (int sn = 0; sn < nameCount; sn++) {
                            String name = structdesc.getMetaData().getColumnName(sn + 1);
                            logNode.info(String.format("struct attr: %s, %s", name, values[sn]));
                            obj.setValue(context, name, values[sn]);
                        }
                        outPar.setObjectValue(obj);
                    } else if (outPar.isRefCursorParameter()) {
                        /*
                         * get cursor value
                         */
                        OracleCallableStatement oraCallableStatement = callableStatement.unwrap(OracleCallableStatement.class);
                        ResultSet rs = oraCallableStatement.getCursor(j);
                        int colCount = rs.getMetaData().getColumnCount();
                        java.util.List<IMendixObject> resultList = new ArrayList<IMendixObject>();
                        while (rs.next()) {
                            logNode.info("Result set record: " + rs);
                            IMendixObject obj = objectInstantiator.instantiate(context, outPar.getEntityName());
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
                        outPar.setObjectListResult(resultList);
                        rs.close();
                    }
                }
            }
            //OracleStruct struct = (OracleStruct) callableStatement.getObject(2);
            //Object o = struct.getAttributes()[0];
            //logNode.info("struct 0: " + o);
            return true;
        }
    }

    public void setOutParameterObject(String resultEntity, String sqlTypeName) {
        logNode.info("setOutParameterObject");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.OBJECT_TYPE, resultEntity, sqlTypeName));

    }

    public void setOutParameterString() {
        logNode.info("setOutParameterString");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.STRING_TYPE));

    }

    public void setOutParameterRefCursor(String entityName) {
        logNode.info("setOutParameterRefCursor");
        Map<Integer, SqlParameter> parameters = getParameters();
        SqlOutParameter outPar = new SqlOutParameter(SqlOutParameter.REFCURSOR_TYPE, entityName);
        parameters.put(parameters.size() + 1, outPar);
    }

    public void resetSqlParameters() {
        logNode.info("resetSqlParameters");
        Map<Integer, SqlParameter> parameters = null;
        parameters = new HashMap<Integer, SqlParameter>();
        nextParameters.set(parameters);
    }

    static ThreadLocal<Map<Integer, SqlParameter>> nextParameters = new ThreadLocal<Map<Integer, SqlParameter>>();

    public String getStringParameter(Long parameterIndex) {
        logNode.info("getStringParameter: " + parameterIndex);
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        logNode.info("nextParameters: " + nextParameters);
        if (parameters == null) {
            parameters = new HashMap<Integer, SqlParameter>();
            nextParameters.set(parameters);
        }
        logNode.info("nextParameters: " + nextParameters);

        Set<Integer> keys = parameters.keySet();
        logNode.info("Keys: " + Arrays.toString(keys.toArray()));

        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return outPar.getStringValue();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }

    public java.util.List<IMendixObject> getRefCursorParameter(Long parameterIndex) {
        logNode.info("getRefCursorParameter: " + parameterIndex);
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return outPar.getCursorList();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }

    public void setInParameterString(String stringValue) {
        logNode.info("setInParameterString");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlInParameter(stringValue));
    }

    public void setInParameterDecimal(BigDecimal decimalValue) {
        logNode.info("setInParameterDecimal");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlInParameter(decimalValue));
    }

    public void setInParameterLong(Long longValue) {
        logNode.info("setInParameterLong");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlInParameter(longValue));
    }

    public void setInParameterDateTime(Date dateTimeValue) {
        logNode.info("setInParameterDateTime");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlInParameter(dateTimeValue));

    }

    public void setInParameterBoolean(Boolean booleanValue) {
        logNode.info("setInParameterBoolean");
        Map<Integer, SqlParameter> parameters = getParameters();

        parameters.put(parameters.size() + 1, new SqlInParameter(booleanValue));

    }

    private Map<Integer, SqlParameter> getParameters() {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        if (parameters == null) {
            parameters = new HashMap<Integer, SqlParameter>();
            nextParameters.set(parameters);
        }
        return parameters;
    }

    public void setOutParameterBoolean() {
        logNode.info("setOutParameterBoolean");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.BOOLEAN_TYPE));
    }

    public void setOutParameterDate() {
        logNode.info("setOutParameterDate");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.DATE_TIME_TYPE));
    }

    public void setOutParameterDecimal() {
        logNode.info("setOutParameterDecimal");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.DECIMAL_TYPE));
    }

    public void setOutParameterLong() {
        logNode.info("setOutParameterLong");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlOutParameter(SqlOutParameter.LONG_TYPE));
    }

    public Date getDateTimeParameterValue(Long parameterIndex) {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return (Date) outPar.getDateTimeValue();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }

    public BigDecimal getDecimalParameterValue(Long parameterIndex) {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return outPar.getDecimalValue();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }

    public Long getLongParameterValue(Long parameterIndex) {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return outPar.getLongValue();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }

    public void setInParameterObject(IMendixObject objectValue, String sqlType) {
        logNode.info("setInParameterObject");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlInParameter(objectValue, sqlType));

    }

    public IMendixObject getObjectParameter(Long parameterIndex, String resultEntity) {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        logNode.info("sqlPar: " + sqlPar);

        if (sqlPar instanceof SqlOutParameter) {
            SqlOutParameter outPar = ((SqlOutParameter) sqlPar);
            return outPar.getObjectValue();
        } else {
            // not an out parameter
            logNode.error("Not an out parameter");
        }
        return null;
    }
}
