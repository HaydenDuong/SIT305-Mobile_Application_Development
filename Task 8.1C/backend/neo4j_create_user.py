from neo4j import GraphDatabase

uri = "neo4j+ssc://26c68983.databases.neo4j.io"
user = "neo4j"
password = "B0_uJk_ec2ISsyystlRcGpfqJlIHq3MgIs3OtCq1Tq8"

driver = GraphDatabase.driver(uri, auth=(user, password))

def create_user_with_interests(user_id, interests):
    with driver.session(database="neo4j") as session:
        # Create user node (if not exists)
        session.run(
            "MERGE (u:User {id: $user_id})",
            user_id=user_id
        )
        # Create interest nodes and relationships
        for interest in interests:
            session.run(
                """
                MERGE (i:Interest {name: $interest})
                MERGE (u:User {id: $user_id})
                MERGE (u)-[:HAS_INTEREST]->(i)
                """,
                user_id=user_id,
                interest=interest
            )
        print(f"User '{user_id}' and interests {interests} created/linked.")

try:
    # Example usage
    create_user_with_interests("user A", ["hiking", "guitar", "reading"])
finally:
    driver.close()