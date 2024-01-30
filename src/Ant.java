import java.util.ArrayList;
import java.util.List;

public class Ant {
    private List<City> visitedCities;
    private City currentCity;
    private double tourLength;
    private Vehicle currentVehicle;

    public Ant(List<City> cities) {
        this.visitedCities = new ArrayList<>();
        this.currentCity = startDepoCity(cities);
        this.currentVehicle = new Vehicle();
        this.visitedCities.add(currentCity);
        this.tourLength = 0.0;

        this.depoCityId = currentCity.getId();
        this.vehicleRunCount = 0;
        this.indexesOfStartEnd = new ArrayList<>();
    }

    public Ant(Ant ant) {
        this.visitedCities = new ArrayList<>(ant.visitedCities);
        this.currentCity = visitedCities.get(visitedCities.size()-1);
        this.tourLength = ant.tourLength;

        this.depoCityId = currentCity.getId();
        this.vehicleRunCount = ant.vehicleRunCount;
        this.indexesOfStartEnd = new ArrayList<>(ant.indexesOfStartEnd);
    }

    public void updateAntValues(Ant ant) {
        this.visitedCities = new ArrayList<>(ant.visitedCities);
        this.currentCity = visitedCities.get(visitedCities.size()-1);
        this.tourLength = ant.tourLength;

        this.depoCityId = currentCity.getId();
        this.vehicleRunCount = ant.vehicleRunCount;
        this.indexesOfStartEnd = new ArrayList<>(ant.indexesOfStartEnd);
    }

    public City startDepoCity(List<City> cities) {

        return cities.get(0);
    }

    public void visitCity(City city, double distance) {
        visitedCities.add(city);
        this.currentCity = city;
        this.tourLength += distance;
    }

    public boolean hasVisited(City city) {
        return visitedCities.contains(city);
    }

    public void completeVehicleTour() {
        City depositCity = visitedCities.get(0);
        tourLength += calculateDistance(currentCity, depositCity);
        visitedCities.add(depositCity);
        currentCity = depositCity;

        // for mutation
        vehicleRunCount++;
        Integer tourStartIndex = findTourStart();
        indexesOfStartEnd.add(new Pair<>(tourStartIndex, visitedCities.size()-1));


        currentVehicle = new Vehicle();
    }

    private double calculateDistance(City city1, City city2) {
        return city1.distanceTo(city2);
    }

    public List<City> getVisitedCities() {
        return visitedCities;
    }

    public double getTourLength() {
        return tourLength;
    }

    public void reset(List<City> cities) {
        visitedCities.clear();
        currentCity = startDepoCity(cities);
        visitedCities.add(currentCity);
        tourLength = 0.0;
        currentVehicle = new Vehicle();

        vehicleRunCount = 0;
        indexesOfStartEnd = new ArrayList<>();
    }

    public void setVisitedCities(List<City> visitedCities) {
        this.visitedCities = visitedCities;
    }

    public City getCurrentCity() {
        return currentCity;
    }

    public void setCurrentCity(City currentCity) {
        this.currentCity = currentCity;
    }

    public void setTourLength(double tourLength) {
        this.tourLength = tourLength;
    }

    public Vehicle getCurrentVehicle() {
        return currentVehicle;
    }

    public void setCurrentVehicle(Vehicle currentVehicle) {
        this.currentVehicle = currentVehicle;
    }

    // for mutation purposes

    private int vehicleRunCount;

    public int getVehicleRunCount() {
        return vehicleRunCount;
    }

    public double evaluateDistance() {
        double tourLength = 0;
        for(int i = 0; i < visitedCities.size()-1; i++) {
           tourLength += this.calculateDistance(visitedCities.get(i), visitedCities.get(i+1));
        }

        this.tourLength = tourLength;
        return tourLength;
    }

    private List<Pair<Integer, Integer>> indexesOfStartEnd;
    private int depoCityId;

    public int getDepoCityId() {
        return depoCityId;
    }

    public List<Pair<Integer, Integer>> getIndexesOfStartEnd() {
        return indexesOfStartEnd;
    }

    public int findTourStart() {

        for(int i = visitedCities.size()-2; i>0; i--) {
            if (visitedCities.get(i).id == visitedCities.get(0).id) {
                return i;
            }
        }

        return 0;
    }

}
