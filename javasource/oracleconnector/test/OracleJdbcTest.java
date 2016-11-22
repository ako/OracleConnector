package oracleconnector.test;

import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleStruct;
import oracle.jdbc.OracleTypes;

import java.sql.*;

/**
 * Created by ako on 18-11-2016.
 */
public class OracleJdbcTest {
    /**
     * Call package procedure returning plsql record type.
     * plsql types cannot be using in jdbc, we need to wrap it in a plsql block
     */
    public void example2(Connection conn) throws SQLException {
        System.out.println("=== example2 ===");
        try {
            CallableStatement stmt = conn.prepareCall("DECLARE\n" +
                    "  l_num2 poc_api_store.NasaNumberRecordType;\n" +
                    "BEGIN\n" +
                    "  poc_api_store.get_nasa_record(:1,l_num2);\n" +
                    "  :2 := l_num2.nasa_number;" +
                    "END;");
            stmt.setString(1, "a");
            stmt.registerOutParameter(2, Types.NUMERIC);
            stmt.execute();
            Long nasa_number = stmt.getLong(2);
            System.out.println("nasa number: " + nasa_number);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call package procedure returning sql object type, this can be handled as objects
     */
    public void example3(Connection conn) throws SQLException {
        System.out.println("=== example3 ===");
        try {
            CallableStatement stmt = conn.prepareCall("begin poc_api_store.get_nasa_object(:1,:2); END;");
            stmt.setString(1, "a");
            stmt.registerOutParameter(2, OracleTypes.STRUCT, "Nasa_number_obj".toUpperCase());
            stmt.execute();
            OracleStruct struct = (OracleStruct) stmt.getObject(2);
            Object o = struct.getAttributes()[0];
            System.out.println("nasa number: " + o);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Call package procedure returning associative array with name varchar
     */
    public void example4(Connection conn) throws SQLException {
        System.out.println("=== example3 ===");
        try {
            OracleCallableStatement stmt = (OracleCallableStatement)conn.prepareCall("begin poc_api_store.get_emp_name_tab_type(:1,:2); END;");
            stmt.setString(1, "a");
            //stmt.registerOutParameter(2, OracleTypes.STRUCT, "Nasa_number_obj".toUpperCase());
            stmt.registerIndexTableOutParameter(2, 30, OracleTypes.VARCHAR, 0);
            stmt.execute();
            String[] names = (String[])stmt.getPlsqlIndexTable(2);
            System.out.println("nasa number: " + names[2]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = null;
        conn = DriverManager.getConnection("jdbc:oracle:thin:ahold/ahold@localhost:21521:xe");
        OracleJdbcTest test = new OracleJdbcTest();
        test.example2(conn);
        test.example3(conn);
        test.example4(conn);
        conn.close();
    }
}
