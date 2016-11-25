package oracleconnector.impl;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * Created by ako on 18-11-2016.
 */
public abstract class SqlParameter {
    public static final String STRING_TYPE = "String";
    public static final String REFCURSOR_TYPE = "RefCursor";
    public static final String OBJECT_TYPE = "Object";
    public static final String LONG_TYPE = "Long";
    public static final String BOOLEAN_TYPE = "Boolean";
    public static final String DATE_TIME_TYPE = "DateTime";
    public static final String DECIMAL_TYPE = "Decimal";
    public static final String DIRECTION_IN = "In";
    public static final String DIRECTION_OUT = "Out";
    public static final String DIRECTION_INOUT = "InOut";
    public IContext context;

    public String getParameterDirection() {
        return parameterDirection;
    }

    public void setParameterDirection(String parameterDirection) {
        this.parameterDirection = parameterDirection;
    }

    private String parameterDirection = null;

    public String getParameterType() {
        return parameterType;
    }

    public void setParameterType(String parameterType) {
        this.parameterType = parameterType;
    }

    private String parameterType = null;

    public boolean isStringParameter() {
        return parameterType.equals(STRING_TYPE);
    }

    public boolean isLongParameter() {
        return parameterType.equals(LONG_TYPE);
    }

    public boolean isBooleanParameter() {
        return parameterType.equals(BOOLEAN_TYPE);
    }

    public boolean isDateTimeParameter() {
        return parameterType.equals(DATE_TIME_TYPE);
    }

    public boolean isDecimalParameter() {
        return parameterType.equals(DECIMAL_TYPE);
    }

    public boolean isRefCursorParameter() {
        return parameterType.equals(REFCURSOR_TYPE);
    }

    public boolean isObjectParameter() {
        return parameterType.equals(OBJECT_TYPE);
    }

    abstract public void prepareCall(IContext context, CallableStatement callableStatement) throws SQLException;

    abstract public void retrieveResult(IContext context, CallableStatement callableStatement) throws SQLException;

    abstract public <T extends Object> T getResultValue(Class<T> type) throws SQLException;

    protected final ILogNode logNode = Core.getLogger(this.getClass().getName());

    public int getParameterIndex() {
        return parameterIndex;
    }

    public void setParameterIndex(int parameterIndex) {
        this.parameterIndex = parameterIndex;
    }

    protected int parameterIndex;

    public boolean isInParameter() {
        return parameterDirection.equals(DIRECTION_IN);
    }

    public boolean isOutParameter() {
        return parameterDirection.equals(DIRECTION_OUT);
    }

    public boolean isInOutParameter() {
        return parameterDirection.equals(DIRECTION_INOUT);
    }
}
