import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class App {
    public static final Logger LOGGER = Logger.getLogger(App.class.getName());
    static {
        try {

            FileHandler allLogsHandler = new FileHandler("all_logs.log", true);
            allLogsHandler.setFormatter(new SimpleFormatter());

            FileHandler importantLogsHandler = new FileHandler("important_logs.log", true);
            importantLogsHandler.setFormatter(new SimpleFormatter());
            importantLogsHandler.setFilter(new ImportantLogFilter());

            FileHandler lessImportantLogsHandler = new FileHandler("less_important_logs.log", true);
            lessImportantLogsHandler.setFormatter(new SimpleFormatter());
            lessImportantLogsHandler.setFilter(new LessImportantLogFilter());

            LOGGER.addHandler(allLogsHandler);
            LOGGER.addHandler(importantLogsHandler);
            LOGGER.addHandler(lessImportantLogsHandler);

        } catch (IOException e) {
            LOGGER.warning("Failed to initialize logger file handlers");
        }
    }

    public static void main(String[] args) throws Exception {

        String fileName = "F-n135-k7.vrp";
        String folderPath = "./src/cities/";
        var cities = City.fetchCities(folderPath + fileName);
        long startTime = System.currentTimeMillis();

        VrpAco aco = new VrpAco(cities);
        aco.runOptimization();
        long endTime = System.currentTimeMillis();
        double executionTime = (endTime - startTime) / 1000.0;

        LOGGER.severe("[RESULT] [ " + fileName + "] bestTour: "
                + CityListToStringConverter.convertCityIdsToString(aco.getBestTour()) + " \r\nexecTime:"
                + executionTime + "[s] \r\nLength: "
                + aco.getBestTourLength());
        ;

        // System.out.println(CityListToStringConverter.convertCitiesToDetailedString(cities));
    }

}
