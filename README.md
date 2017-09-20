# OpenDataToNeo4J

This project is for notebooks initiated, they migrate open data sources to the graph database [Neo4J](http://neo4j.com/). More informations about open data can be find under the following link: (https://okfn.org).

The code in this project is developed with Python v3 and Jupyter v5.0.0.

In this moment exists only one notebook to bring open data to Neo4J. In the notebook *DB_OpenData/Carsharing_Data.ipynb* is demonstrated, how some relational csv data from OpenData-platform of Deutsche Bahn (DB) can be migrated to a Neo4J database.

To successfully execute the transformation on this notebook and migrate the data to a Neo4J database, you need to take the following steps:

- Clone this project
- Download the following csv-sources from the [website](http://data.deutschebahn.com/dataset/data-flinkster).
  - OPENDATA_BOOKING_CARSHARING.csv
  - OPENDATA_CATEGORY_CARSHARING.csv
  - OPENDATA_RENTAL_ZONE_CARSHARING.csv
  - OPENDATA_VEHICLE_CARSHARING.csv
- Save the csv-sources in the subfolder *"DB_OpenData/datasets"*
- Create the subfolder *"DB_OpenData/output"*
- Execute the transformation in notebook *DB_OpenData/Carsharing_Data.ipynb*
- Modify the script *DB_OpenData/scripts/loadDataToNeo4J.sh* depend on the installation folder in your environment.
- Execute the script *DB_OpenData/scripts/loadDataToNeo4J.sh*.

After you've migrated the data to Neo4J, you can also find an example to explore this data with python-based components in the notebook *DB_OpenData/Explore_With_py2neo_NetworkX.ipynb*

I hope you've fun with the project. Feel free to contribute and give suggestions.
