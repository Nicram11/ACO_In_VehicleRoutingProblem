import java.util.logging.Filter;
import java.util.logging.LogRecord;
import java.util.logging.Level;

class ImportantLogFilter implements Filter {
    @Override
    public boolean isLoggable(LogRecord record) {

        return record.getLevel().equals(Level.SEVERE);
    }
}

class LessImportantLogFilter implements Filter {
    @Override
    public boolean isLoggable(LogRecord record) {

        return record.getLevel().intValue() <= Level.INFO.intValue();
    }
}
