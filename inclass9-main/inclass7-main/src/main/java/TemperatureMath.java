/** Pure temperature-conversion rules, separated from JavaFX for reliable unit testing. */
public final class TemperatureMath {
    private TemperatureMath() {
    }

    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32) * 5 / 9;
    }

    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9 / 5) + 32;
    }

    public static double convert(double value, TemperatureConverter.Unit from,
                                 TemperatureConverter.Unit to) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Temperature must be a finite number.");
        }
        if (from == null || to == null) {
            throw new IllegalArgumentException("Temperature units must not be null.");
        }
        if (from == to) {
            return value;
        }
        return from == TemperatureConverter.Unit.FAHRENHEIT
                ? fahrenheitToCelsius(value)
                : celsiusToFahrenheit(value);
    }

    public static boolean isExtremeCelsius(double celsius) {
        return celsius < -40 || celsius > 50;
    }
}
