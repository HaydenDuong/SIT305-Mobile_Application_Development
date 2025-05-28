from neo4j import GraphDatabase

uri = "neo4j+ssc://26c68983.databases.neo4j.io"
user = "neo4j"
password = "B0_uJk_ec2ISsyystlRcGpfqJlIHq3MgIs3OtCq1Tq8"

driver = GraphDatabase.driver(uri, auth=(user, password))

try:
    with driver.session(database="neo4j") as session:
        # 1. Get Neo4j version
        result = session.run("CALL dbms.components() YIELD name, versions, edition RETURN name, versions, edition")
        for record in result:
            print("Neo4j version info:", record)

        # 2. List all nodes (should be empty)
        result = session.run("MATCH (n) RETURN n LIMIT 5")
        nodes = list(result)
        print(f"Found {len(nodes)} nodes in the database.")
finally:
    driver.close()