import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Creates the local SQLite database and applies the bundled schema/seed script. */
public final class MeasurementDatabase {
    private MeasurementDatabase() {
    }

    public static Connection open() throws SQLException, IOException {
        String configuredPath = System.getenv("TEMPERATURE_DB_PATH");
        Path databasePath = configuredPath == null || configuredPath.isBlank()
                ? Path.of(System.getProperty("user.home"), ".temperature-converter", "measurements.db")
                : Path.of(configuredPath);
        Path parent = databasePath.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
        try {
            initialize(connection);
            return connection;
        } catch (SQLException | IOException exception) {
            connection.close();
            throw exception;
        }
    }

    private static void initialize(Connection connection) throws SQLException, IOException {
        try (InputStream input = MeasurementDatabase.class.getResourceAsStream("/db/init.sql")) {
            if (input == null) {
                throw new IOException("Database initialization script /db/init.sql was not found.");
            }
            String script = new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            try (Statement statement = connection.createStatement()) {
                for (String sql : script.split(";")) {
                    String trimmed = sql.trim();
                    if (!trimmed.isEmpty()) {
                        statement.execute(trimmed);
                    }
                }
            }
        }
    }
}
