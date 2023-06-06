

# CRExplorer (Cited References Explorer)

see [Project Website](https://andreas-thor.github.io/CRExplorer/) for more information

## Download 

* Stable Version 2: https://github.com/andreas-thor/CRExplorer/releases/download/v2.0/crexplorer.jar
* Developer Build: https://github.com/andreas-thor/CRExplorer/releases/download/dev/crexplorer.jar

## Run

Go to directory where crexplorer.jar is located

* Run the GUI: ``java -jar crexplorer.jar`` 
* Script execution: ``java -cp crexplorer.jar cre.Script <myscriptfile>``

Enable database mode (data is stored in a local database and, thus, CRExplorer consumes less main memory but needs longer for certain operations)

* additional parameter: ``-db``
* with specific database location: ``-db=<path>/<dbname>`` (e.g., ``-db=C:/dev/meinedb``)

