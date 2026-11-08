import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.geometry.NodeOrientation;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

public class TemperatureConverterController {
    @FXML private ComboBox<String> languageChoice;
    @FXML private ComboBox<TemperatureConverter.Unit> fromUnitChoice;
    @FXML private ComboBox<TemperatureConverter.Unit> toUnitChoice;
    @FXML private TextField temperatureInput;
    @FXML private Label resultLabel;
    @FXML private Label statusLabel;
    @FXML private ListView<String> historyList;
    @FXML private VBox root;

    private TemperatureConverter application;
    private ResourceBundle messages;
    private final Map<String, String> unitNames = new HashMap<>();

    @FXML
    private void initialize() {
        messages = ResourceBundle.getBundle("i18n.messages", Locale.ENGLISH);
        languageChoice.getItems().setAll("English", "Suomi", "Svenska", "日本語", "العربية");
        languageChoice.getSelectionModel().select(0);
        fromUnitChoice.getItems().setAll(TemperatureConverter.Unit.values());
        toUnitChoice.getItems().setAll(TemperatureConverter.Unit.values());
        fromUnitChoice.setValue(TemperatureConverter.Unit.FAHRENHEIT);
        toUnitChoice.setValue(TemperatureConverter.Unit.CELSIUS);
        fromUnitChoice.setConverter(unitConverter());
        toUnitChoice.setConverter(unitConverter());
    }

    void setApplication(TemperatureConverter application) {
        this.application = application;
        messages = ResourceBundle.getBundle("i18n.messages", applicationLocale());
        refreshLocalizedUnits();
        fromUnitChoice.setConverter(unitConverter());
        toUnitChoice.setConverter(unitConverter());
        languageChoice.getSelectionModel().select(languageIndex(applicationLocale()));
        updateDirection();
        refreshHistory();
    }

    @FXML
    private void onLanguageChanged() throws IOException {
        if (application == null || languageChoice.getValue() == null) return;
        int selectedIndex = languageChoice.getSelectionModel().getSelectedIndex();
        if (selectedIndex != languageIndex(applicationLocale())) {
            application.changeLanguage(localeFor(selectedIndex));
        }
    }

    @FXML
    private void onConvert() {
        try {
            if (fromUnitChoice.getValue() == null || toUnitChoice.getValue() == null) {
                resultLabel.setText(messages.getString("error.number"));
                statusLabel.setText(" ");
                return;
            }
            double value = Double.parseDouble(temperatureInput.getText().trim());
            double converted = application.convertTemperature(value, fromUnitChoice.getValue(), toUnitChoice.getValue());
            resultLabel.setText(String.format(applicationLocale(), "%.1f %s = %.1f %s",
                    value, unitName(fromUnitChoice.getValue()), converted, unitName(toUnitChoice.getValue())));
            double celsius = application.convertTemperature(converted, toUnitChoice.getValue(), TemperatureConverter.Unit.CELSIUS);
            boolean extreme = application.isExtremeTemperature(celsius);
            statusLabel.setText(messages.getString(extreme ? "status.extreme" : "status.normal"));
            statusLabel.setStyle(extreme ? "-fx-text-fill: #b91c1c;" : "-fx-text-fill: #15803d;");
            application.getMeasurementDao().saveConversion(value,
                    fromUnitChoice.getValue().name(), toUnitChoice.getValue().name(), converted,
                    applicationLocale().getLanguage());
            refreshHistory();
        } catch (NumberFormatException exception) {
            resultLabel.setText(messages.getString("error.number"));
            statusLabel.setText(" ");
        } catch (IllegalArgumentException exception) {
            resultLabel.setText(messages.getString("error.number"));
            statusLabel.setText(" ");
        } catch (SQLException exception) {
            resultLabel.setText(messages.getString("error.database"));
            statusLabel.setText(" ");
        }
    }

    private void refreshLocalizedUnits() {
        if (application == null) return;
        try {
            unitNames.clear();
            for (MeasurementDao.LocalizedUnit unit
                    : application.getMeasurementDao().findUnits(applicationLocale().getLanguage())) {
                unitNames.put(unit.code(), unit.name());
            }
        } catch (SQLException exception) {
            statusLabel.setText(messages.getString("error.database"));
        }
    }

    private void refreshHistory() {
        if (application == null || historyList == null) return;
        try {
            historyList.getItems().setAll(application.getMeasurementDao().findLatestConversions(5).stream()
                    .map(record -> String.format(applicationLocale(), "%.1f %s → %.1f %s  ·  %s",
                            record.value(), unitNames.getOrDefault(record.fromUnit(), record.fromUnit()),
                            record.result(), unitNames.getOrDefault(record.toUnit(), record.toUnit()),
                            record.convertedAt()))
                    .toList());
        } catch (SQLException exception) {
            statusLabel.setText(messages.getString("error.database"));
        }
    }

    private String unitName(TemperatureConverter.Unit unit) {
        return unitNames.getOrDefault(unit.name(), unit.name());
    }

    private javafx.util.StringConverter<TemperatureConverter.Unit> unitConverter() {
        return new javafx.util.StringConverter<>() {
            @Override public String toString(TemperatureConverter.Unit unit) {
                return unitName(unit);
            }
            @Override public TemperatureConverter.Unit fromString(String value) { return null; }
        };
    }

    private Locale applicationLocale() {
        if (application == null) return Locale.ENGLISH;
        return application.getLocale();
    }

    private void updateDirection() {
        if (root != null) root.setNodeOrientation(applicationLocale().getLanguage().equals("ar")
                ? NodeOrientation.RIGHT_TO_LEFT : NodeOrientation.LEFT_TO_RIGHT);
    }

    private int languageIndex(Locale locale) {
        return switch (locale.getLanguage()) {
            case "fi" -> 1;
            case "sv" -> 2;
            case "ja" -> 3;
            case "ar" -> 4;
            default -> 0;
        };
    }

    private Locale localeFor(int index) {
        return switch (index) {
            case 1 -> Locale.forLanguageTag("fi");
            case 2 -> Locale.forLanguageTag("sv");
            case 3 -> Locale.forLanguageTag("ja");
            case 4 -> Locale.forLanguageTag("ar");
            default -> Locale.ENGLISH;
        };
    }

}
