# Mendix Oracle Connector

Mendix connector to easily connect to external Oracle databases.
This is a fork of the Database Connector, with additional Oracle specific functionality.
This additional functionality is mainly focussed on calling plsql functionality.

## Examples

### Simple Pl/SQL parameters

The following example executes an anonymous plsql block with one string input parameter, and one string output parameter.

 ![Anonymous pl/sql block with String parameters][1]
 
Before the call you need to provide for every parameter:
* a call to set its input value
* a call to register it as an output parameter
* after the call you can retrieve the output parameters one by one

### Multiple in and out parameters

The following example has 4 input parameters and 4 output parameters:

 ![Multiple in and out parameters with plsql block][2]

### Ref Cursor

The following plsql block returns a refcursor:

    declare
      l_cursor sys_refcursor;
    begin
      open l_cursor for
        select username
        ,      user_id as userid
        ,      created 
        from   all_users
        order  by 1;
      :1 := l_cursor;
    end;

You can use it in a microflow:

 ![Using a user defined SQL type in a plsql block][3]

### User defined SQL Types

Next example uses a SQL type:

    create or replace type user_info_obj is object (
        userid number(8), username varchar2(30), created date);

This sql type will be mapped on the following entity:

 ![Entity for SQL type][5]
 
The mapping is case-sensitive, so all the attributes need to be uppercased.

We want to call the following plsql code:

    declare
      l_user     user_info_obj := :1;
    begin
      dbms_output.put_line(''Got user: '' || l_user.username);
      l_user.username := l_user.username || ''x''; 
      l_user.userid := l_user.userid + 1;
      l_user.created := l_user.created + 1;
      :2 := l_user;
    end;

 ![Reading from a refcursor in a plsql block][4]
 
## Development

This project uses ivy to manage java dependencies:
* runivy.cmd - download all dependencies required for running the project into userlib folder
* runivy-export.cmd - download only the dependencies which should be included in the exported module

## Known Issues

* Use of dbms_output doesn't work reliably as it may be using multiple different database connections from the connection pool.

## License

Apache 2.0 license

## Release history

* 0.1 - Initial release

 [1]: docs/images/plsql_string_par_mf.png
 [2]: docs/images/plsql_multiple_pars_mf.png
 [3]: docs/images/sql_object_type_mf.png
 [4]: docs/images/plsql_refcursor_mf.png
 [5]: docs/images/sql_type_entity.png