CREATE TABLE emp  (
  id NUMBER(8) PRIMARY KEY,
  name varchar2(200)
);

INSERT INTO emp (id,name) VALUES (1,'Piet');
INSERT INTO emp (id,name) VALUES (2,'Albert');
INSERT INTO emp (id,name) VALUES (3,'Kees');

CREATE OR replace TYPE emp_type AS object(id NUMBEr(8), name varchar2(200));

SELECT * FROM user_errors;

