public class Vehicle {
    private int capacity = Env.capacity;
    private int load = 0;
    private double tourLength;

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getLoad() {
        return load;
    }

    public void setLoad(int load) {
        this.load = load;
    }

    public double getTourLength() {
        return tourLength;
    }

    public void setTourLength(double tourLength) {
        this.tourLength = tourLength;
    }

    public boolean canAddLoad(int newLoad) {
        return (this.load + newLoad) <= this.capacity;
    }

    public void addLoad(int newLoad) {
        if (canAddLoad(newLoad))
            this.load += newLoad;
        else {
            App.LOGGER.warning("[ERROR] load exceed capacity of vehicle ");
            throw new Error(" load exceed capacity of vehicle");
        }
    }
}
