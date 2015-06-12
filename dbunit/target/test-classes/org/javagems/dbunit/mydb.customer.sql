DROP TABLE IF EXISTS mydb.customer;
CREATE TABLE mydb.customer (
    id         INT NOT NULL AUTO_INCREMENT
   ,first_name VARCHAR(30) NOT NULL
   ,last_name  VARCHAR(30) NOT NULL
   ,initial    VARCHAR(1) NULL
   ,PRIMARY KEY(id)
);
