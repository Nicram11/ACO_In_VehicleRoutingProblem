import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class VrpAco {
    private List<City> cities;
    private Map<Pair<City, City>, Edge> edgeMap;
    private List<Ant> ants;
    private List<City> bestTour;
    private double bestTourLength;

    public VrpAco(List<City> cities) {
        this.cities = cities;

        this.edgeMap = createEdges(cities);
        this.ants = initializeAnts(Env.numberOfAnts, cities);
        bestTourLength = Double.MAX_VALUE;
    }

    private Map<Pair<City, City>, Edge> createEdges(List<City> cities) {
        Map<Pair<City, City>, Edge> edgeMap = new HashMap<>();

        for (int i = 0; i < cities.size(); i++) {
            for (int j = i + 1; j < cities.size(); j++) {
                City city1 = cities.get(i);
                City city2 = cities.get(j);

                Edge edge = new Edge(city1, city2, Env.initialPheromoneLevel);
                edgeMap.put(new Pair<>(city1, city2), edge);
            }
        }
        return edgeMap;
    }

    private List<Ant> initializeAnts(int numberOfAnts, List<City> cities) {
        List<Ant> ants = new ArrayList<>();
        for (int i = 0; i < numberOfAnts; i++) {
            ants.add(new Ant(cities));
        }
        return ants;
    }

    public void runOptimization() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int iteration = 0; iteration < Env.maxIterations; iteration++) {
            List<Future<?>> futures = new ArrayList<>();
            for (Ant ant : ants) {
                Future<?> future = executor.submit(() -> {
                    constructAntTour(ant);
                    updateBestTour(ant);
                });
                futures.add(future);

            }
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            updatePheromones();

        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
    }

    private synchronized void updateBestTour(Ant ant) {
        double currentTourLength = ant.getTourLength();
        if (currentTourLength < bestTourLength) {
            bestTourLength = currentTourLength;
            bestTour = new ArrayList<>(ant.getVisitedCities());
        }
    }

    private void updatePheromones() {
        evaporatePheromones();
        for (Ant ant : ants) {
            updatePheromonesForAnt(ant);
        }
    }

    private void addPheromoneContribution(City city1, City city2, double contribution) {
        Edge edge = edgeMap.get(new Pair<>(city1, city2));
        if (edge != null) {
            edge.addPheromone(contribution);
        }
    }

    private void evaporatePheromones() {
        for (Edge edge : edgeMap.values()) {
            edge.evaporatePheromone(Env.pheromoneEvaporationCoefficient);
        }
    }

    private void updatePheromonesForAnt(Ant ant) {
        double contribution = Env.Q / ant.getTourLength();
        List<City> visitedCities = ant.getVisitedCities();

        for (int i = 0; i < visitedCities.size() - 1; i++) {
            addPheromoneContribution(visitedCities.get(i), visitedCities.get(i + 1), contribution);
        }

        if (!visitedCities.isEmpty()) {
            addPheromoneContribution(visitedCities.get(visitedCities.size() - 1),
                    visitedCities.get(0), contribution);
        }
    }

    private void constructAntTour(Ant ant) {

        ant.reset(cities);
        while (ant.getVisitedCities().size() < cities.size()) {
            City nextCity = selectNextCity(ant);

            double distance = ant.getCurrentCity().distanceTo(nextCity);
            int demand = nextCity.getDemand();
            Vehicle vehicle = ant.getCurrentVehicle();
            if (vehicle.canAddLoad(demand)) {
                vehicle.addLoad(demand);
                ant.visitCity(nextCity, distance);
            } else {
                ant.completeVehicleTour();
            }

        }
        ant.completeVehicleTour();
    }

    private City selectNextCity(Ant ant) {
        City currentCity = ant.getCurrentCity();
        List<City> unvisitedCities = getUnvisitedCities(ant.getVisitedCities());

        double total = 0.0;
        Map<City, Double> probabilities = new HashMap<>();

        for (City city : unvisitedCities) {
            Edge edge = findEdge(currentCity, city);
            double pheromone = Math.pow(edge.getPheromoneLevel(), Env.alpha);
            double heuristic = Math.pow(1.0 / edge.getLength(), Env.beta);
            double probability = pheromone * heuristic;
            probabilities.put(city, probability);
            total += probability;
        }

        double random = Math.random() * total;
        double cumulative = 0.0;
        for (Map.Entry<City, Double> entry : probabilities.entrySet()) {
            cumulative += entry.getValue();
            if (random <= cumulative) {
                return entry.getKey();
            }
        }

        return unvisitedCities.get(0);
    }

    private List<City> getUnvisitedCities(List<City> visitedCities) {
        List<City> unvisited = new ArrayList<>(cities);
        unvisited.removeAll(visitedCities);
        return unvisited;
    }

    private Edge findEdge(City city1, City city2) {
        return edgeMap.get(new Pair<>(city1, city2));
    }

    public List<City> getBestTour() {
        return bestTour;
    }

    public double getBestTourLength() {
        return bestTourLength;
    }
}