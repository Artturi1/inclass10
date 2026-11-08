import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MeasurementDatabaseTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void initializesSchemaAndSeedsAllFiveLanguages() throws Exception {
        String previousHome = System.getProperty("user.home");
        System.setProperty("user.home", temporaryDirectory.toString());
        try (Connection connection = MeasurementDatabase.open()) {
            MeasurementDao dao = new MeasurementDao(connection);
            for (String language : List.of("en", "fi", "sv", "ja", "ar")) {
                List<MeasurementDao.LocalizedUnit> units = dao.findUnits(language);
                assertEquals(2, units.size(), language);
                assertFalse(units.get(0).name().isBlank(), language);
                assertFalse(units.get(0).description().isBlank(), language);
            }
        } finally {
            if (previousHome == null) {
                System.clearProperty("user.home");
            } else {
                System.setProperty("user.home", previousHome);
            }
        }
    }
}
