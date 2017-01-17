package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.ArrayList;
import java.util.List;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "cardou"));
    }

    public List<GraphItem> getConnectionsToBacon(String actorName) {
        Session session = driver.session();
        StatementResult result = session.run("MATCH p=shortestPath(\n" +
                "  (bacon:Actors {name: \"Bacon, Kevin (I)\"})-[*]-(relation:Actors {name: actorName})\n" +
                ")\n" +
                "RETURN p");

        List<GraphItem> graph = new ArrayList<GraphItem>() ;

        System.out.println(result.toString()) ;

        while (result.hasNext()) {
            Record record = result.next() ;
            GraphItem item ;

            for (Value value:record.get("nodes").values()) {
                Node node = value.asNode();

                if (node.containsKey("name")) {
                    item = new GraphNode(node.id(), node.get("name").asString(), "Actors");
                } else {
                    item = new GraphNode(node.id(), node.get("title").asString(), "Movies");
                }
                graph.add(item);
            }

            for (Value value:record.get("relations").values()) {
                Relationship relation = value.asRelationship();

                item = new GraphEdge(relation.id(), relation.startNodeId(), relation.endNodeId(), "PLAYED_IN") ;

                graph.add(item);
            }
        }
        session.close() ;

        System.out.println(graph.toString());
        return graph;
    }

    public String parseList(List<GraphItem> list){
        String result="[\n";
        for(GraphItem i: list){

            result+="{\n" +
                        "\"data\": {\n" +
                            "\"id\": " + i.id + ",\n";
            if(i instanceof GraphNode){
                result+="\"type\":"+((GraphNode) i).type + ",\n"
                        + "\"value\":"+ ((GraphNode) i).value + "\n";
            }
            else if(i instanceof GraphEdge){
                result+="\"source\":"+((GraphEdge) i).source + ",\n"
                        + "\"target\":"+((GraphEdge) i).target + ",\n"
                        + "\"value\":"+ ((GraphNode) i).value + "\n";
            }

            result+="}\n" + "},\n";
        }

        result+="]";
        return result;
    }

    private static abstract class GraphItem {
        public final long id;

        private GraphItem(long id) {
            this.id = id;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            GraphItem graphItem = (GraphItem) o;

            return id == graphItem.id;

        }

        @Override
        public int hashCode() {
            return (int) (id ^ (id >>> 32));
        }
    }

    private static class GraphNode extends GraphItem {
        public final String type;
        public final String value;

        public GraphNode(long id, String value, String type) {
            super(id);
            this.value = value;
            this.type = type;
        }
    }

    private static class GraphEdge extends GraphItem {
        public final long source;
        public final long target;
        public final String value;

        public GraphEdge(long id, long source, long target, String value) {
            super(id);
            this.source = source;
            this.target = target;
            this.value = value;
        }
    }

    public static void main(String[] args) {
        Neo4JRepository test = new Neo4JRepository();
        test.getConnectionsToBacon("Cruise, Tom");
    }

}


