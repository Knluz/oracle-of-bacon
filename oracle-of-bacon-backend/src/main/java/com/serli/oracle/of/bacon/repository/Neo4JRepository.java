package com.serli.oracle.of.bacon.repository;


import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class Neo4JRepository {
    private final Driver driver;

    public Neo4JRepository() {
        driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "cardou"));
    }

    public List<?> getConnectionsToBacon(String actorName) {
        Session session = driver.session();

        StatementResult result = session.run("MATCH p=shortestPath(\n" +
                "  (bacon:Actors {name: 'Bacon, Kevin (I)'})-[*]-(relation:Actors {name: '" + actorName + "'})\n" +
                ")\n" +
                "RETURN nodes(p) as nodes,relationships(p) as relations");

        List<GraphItem> graph = new ArrayList<GraphItem>();


        while (result.hasNext()) {
            Record record = result.next();
            GraphItem item;

            for (Value value : record.get("nodes").values()) {
                Node node = value.asNode();

                if (node.containsKey("name")) {
                    item = new GraphNode(node.id(), node.get("name").asString(), "Actor");
                } else {
                    item = new GraphNode(node.id(), node.get("title").asString(), "Movie");
                }
                graph.add(item);
            }

            for (Value value : record.get("relations").values()) {
                Relationship relation = value.asRelationship();

                item = new GraphEdge(relation.id(), relation.startNodeId(), relation.endNodeId(), "PLAYED_IN");

                graph.add(item);
            }
        }

        session.close();


        return graph.stream()
                .map(graphItem -> new DataObject(graphItem))
                .collect(Collectors.toList());
    }

    private static class DataObject {
        private final GraphItem data;

        private DataObject(GraphItem data) {
            this.data = data;
        }
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
}


