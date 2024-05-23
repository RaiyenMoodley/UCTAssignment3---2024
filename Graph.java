import java.util.*;

class Graph {

    private Map<Integer, List<Edge>> edges;

    public Graph(int vertices) {
        edges = new HashMap<>();
        for (int i = 0; i < vertices; i++) {
            edges.put(i, new ArrayList<>());
        }
    }

    public void addEdge(int from, int to, int cost) {
        edges.get(from).add(new Edge(to, cost));
    }

    public List<Edge> getEdges(int vertex) {
        return edges.get(vertex);
    }

    static class Edge {
        int target;
        int cost;

        public Edge(int target, int cost) {
            this.target = target;
            this.cost = cost;
        }
    }

    public DijkstraResult dijkstra(int source) {
        PriorityQueue<Edge> queue = new PriorityQueue<>(Comparator.comparingInt(e -> e.cost));
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> prev = new HashMap<>();
        Map<Integer, Integer> pathCount = new HashMap<>();
        Set<Integer> visited = new HashSet<>();

        edges.keySet().forEach(node -> {
            dist.put(node, Integer.MAX_VALUE);
            prev.put(node, -1);
            pathCount.put(node, 0);
        });
        dist.put(source, 0);
        pathCount.put(source, 1);
        queue.offer(new Edge(source, 0));

        while (!queue.isEmpty()) {
            Edge current = queue.poll();
            if (!visited.add(current.target)) continue;
            for (Edge edge : edges.get(current.target)) {
                if (visited.contains(edge.target)) continue;
                int newDist = dist.get(current.target) + edge.cost;
                if (newDist < dist.get(edge.target)) {
                    dist.put(edge.target, newDist);
                    prev.put(edge.target, current.target);
                    pathCount.put(edge.target, pathCount.get(current.target));
                    queue.offer(new Edge(edge.target, newDist));
                } else if (newDist == dist.get(edge.target)) {
                    pathCount.put(edge.target, pathCount.get(edge.target) + pathCount.get(current.target));
                }
            }
        }
        return new DijkstraResult(dist, prev, pathCount);
    }

    public static class DijkstraResult {
        Map<Integer, Integer> distances;
        Map<Integer, Integer> predecessors;
        Map<Integer, Integer> pathCounts;

        public DijkstraResult(Map<Integer, Integer> distances, Map<Integer, Integer> predecessors, Map<Integer, Integer> pathCounts) {
            this.distances = distances;
            this.predecessors = predecessors;
            this.pathCounts = pathCounts;
        }
    }
}