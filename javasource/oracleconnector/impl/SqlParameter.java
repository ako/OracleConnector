package oracleconnector.impl;

import java.sql.CallableStatement;

/**
 * Created by ako on 18-11-2016.
 */
public class SqlParameter {
    public static final String STRING_TYPE = "String";
    public static final String REFCURSOR_TYPE = "RefCursor";
    public static final String OBJECT_TYPE = "Object";
    public static final String LONG_TYPE = "Long";
    public static final String BOOLEAN_TYPE = "Boolean";
    public static final String DATE_TIME_TYPE = "DateTime";
    public static final String DECIMAL_TYPE = "Decimal";

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
}
