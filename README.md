

# CRExplorer (Cited References Explorer)

see [Project Website](https://andreas-thor.github.io/CRExplorer/) for more information

## Download 

* Stable Version 2: https://github.com/andreas-thor/CRExplorer/releases/download/v2.0/crexplorer.jar
* Developer Build: https://github.com/andreas-thor/CRExplorer/releases/download/dev/crexplorer.jar

## Run

Go to directory where crexplorer.jar is located

* Run the GUI: ``java -jar crexplorer.jar`` 
* Script execution: ``java -cp crexplorer.jar cre.Script <myscriptfile>``

Enable database mode
* data is stored in a local PostgreSQL database and, thus, CRExplorer consumes less main memory but needs longer for certain operations
* connection to database via JDBC
    * default database: localhost:5455/postgres with username=postgres and password=cre
    * see section Docker for running a PostgreSQL db
* additional parameter: ``-db`` employs default database 
* with specific database location: ``-db=<host>:<port>/<dbname>`` (e.g., ``-db=192.0.1.2:6512/credb)

## Run PostgreSQL Database in docker

* start container (and download image if necessary): ``docker run --name CREPostgres -p 5455:5432 -e POSTGRES_PASSWORD=cre -d postgres:15.2``
* stop container: ``docker stop CREPostgres``
* re-start container (when stopped): ``docker start CREPostgres``
* remove container: ``docker rm CREPostgres``