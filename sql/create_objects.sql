CREATE OR replace TYPE Nasa_number_obj AS OBJECT (nasa_number number);

CREATE TABLE emp  (
  id NUMBER(8) PRIMARY KEY,
  name varchar2(200)
);

INSERT INTO emp (id,name) VALUES (1,'Piet');
INSERT INTO emp (id,name) VALUES (2,'Albert');
INSERT INTO emp (id,name) VALUES (3,'Kees');

CREATE OR replace TYPE emp_type AS object(id NUMBEr(8), name varchar2(200));

SELECT * FROM user_errors;

DROP PACKAGE poc_api_store;
/*
 ************************************************
 * package spec
 ************************************************
 */
CREATE OR replace PACKAGE poc_api_store AS
  PROCEDURE get_nasa_number
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT number
  );
  TYPE NasaNumberRecordType IS record(nasa_number number);
  PROCEDURE get_nasa_record
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT NasaNumberRecordType
  );
  PROCEDURE get_nasa_object
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT Nasa_Number_obj
  );
  PROCEDURE get_emp_type
  ( p_item_number   IN  VARCHAR2
  , p_emp           OUT emp_type
  );
  TYPE emp_record_type IS record(id NUMBER, name varchar2(200));
  TYPE emp_tab_type IS TABLE OF emp_record_type INDEX BY BINARY_integer;
  PROCEDURE get_emp_tab_type
  ( p_item_number IN VARCHAR2
  , p_emp_tab     OUT emp_tab_type
  );
  TYPE emp_name_tab_type IS TABLE OF varchar2(200) INDEX BY BINARY_integer;
  PROCEDURE get_emp_name_tab_type
  ( p_item_number IN VARCHAR2
  , p_emp_tab     OUT emp_name_tab_type
  );
END;

SELECT * FROM user_errors;

/*
 ************************************************
 * package body
 ************************************************
 */
create OR replace package body poc_api_store AS
  PROCEDURE get_nasa_number
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT number
  ) iS
  begin
	  p_nasa_number := 2;
  END;

  PROCEDURE get_nasa_record
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT NasaNumberRecordType
  ) IS
  begin
	  p_nasa_number.nasa_number := 3;
  END;
  
  PROCEDURE get_nasa_object
  ( p_item_number   IN  VARCHAR2
  , p_nasa_number   OUT Nasa_Number_obj
  ) IS
  BEGIN
	p_nasa_number := Nasa_Number_obj(4);  
  END;
  
  PROCEDURE get_emp_type
  ( p_item_number   IN  VARCHAR2
  , p_emp           OUT emp_type
  ) IS
    CURSOR c_emp IS
      SELECT id
      ,       name
      FROM    EMP;
  BEGIN
	  FOR r_emp IN c_emp loop
   	    p_emp := emp_type(r_emp.id, r_emp.name);
	  END loop;
  END;

  PROCEDURE get_emp_tab_type
  ( p_item_number IN VARCHAR2
  , p_emp_tab     OUT emp_tab_type
  ) IS 
    CURSOR c_emp IS
      SELECT id
      ,       name
      FROM    EMP;
      l_index integer := 0;
  BEGIN
	  FOR r_emp IN c_emp loop
	      l_index := l_index + 1;
	      dbms_output.put_line('r: ' || l_index);
	      p_emp_tab(l_index).id := r_emp.id;
	      p_emp_tab(l_index).name := r_emp.name;
	  END loop;
  end;

  PROCEDURE get_emp_name_tab_type
  ( p_item_number IN VARCHAR2
  , p_emp_tab     OUT emp_name_tab_type
  ) IS
    CURSOR c_emp IS
      SELECT id
      ,       name
      FROM    EMP;
      l_index integer := 0;
  BEGIN
	  FOR r_emp IN c_emp loop
	      l_index := l_index + 1;
	      dbms_output.put_line('r: ' || l_index);
	      p_emp_tab(l_index) := r_emp.name;
	  END loop;
  end;
END;

SELECT * FROM user_errors;
SELECT * FROM emp;

DECLARE
  l_num NUMBER;
  l_num2 poc_api_store.NasaNumberRecordType;
  l_num3 Nasa_Number_obj;
  l_emp emp_type;
  l_emp_tab poc_api_store.emp_tab_type;
  emp_ref_cursor sys_Refcursor;
  l_emp_name_tab poc_api_store.emp_name_tab_type;
BEGIN
  poc_api_store.get_nasa_number('a',l_num);
  poc_api_store.get_nasa_record('a',l_num2);
  poc_api_store.get_nasa_object('a',l_num3);
  poc_api_store.get_emp_type('a',l_emp);
  poc_api_store.get_emp_tab_type('a',l_emp_tab);
  dbms_output.put_line('x: ' || l_emp_tab(2).name);
  
  --OPEN emp_ref_cursor
  --  FOR SELECT * FROM table(cast(l_emp_tab AS emp_type));
    
  poc_api_store.get_emp_name_tab_type('a',l_emp_name_tab);
  dbms_output.put_line('x: ' || l_emp_name_tab(3));
END;
