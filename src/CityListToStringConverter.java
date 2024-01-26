import java.util.List;

public class CityListToStringConverter {
    public static String convertCitiesToString(List<City> cities) {
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
}
