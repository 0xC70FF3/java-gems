drop table if exists customer;
create table customer (
    id         INT NOT NULL AUTO_INCREMENT
   ,first_name varchar(30) not null
   ,last_name  varchar(30) not null
   ,initial    varchar(1)  null   
   ,PRIMARY KEY(id)
);
