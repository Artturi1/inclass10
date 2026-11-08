-- Temperature measurement reference data and conversion history (SQLite).
PRAGMA foreign_keys = ON;

CREATE TABLE IF NOT EXISTS measurement_unit (
    unit_code TEXT PRIMARY KEY,
    symbol TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS unit_translation (
    unit_code TEXT NOT NULL,
    language_code TEXT NOT NULL,
    unit_name TEXT NOT NULL,
    description TEXT NOT NULL,
    PRIMARY KEY (unit_code, language_code),
    FOREIGN KEY (unit_code) REFERENCES measurement_unit(unit_code)
);

CREATE TABLE IF NOT EXISTS conversion_history (
    conversion_id INTEGER PRIMARY KEY AUTOINCREMENT,
    input_value REAL NOT NULL,
    from_unit TEXT NOT NULL,
    to_unit TEXT NOT NULL,
    result_value REAL NOT NULL,
    language_code TEXT NOT NULL,
    converted_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
    FOREIGN KEY (from_unit) REFERENCES measurement_unit(unit_code),
    FOREIGN KEY (to_unit) REFERENCES measurement_unit(unit_code)
);

CREATE INDEX IF NOT EXISTS idx_conversion_history_time
    ON conversion_history(converted_at DESC, conversion_id DESC);

INSERT OR IGNORE INTO measurement_unit(unit_code, symbol) VALUES
    ('CELSIUS', '°C'),
    ('FAHRENHEIT', '°F');

INSERT OR IGNORE INTO unit_translation(unit_code, language_code, unit_name, description) VALUES
    ('CELSIUS', 'en', 'Celsius', 'Temperature scale where water freezes at 0 degrees.'),
    ('FAHRENHEIT', 'en', 'Fahrenheit', 'Temperature scale where water freezes at 32 degrees.'),
    ('CELSIUS', 'fi', 'Celsius-aste', 'Lämpötila-asteikko, jossa vesi jäätyy 0 asteessa.'),
    ('FAHRENHEIT', 'fi', 'Fahrenheit-aste', 'Lämpötila-asteikko, jossa vesi jäätyy 32 asteessa.'),
    ('CELSIUS', 'sv', 'Celsius', 'Temperaturskala där vatten fryser vid 0 grader.'),
    ('FAHRENHEIT', 'sv', 'Fahrenheit', 'Temperaturskala där vatten fryser vid 32 grader.'),
    ('CELSIUS', 'ja', 'セルシウス度', '水が0度で凍る温度尺度です。'),
    ('FAHRENHEIT', 'ja', '華氏', '水が32度で凍る温度尺度です。'),
    ('CELSIUS', 'ar', 'درجة مئوية', 'مقياس حرارة يتجمد فيه الماء عند 0 درجة.'),
    ('FAHRENHEIT', 'ar', 'درجة فهرنهايت', 'مقياس حرارة يتجمد فيه الماء عند 32 درجة.');
