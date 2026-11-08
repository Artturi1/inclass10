# Temperature Converter

A JavaFX temperature converter with English, Finnish, Swedish, Japanese, and Arabic interfaces. Conversion history and unit translations are stored locally in SQLite.

## Database localization approach

This application uses a **normalized translation-table approach**: measurement facts and language-specific text are stored separately instead of adding one name column for every supported language or duplicating unit rows. The schema has three tables. `measurement_unit` stores each stable unit code and its symbol. `unit_translation` stores one row per `(unit_code, language_code)`, with a localized unit name and description; that pair is the primary key and the unit code is a foreign key. `conversion_history` stores the entered value, source and destination unit codes, converted value, selected language, and database-generated UTC timestamp. The `init.sql` script creates the tables and indexes and seeds Celsius and Fahrenheit translations for `en`, `fi`, `sv`, `ja`, and `ar`.

The DAO queries the selected language and left-joins English as the fallback, so missing translations do not prevent a unit from appearing. Keeping codes separate from display text makes saved conversions stable if a translation changes, and avoids schema changes when adding a language. The normalized design also keeps the same unit metadata consistent across all languages. SQLite is embedded, so no database server is needed for a desktop install or the GUI container; the database file defaults to `~/.temperature-converter/measurements.db` and can be relocated with `TEMPERATURE_DB_PATH`. The GUI requests the selected language's names from the DAO for both dropdowns and conversion output.

## Build and tests

Requirements: JDK 17+ and Maven 3.9+. Run `mvn clean verify`. This runs JUnit tests, creates `target/site/jacoco/index.html`, and fails verification if line coverage of `TemperatureMath` and `MeasurementDao` is below 80%. The in-memory DAO tests include Japanese localization, missing-translation English fallback, persisted timestamps, and latest-five ordering.




