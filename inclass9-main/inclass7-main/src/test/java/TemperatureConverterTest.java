import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemperatureConverterTest {

    private final TemperatureConverter converter = new TemperatureConverter();

    @Test
    void testFahrenheitToCelsius() {
        assertEquals(0.0, converter.fahrenheitToCelsius(32), 0.001);
        assertEquals(100.0, converter.fahrenheitToCelsius(212), 0.001);
        assertEquals(-40.0, converter.fahrenheitToCelsius(-40), 0.001);
    }

    @Test
    void testCelsiusToFahrenheit() {
        assertEquals(32.0, converter.celsiusToFahrenheit(0), 0.001);
        assertEquals(212.0, converter.celsiusToFahrenheit(100), 0.001);
        assertEquals(-40.0, converter.celsiusToFahrenheit(-40), 0.001);
    }

    @Test
    void testIsExtremeTemperature() {
        assertTrue(converter.isExtremeTemperature(-41));
        assertFalse(converter.isExtremeTemperature(-40));
        assertFalse(converter.isExtremeTemperature(50));
        assertTrue(converter.isExtremeTemperature(51));
        assertFalse(converter.isExtremeTemperature(20));
    }

        @Test
        void testConvertTemperatureBetweenUnits() {
        assertEquals(0.0, converter.convertTemperature(32,
            TemperatureConverter.Unit.FAHRENHEIT, TemperatureConverter.Unit.CELSIUS), 0.001);
        assertEquals(212.0, converter.convertTemperature(100,
            TemperatureConverter.Unit.CELSIUS, TemperatureConverter.Unit.FAHRENHEIT), 0.001);
        assertEquals(20.0, converter.convertTemperature(20,
            TemperatureConverter.Unit.CELSIUS, TemperatureConverter.Unit.CELSIUS), 0.001);
        }

        @Test
        void testConvertTemperatureRejectsNonFiniteAndNullUnits() {
        assertThrows(IllegalArgumentException.class, () -> converter.convertTemperature(
            Double.NaN, TemperatureConverter.Unit.CELSIUS, TemperatureConverter.Unit.FAHRENHEIT));
        assertThrows(IllegalArgumentException.class, () -> converter.convertTemperature(
            10, null, TemperatureConverter.Unit.CELSIUS));
        }

            @Test
            void temperatureMathCoversBothConversionsAndThresholdBoundaries() {
            assertEquals(0.0, TemperatureMath.convert(32, TemperatureConverter.Unit.FAHRENHEIT,
                TemperatureConverter.Unit.CELSIUS), 0.001);
            assertEquals(212.0, TemperatureMath.convert(100, TemperatureConverter.Unit.CELSIUS,
                TemperatureConverter.Unit.FAHRENHEIT), 0.001);
            assertEquals(12.5, TemperatureMath.convert(12.5, TemperatureConverter.Unit.CELSIUS,
                TemperatureConverter.Unit.CELSIUS), 0.001);
            assertTrue(TemperatureMath.isExtremeCelsius(-40.1));
            assertFalse(TemperatureMath.isExtremeCelsius(-40));
            assertFalse(TemperatureMath.isExtremeCelsius(50));
            assertTrue(TemperatureMath.isExtremeCelsius(50.1));
            assertThrows(IllegalArgumentException.class, () -> TemperatureMath.convert(
                Double.POSITIVE_INFINITY, TemperatureConverter.Unit.CELSIUS,
                TemperatureConverter.Unit.FAHRENHEIT));
            assertThrows(IllegalArgumentException.class, () -> TemperatureMath.convert(
                1, TemperatureConverter.Unit.CELSIUS, null));
            }
}
