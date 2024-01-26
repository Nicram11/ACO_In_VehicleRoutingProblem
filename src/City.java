import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class City {
    int id;
    double x;
    double y;
    int demand;

    public City(int id, double x, double y) {
        this.id = id;
        this.x = x;
        this.y = y;

    }

    public City(int id, double x, double y, int demand) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.demand = demand;

    }

    public double distanceTo(City other) {
        double dx = this.x - other.x;
        double dy = this.y - other.y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    public int visit() {

        return this.id;
    }

    @Override
    public String toString() {

        return "City [id=" + id + ",demand=" + demand + "]";
    }

    public static ArrayList<City> fetchCities(String filename) throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line = reader.readLine();

        while (!line.contains("NODE_COORD_SECTION")) {
            line = reader.readLine();
        }
        line = reader.readLine();

        ArrayList<City> coordinates = new ArrayList<>();

        while (!line.contains("DEMAND_SECTION")) {

            String[] values = line.trim().split("\\s+");
            coordinates.add(new City(Integer.parseInt(values[0]), Double.parseDouble(values[1]),
                    Double.parseDouble(values[2])));
            line = reader.readLine();
        }
        line = reader.readLine();

        int i = 0;
        while (!line.contains("DEPOT_SECTION")) {
            String[] values = line.trim().split("\\s+");
            int cityId = Integer.parseInt(values[0]);
            int demand = Integer.parseInt(values[1]);

            for (City city : coordinates) {
                if (city.getId() == cityId) {
                    city.setDemand(demand);
                    break;
                }
            }

            line = reader.readLine();
        }

        reader.close();
        // App.LOGGER.info(coordinates.toString());
        return coordinates;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public int getDemand() {
        return demand;
    }

    public void setDemand(int demand) {
        this.demand = demand;
    }
}