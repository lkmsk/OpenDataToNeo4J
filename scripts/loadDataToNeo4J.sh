#/usr/bin/env bash
echo 'Stop neo4j'
/Applications/neo4j-community-3.2.3/bin/neo4j stop
echo 'wait for 10 seconds'
sleep 10
/Applications/neo4j-community-3.2.3/bin/neo4j status
echo 'Remove graphdb content'
rm -rf /Applications/neo4j-community-3.2.3/data/databases/bahnOpenDataCS.graph.db
echo 'create database-folder'
mkdir /Applications/neo4j-community-3.2.3/data/databases/bahnOpenDataCS.graph.db
chmod 775 -R /Applications/neo4j-community-3.2.3/data/databases/bahnOpenDataCS.graph.db
echo 'database folder'
ls -la /Applications/neo4j-community-3.2.3/data/databases/bahnOpenDataCS.graph.db
/Applications/neo4j-community-3.2.3/bin/neo4j-admin import --mode csv --database bahnOpenDataCS.graph.db --nodes /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/bookings.dsv --nodes /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/parent_categories.dsv --nodes /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/categories.dsv --nodes /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rentalZones.dsv --nodes /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/vehicles.dsv  --relationships /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rel_booking_vehicle.dsv --relationships /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rel_booking_category.dsv --relationships /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rel_cat_pcat.dsv --relationships /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rel_vehicle_end_rental_zone.dsv --relationships /Users/ilker/Workspaces/jupyter/DB_OpenData_To_Neo4J/output/rel_vehicle_start_rental_zone.dsv --ignore-extra-columns=true --ignore-duplicate-nodes=true --ignore-missing-nodes=true --id-type=INTEGER
echo 'end of data load'
echo 'Start neo4j'
/Applications/neo4j-community-3.2.3/bin/neo4j start
echo 'wait for 10 seconds'
sleep 10
/Applications/neo4j-community-3.2.3/bin/neo4j status
