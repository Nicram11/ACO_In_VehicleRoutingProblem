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

        System.out.println("Hello, World!");
        String filePath = "./src/cities/A-n32-k5.vrp";
        var cities = City.fetchCities(filePath);
        VrpAco aco = new VrpAco(cities);
        aco.runOptimization();
        System.out.println("bestTour" + aco.getBestTour());

        System.out.println("bestTourLength" + aco.getBestTourLength());
        // System.out.println(cities);
    }
}
