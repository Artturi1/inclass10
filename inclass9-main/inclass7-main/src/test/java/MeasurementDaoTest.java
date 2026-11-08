import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MeasurementDaoTest {
    private Connection connection;
    private MeasurementDao dao;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE measurement_unit (unit_code TEXT PRIMARY KEY, symbol TEXT NOT NULL)");
            statement.execute("CREATE TABLE unit_translation (unit_code TEXT NOT NULL, language_code TEXT NOT NULL, "
                    + "unit_name TEXT NOT NULL, description TEXT NOT NULL, PRIMARY KEY(unit_code, language_code), "
                    + "FOREIGN KEY(unit_code) REFERENCES measurement_unit(unit_code))");
            statement.execute("CREATE TABLE conversion_history (conversion_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "input_value REAL NOT NULL, from_unit TEXT NOT NULL, to_unit TEXT NOT NULL, "
                    + "result_value REAL NOT NULL, language_code TEXT NOT NULL, "
                    + "converted_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            statement.execute("INSERT INTO measurement_unit VALUES ('CELSIUS', '°C'), ('FAHRENHEIT', '°F')");
            statement.execute("INSERT INTO unit_translation VALUES "
                    + "('CELSIUS','en','Celsius','English Celsius description'), "
                    + "('FAHRENHEIT','en','Fahrenheit','English Fahrenheit description'), "
                    + "('CELSIUS','ja','セルシウス度','日本語の説明'), "
                    + "('FAHRENHEIT','ja','華氏','日本語の説明')");
        }
        dao = new MeasurementDao(connection);
    }

    @Test
    void returnsTranslationsForSelectedLanguage() throws Exception {
        List<MeasurementDao.LocalizedUnit> units = dao.findUnits("ja-JP");

        assertEquals(2, units.size());
        assertEquals("セルシウス度", units.get(0).name());
        assertEquals("日本語の説明", units.get(0).description());
        assertEquals("セルシウス度", units.get(0).toString());
    }

    @Test
    void fallsBackToEnglishWhenTranslationIsMissing() throws Exception {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM unit_translation "
                    + "WHERE unit_code='FAHRENHEIT' AND language_code='ja'");
        }

        MeasurementDao.LocalizedUnit fahrenheit = dao.findUnits("ja").stream()
                .filter(unit -> unit.code().equals("FAHRENHEIT"))
                .findFirst().orElseThrow();

        assertEquals("Fahrenheit", fahrenheit.name());
        assertEquals("English Fahrenheit description", fahrenheit.description());
        assertEquals("Fahrenheit", dao.findUnits("xx").get(1).name());
    }

    @Test
    void storesConversionsAndReturnsOnlyTheNewestRequestedRecords() throws Exception {
        for (int i = 1; i <= 6; i++) {
            dao.saveConversion(i, "FAHRENHEIT", "CELSIUS", (i - 32) * 5 / 9.0, "ar");
        }

        List<MeasurementDao.ConversionRecord> records = dao.findLatestConversions(5);

        assertEquals(5, records.size());
        assertEquals(6.0, records.get(0).value());
        assertEquals("ar", records.get(0).languageCode());
        assertTrue(records.get(0).convertedAt() != null && !records.get(0).convertedAt().isBlank());
        assertEquals(2.0, records.get(4).value());
    }

    @Test
    void rejectsNonPositiveHistoryLimit() {
        assertThrows(IllegalArgumentException.class, () -> dao.findLatestConversions(0));
    }

    @Test
    void defaultsBlankLanguageToEnglish() throws Exception {
        dao.saveConversion(32, "FAHRENHEIT", "CELSIUS", 0, " ");

        assertEquals("en", dao.findLatestConversions(1).get(0).languageCode());
    }
}
