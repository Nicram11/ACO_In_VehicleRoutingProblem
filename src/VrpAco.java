import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class VrpAco {
    private List<City> cities;
    private Map<Pair<City, City>, Edge> edgeMap;
    private List<Ant> ants;
    private Ant bestAnt;
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
            bestAnt = new Ant(ant);
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

        while (!getUnvisitedCities(ant.getVisitedCities()).isEmpty()) {
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
        return bestAnt.getVisitedCities();
    }

    public double getBestTourLength() {
        return bestTourLength;
    }

    // ACO with mutations (1 swap between different route + local optimisation)
    // selecting cities until it can no more

    /*
     * private void constructAntTourTabuList(Ant ant) {
     * 
     * ant.reset(cities);
     * 
     * List<City> tabuList = new ArrayList<>();
     * while (ant.getVisitedCities().size() < cities.size()) {
     * City nextCity = selectNextCityTabuList(ant, tabuList);
     * 
     * if(nextCity == null) {
     * ant.completeVehicleTour();
     * tabuList.clear();
     * continue;
     * }
     * 
     * double distance = ant.getCurrentCity().distanceTo(nextCity);
     * int demand = nextCity.getDemand();
     * Vehicle vehicle = ant.getCurrentVehicle();
     * if (vehicle.canAddLoad(demand)) {
     * vehicle.addLoad(demand);
     * ant.visitCity(nextCity, distance);
     * } else {
     * tabuList.add(nextCity);
     * }
     * }
     * ant.completeVehicleTour();
     * }
     */

    /*
     * private City selectNextCityTabuList(Ant ant, List<City> tabuList) {
     * City currentCity = ant.getCurrentCity();
     * List<City> allowedCities = new
     * ArrayList<>(getUnvisitedCities(ant.getVisitedCities()));
     * allowedCities.removeAll(tabuList);
     * 
     * if(allowedCities.isEmpty()) {
     * return null;
     * }
     * 
     * double total = 0.0;
     * Map<City, Double> probabilities = new HashMap<>();
     * 
     * for (City city : allowedCities) {
     * Edge edge = findEdge(currentCity, city);
     * double pheromone = Math.pow(edge.getPheromoneLevel(), Env.alpha);
     * double heuristic = Math.pow(1.0 / edge.getLength(), Env.beta);
     * double probability = pheromone * heuristic;
     * probabilities.put(city, probability);
     * total += probability;
     * }
     * 
     * double random = Math.random() * total;
     * double cumulative = 0.0;
     * for (Map.Entry<City, Double> entry : probabilities.entrySet()) {
     * cumulative += entry.getValue();
     * if (random <= cumulative) {
     * return entry.getKey();
     * }
     * }
     * 
     * return allowedCities.get(0);
     * }
     */

    public static double calculateTourLength(List<City> tour) {

        double sum = 0;

        Iterator<City> cityIterator = tour.iterator();
        City currentCity = cityIterator.next();

        while (cityIterator.hasNext()) {
            City nextCity = cityIterator.next();
            sum += currentCity.distanceTo(nextCity);
            currentCity = nextCity;
        }

        return sum;
    }

    public void mutate(Ant ant) {
        if (Math.random() < Env.mutationProbability) {
            return;
        }

        for (int i = 0; i < Env.findingMutationTryCount; i++) {

            Random random = new Random();
            int firstTripId = random.nextInt(0, ant.getVehicleRunCount());
            int secondTripId = random.nextInt(0, ant.getVehicleRunCount());

            Pair<Integer, Integer> firstTrip = ant.getIndexesOfStartEnd().get(firstTripId);
            Pair<Integer, Integer> secondTrip = ant.getIndexesOfStartEnd().get(secondTripId);

            if (firstTrip.getFirst() + 1 >= firstTrip.getSecond()
                    || secondTrip.getFirst() + 1 >= secondTrip.getSecond()) {
                continue;
            }
            int firstIndex = random.nextInt(firstTrip.getFirst() + 1, firstTrip.getSecond());
            int secondIndex = random.nextInt(secondTrip.getFirst() + 1, secondTrip.getSecond());
            City firstCity = ant.getVisitedCities()
                    .get(firstIndex);
            City secondCity = ant.getVisitedCities()
                    .get(secondIndex);

            if (firstCity.getId() == ant.getDepoCityId() || secondCity.getId() == ant.getDepoCityId()) {
                continue;
            }

            Ant mutatedAnt = new Ant(ant);

            Collections.swap(mutatedAnt.getVisitedCities(), firstIndex, secondIndex);

            List<City> upgradedTour1 = hilltopSearch(
                    mutatedAnt.getVisitedCities().subList(firstTrip.getFirst(), firstTrip.getSecond() + 1));
            List<City> upgradedTour2 = hilltopSearch(
                    mutatedAnt.getVisitedCities().subList(secondTrip.getFirst(), secondTrip.getSecond() + 1));

            for (int j = firstTrip.getFirst(); j <= firstTrip.getSecond(); j++) {
                mutatedAnt.getVisitedCities().set(j, upgradedTour1.get(j - firstTrip.getFirst()));
            }

            for (int j = secondTrip.getFirst(); j <= secondTrip.getSecond(); j++) {
                mutatedAnt.getVisitedCities().set(j, upgradedTour2.get(j - secondTrip.getFirst()));
            }

            if (ant.getTourLength() > mutatedAnt.evaluateDistance()) {
                ant.updateAntValues(mutatedAnt);
            }
        }
    }

    public List<City> hilltopSearch(List<City> trip) {

        List<City> best = new ArrayList<>(trip);
        List<City> current;

        for (int k = 0; k < Env.hilltopSearchTryCount; k++) {
            for (int i = 1; i < trip.size() - 1; i++) {
                current = new ArrayList<>(best);
                City cityI = current.get(i);
                for (int j = i + 1; j < trip.size() - 1; j++) {
                    City cityJ = current.get(j);
                    current.set(i, cityJ);
                    current.set(j, cityI);
                    if (calculateTourLength(current) < calculateTourLength(best)) {
                        best = new ArrayList<>(current);
                    }
                    current.set(i, cityI);
                    current.set(j, cityJ);
                }

            }
        }
        return best;
    }

    public void runOptimizationWithMutations() {
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        for (int iteration = 0; iteration < Env.maxIterations; iteration++) {
            List<Future<?>> futures = new ArrayList<>();
            for (Ant ant : ants) {
                Future<?> future = executor.submit(() -> {
                    constructAntTour(ant);
                    mutate(ant);
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

}