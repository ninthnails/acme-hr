-- Uncomment if you want to clear the data, then comment back
--DROP TABLE IF EXISTS hierarchy;

CREATE TABLE IF NOT EXISTS hierarchy (
    employee varchar(128) not null,
    supervisor varchar(128),
    primary key(employee)
);

-- Some values for testing
--INSERT INTO hierarchy (employee, supervisor) VALUES ('Daisy', 'Elody'), ('Charlie', 'Daisy'), ('Alice', 'Charlie'), ('Bob', 'Charlie');
