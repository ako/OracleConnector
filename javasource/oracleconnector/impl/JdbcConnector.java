package oracleconnector.impl;


import com.mendix.core.objectmanagement.member.MendixHashString;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaObject;
import com.mendix.systemwideinterfaces.core.meta.IMetaPrimitive.PrimitiveType;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleTypes;
import oracleconnector.interfaces.ConnectionManager;
import oracleconnector.interfaces.ObjectInstantiator;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * JdbcConnector implements the execute query (and execute statement) functionality, and returns a {@link Stream} of {@link IMendixObject}s.
 */
public class JdbcConnector {
    public static final String LOGNAME = "OracleConnector";
    static ThreadLocal<Map<Integer, SqlParameter>> nextParameters = new ThreadLocal<Map<Integer, SqlParameter>>();
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
            logNode.trace("Dbms output get lines failed, probably no lines to read: " + e.getMessage());
        }
        return dbmsLines;
    }

    public Boolean executeParameterStatement(String jdbcUrl, String userName, String password, String procedure, IContext context) throws SQLException {
        logNode.info(String.format("executeStatement: %s, %s, %s", jdbcUrl, userName, procedure));


        try (Connection connection = connectionManager.getConnection(jdbcUrl, userName, password);
             CallableStatement callableStatement = connection.prepareCall(procedure)) {
            logNode.trace("parameter count: " + callableStatement.getParameterMetaData().getParameterCount());

            int i = 0;
            Map<Integer, SqlParameter> parameters = nextParameters.get();
            if (parameters != null && !parameters.isEmpty()) {
                Iterator<SqlParameter> parIterator = parameters.values().iterator();

                while (parIterator.hasNext()) {
                    i++;
                    SqlParameter sqlPar = parIterator.next();
                    logNode.trace(String.format("Setting parameter: %d = %s", i, sqlPar));
                    sqlPar.prepareCall(context, callableStatement);
                }
            }
            logNode.trace("callableStatement: " + callableStatement.toString());
            callableStatement.executeUpdate();

            int j = 0;
            if (parameters != null &&!parameters.isEmpty()) {
                Iterator<SqlParameter> outIterator = parameters.values().iterator();

            /*
             * Get out parameters
             */
                while (outIterator.hasNext()) {
                    j++;
                    SqlParameter sqlPar = outIterator.next();
                    sqlPar.retrieveResult(context, callableStatement);
                }
            }
            return true;
        }
    }

    public void setOutParameterObject(String resultEntity, String sqlTypeName) {
        logNode.info("setOutParameterObject");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterObject(parameters.size() + 1, SqlParameter.DIRECTION_OUT, resultEntity, sqlTypeName));

    }

    public void setOutParameterString() {
        logNode.info("setOutParameterString");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterString(parameters.size() + 1, SqlParameter.DIRECTION_OUT));

    }

    public void setOutParameterRefCursor(String entityName) {
        logNode.info("setOutParameterRefCursor");
        Map<Integer, SqlParameter> parameters = getParameters();
        SqlParameter outPar = new SqlParameterRefCursor(parameters.size() + 1, SqlParameter.DIRECTION_OUT, entityName);
        parameters.put(parameters.size() + 1, outPar);
    }

    public void resetSqlParameters() {
        logNode.info("resetSqlParameters");
        Map<Integer, SqlParameter> parameters = null;
        parameters = new HashMap<Integer, SqlParameter>();
        nextParameters.set(parameters);
    }

    public String getStringParameter(Long parameterIndex) throws SQLException {
        logNode.info("getStringParameter: " + parameterIndex);
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        return sqlPar.getResultValue(String.class);

    }

    public java.util.List<IMendixObject> getRefCursorParameter(Long parameterIndex) throws SQLException {
        logNode.info("getRefCursorParameter: " + parameterIndex);
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        java.util.List<IMendixObject> objList = new java.util.ArrayList<IMendixObject>();
        return sqlPar.getResultValue(objList.getClass());
    }

    public void setInParameterString(String stringValue) {
        logNode.info("setInParameterString");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterString(parameters.size() + 1, SqlParameter.DIRECTION_IN, stringValue));
    }

    public void setInParameterDecimal(BigDecimal decimalValue) {
        logNode.info("setInParameterDecimal");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterDecimal(parameters.size() + 1, SqlParameter.DIRECTION_IN, decimalValue));
    }

    public void setInParameterLong(Long longValue) {
        logNode.info("setInParameterLong");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterLong(parameters.size() + 1, SqlParameter.DIRECTION_IN, longValue));
    }

    public void setInParameterDateTime(Date dateTimeValue) {
        logNode.info("setInParameterDateTime");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterDateTime(parameters.size() + 1, SqlParameter.DIRECTION_IN, dateTimeValue));

    }

    private Map<Integer, SqlParameter> getParameters() {
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        if (parameters == null) {
            parameters = new HashMap<Integer, SqlParameter>();
            nextParameters.set(parameters);
        }
        return parameters;
    }

    public void setOutParameterDate() {
        logNode.info("setOutParameterDate");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterDateTime(parameters.size() + 1, SqlParameter.DIRECTION_OUT));
    }

    public void setOutParameterDecimal() {
        logNode.info("setOutParameterDecimal");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterDecimal(parameters.size() + 1, SqlParameter.DIRECTION_OUT));
    }

    public void setOutParameterLong() {
        logNode.info("setOutParameterLong");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterLong(parameters.size() + 1, SqlParameter.DIRECTION_OUT));
    }

    public Date getDateTimeParameterValue(Long parameterIndex) throws SQLException {
        logNode.info("getDateTimeParameterValue");
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        return sqlPar.getResultValue(Date.class);

    }

    public BigDecimal getDecimalParameterValue(Long parameterIndex) throws SQLException {
        logNode.info("getDecimalParameterValue");
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        return sqlPar.getResultValue(BigDecimal.class);

    }

    public Long getLongParameterValue(Long parameterIndex) throws SQLException {
        logNode.info("getLongParameterValue");
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        return sqlPar.getResultValue(Long.class);

    }

    public void setInParameterObject(IMendixObject objectValue, String sqlType) {
        logNode.info("setInParameterObject");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterObject(parameters.size() + 1, SqlParameter.DIRECTION_IN, objectValue, sqlType));

    }

    public IMendixObject getObjectParameter(Long parameterIndex, String resultEntity) throws SQLException {
        logNode.info("getObjectParameterValue");
        Map<Integer, SqlParameter> parameters = nextParameters.get();
        SqlParameter sqlPar = parameters.get(parameterIndex.intValue());
        return sqlPar.getResultValue(IMendixObject.class);
    }

    public void setInParameterObjectArray(List<IMendixObject> objectValue, String sqlType) {
        logNode.info("setInParameterObjectArray");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterObjectArray(parameters.size() + 1, SqlParameter.DIRECTION_IN, objectValue, sqlType));
    }

    public void setOutParameterObjectArray(String entity, String sqlType) {
        logNode.info("setInParameterObjectArray");
        Map<Integer, SqlParameter> parameters = getParameters();
        parameters.put(parameters.size() + 1, new SqlParameterObjectArray(parameters.size() + 1, SqlParameter.DIRECTION_OUT, entity, sqlType));

    }
}
