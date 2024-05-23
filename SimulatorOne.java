import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class SimulatorOne {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int vertexCount = scanner.nextInt();
        scanner.nextLine(); // Skipping the newline character
        Graph graph = new Graph(vertexCount);

        // Populate the graph with edges
        for (int i = 0; i < vertexCount; i++) {
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                String[] parts = line.split(" ");
                int vertex = Integer.parseInt(parts[0]);
                for (int j = 1; j < parts.length; j += 2) {
                    int destination = Integer.parseInt(parts[j]);
                    int weight = Integer.parseInt(parts[j + 1]);
                    graph.addEdge(vertex, destination, weight);
                }
            }
        }

        // Gathering store locations
        int storeCount = scanner.nextInt();
        Set<Integer> stores = new HashSet<>();
        for (int i = 0; i < storeCount; i++) {
            stores.add(scanner.nextInt());
        }

        // Collecting client information
        int clientCount = scanner.nextInt();
        List<Integer> clients = new ArrayList<>();
        for (int i = 0; i < clientCount; i++) {
            clients.add(scanner.nextInt());
        }

        simulateService(graph, stores, clients);
    }

    private static void simulateService(Graph graph, Set<Integer> stores, List<Integer> clients) {
        for (Integer client : clients) {
            System.out.println("client " + client);

            boolean serviceable = false;
            Map<Integer, Integer> distances = new HashMap<>();
            Map<Integer, Graph.DijkstraResult> paths = new HashMap<>();

            // First, evaluate distances and paths from stores to the client
            for (Integer store : stores) {
                Graph.DijkstraResult result = graph.dijkstra(store);
                if (result.distances.get(client) != null && result.distances.get(client) < Integer.MAX_VALUE) {
                    distances.put(store, result.distances.get(client));
                    paths.put(store, result);
                    serviceable = true; // At least one store can reach the client
                }
            }

            // If the client cannot be serviced, print "cannot be helped" and continue to the next client
            if (!serviceable) {
                System.out.println("cannot be helped");
                continue;
            }

            // Now, check if the client can reach any of the stores
            Graph.DijkstraResult clientResult = graph.dijkstra(client);
            Graph.DijkstraResult finalClientResult = clientResult;
            boolean canReach = stores.stream().anyMatch(store -> finalClientResult.distances.get(store) != null && finalClientResult.distances.get(store) < Integer.MAX_VALUE);

            // If the client cannot reach any store, print "cannot be helped"
            if (!canReach) {
                System.out.println("cannot be helped");
                continue;
            }

            // Since the client can be serviced and can reach a store, print the paths
            // For store to client
            List<Map.Entry<Integer, Integer>> sortedEntries = distances.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());
            for (Map.Entry<Integer, Integer> entry : sortedEntries) {
                if (entry.getValue().equals(Collections.min(distances.values()))) {
                    System.out.println("taxi " + entry.getKey());
                    if (paths.get(entry.getKey()).pathCounts.get(client) > 1) {
                        System.out.println("multiple solutions cost " + entry.getValue());
                    } else {
                        printPath(reconstructPath(paths.get(entry.getKey()).predecessors, entry.getKey(), client));
                    }
                }
            }

            clientResult = graph.dijkstra(client);

            int minDist = clientResult.distances.entrySet().stream()
                    .filter(e -> stores.contains(e.getKey()) && e.getValue() != Integer.MAX_VALUE)
                    .mapToInt(Map.Entry::getValue)
                    .min()
                    .orElse(Integer.MAX_VALUE);

            List<Map.Entry<Integer, Integer>> clientEntries = clientResult.distances.entrySet().stream()
                    .filter(e -> stores.contains(e.getKey()) && e.getValue().equals(minDist))
                    .sorted(Map.Entry.comparingByKey())
                    .collect(Collectors.toList());

            for (Map.Entry<Integer, Integer> entry : clientEntries) {
                if (clientResult.pathCounts.get(entry.getKey()) > 1) {
                    System.out.println("shop " + entry.getKey());
                    System.out.println("multiple solutions cost " + entry.getValue());
                } else {
                    System.out.println("shop " + entry.getKey());
                    printPath(reconstructPath(clientResult.predecessors, client, entry.getKey()));
                }
            }
        }
    }

    private static List<Integer> reconstructPath(Map<Integer, Integer> predecessors, int start, int end) {
        LinkedList<Integer> path = new LinkedList<>();
        Integer current = end;
        while (current != null && current != start) {
            path.addFirst(current);
            current = predecessors.get(current);
        }
        if (current != null) path.addFirst(start); // Ensures start is only added if a path exists
        return path;
    }

    private static void printPath(List<Integer> path) {
        if (path.isEmpty() || path.contains(-1)) {
            System.out.println("cannot be helped");
            return;
        }
        path.forEach(node -> System.out.print(node + (path.indexOf(node) < path.size() - 1 ? " " : "")));
        System.out.println();
    }
}