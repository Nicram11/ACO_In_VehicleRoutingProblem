import java.util.List;

public class CityListToStringConverter {
    public static String convertCityIdsToString(List<City> cities) {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0; i < cities.size(); i++) {
            stringBuilder.append(cities.get(i).getId());

            if (i < cities.size() - 1) {
                stringBuilder.append(", ");
            }
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }

    public static String convertCitiesToDetailedString(List<City> cities) {
        StringBuilder stringBuilder = new StringBuilder("[");

        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            stringBuilder.append(city.getId())
                    .append(" ")
                    .append(city.getX())
                    .append(" ")
                    .append(city.getY())
                    .append(" ")
                    .append(city.getDemand());

            if (i < cities.size() - 1) {
                stringBuilder.append("; ");
            }
        }

        stringBuilder.append("]");

        return stringBuilder.toString();
    }
}
