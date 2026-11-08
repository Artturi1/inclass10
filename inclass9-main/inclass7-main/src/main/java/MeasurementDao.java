import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** JDBC access to measurement-unit translations and saved conversions. */
public class MeasurementDao {
    private static final String UNIT_QUERY = """
            SELECT u.unit_code, u.symbol,
                   COALESCE(selected.unit_name, english.unit_name) AS unit_name,
                   COALESCE(selected.description, english.description) AS description
            FROM measurement_unit u
            LEFT JOIN unit_translation selected
              ON selected.unit_code = u.unit_code AND selected.language_code = ?
            LEFT JOIN unit_translation english
              ON english.unit_code = u.unit_code AND english.language_code = 'en'
            ORDER BY u.unit_code
            """;

    private final Connection connection;

    public MeasurementDao(Connection connection) {
        this.connection = connection;
    }

    public List<LocalizedUnit> findUnits(String languageCode) throws SQLException {
        String language = languageCode == null || languageCode.isBlank()
                ? "en" : Locale.forLanguageTag(languageCode).getLanguage();
        List<LocalizedUnit> units = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(UNIT_QUERY)) {
            statement.setString(1, language.isBlank() ? "en" : language);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    units.add(new LocalizedUnit(
                            results.getString("unit_code"),
                            results.getString("symbol"),
                            results.getString("unit_name"),
                            results.getString("description")));
                }
            }
        }
        return List.copyOf(units);
    }

    public void saveConversion(double value, String fromUnit, String toUnit,
                               double result, String languageCode) throws SQLException {
        String sql = "INSERT INTO conversion_history"
                + "(input_value, from_unit, to_unit, result_value, language_code)"
                + " VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, value);
            statement.setString(2, fromUnit);
            statement.setString(3, toUnit);
            statement.setDouble(4, result);
            statement.setString(5, languageCode == null || languageCode.isBlank()
                    ? "en" : Locale.forLanguageTag(languageCode).getLanguage());
            statement.executeUpdate();
        }
    }

    public List<ConversionRecord> findLatestConversions(int limit) throws SQLException {
        if (limit < 1) {
            throw new IllegalArgumentException("History limit must be positive.");
        }
        String sql = "SELECT input_value, from_unit, to_unit, result_value, language_code, converted_at "
                + "FROM conversion_history ORDER BY conversion_id DESC LIMIT ?";
        List<ConversionRecord> records = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, limit);
            try (ResultSet results = statement.executeQuery()) {
                while (results.next()) {
                    records.add(new ConversionRecord(
                            results.getDouble("input_value"),
                            results.getString("from_unit"),
                            results.getString("to_unit"),
                            results.getDouble("result_value"),
                            results.getString("language_code"),
                            results.getString("converted_at")));
                }
            }
        }
        return List.copyOf(records);
    }

    public record LocalizedUnit(String code, String symbol, String name, String description) {
        @Override
        public String toString() {
            return name;
        }
    }

    public record ConversionRecord(double value, String fromUnit, String toUnit,
                                   double result, String languageCode, String convertedAt) {
    }
}
