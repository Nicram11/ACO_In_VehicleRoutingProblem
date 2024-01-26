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
    }

    private City startDepoCity(List<City> cities) {

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

}
