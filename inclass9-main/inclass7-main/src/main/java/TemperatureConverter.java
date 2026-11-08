import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;

public class TemperatureConverter extends Application {
    public enum Unit {
        CELSIUS,
        FAHRENHEIT
    }

    private Stage stage;
    private Locale locale = Locale.ENGLISH;
    private Connection databaseConnection;
    private MeasurementDao measurementDao;

    public Locale getLocale() {
        return locale;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            double fahrenheit = Double.parseDouble(args[0]);
            TemperatureConverter converter = new TemperatureConverter();
            System.out.println(fahrenheit + " F = " + converter.fahrenheitToCelsius(fahrenheit) + " C");
            return;
        }
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        stage = primaryStage;
        try {
            databaseConnection = MeasurementDatabase.open();
            measurementDao = new MeasurementDao(databaseConnection);
        } catch (SQLException exception) {
            throw new IOException("Could not open the measurement database.", exception);
        }
        showLocalizedView();
        stage.setMinWidth(420);
        stage.setMinHeight(620);
        stage.show();
    }

    void changeLanguage(Locale newLocale) throws IOException {
        locale = newLocale;
        showLocalizedView();
    }

    private void showLocalizedView() throws IOException {
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/temperature-converter.fxml"), bundle);
        Parent root = loader.load();
        TemperatureConverterController controller = loader.getController();
        controller.setApplication(this);
        stage.setTitle(bundle.getString("app.title"));
        stage.setScene(new Scene(root, 500, 660));
    }

    MeasurementDao getMeasurementDao() {
        return measurementDao;
    }

    @Override
    public void stop() throws SQLException {
        if (databaseConnection != null && !databaseConnection.isClosed()) {
            databaseConnection.close();
        }
    }

    public double fahrenheitToCelsius(double fahrenheit) {
        return TemperatureMath.fahrenheitToCelsius(fahrenheit);
    }

    public double convertTemperature(double value, Unit from, Unit to) {
        return TemperatureMath.convert(value, from, to);
    }

    public double celsiusToFahrenheit(double celsius) {
        return TemperatureMath.celsiusToFahrenheit(celsius);
    }

    public boolean isExtremeTemperature(double celsius) {
        return TemperatureMath.isExtremeCelsius(celsius);
    }
}
