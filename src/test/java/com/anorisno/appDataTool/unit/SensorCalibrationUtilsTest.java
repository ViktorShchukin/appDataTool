package com.anorisno.appDataTool.unit;

import com.anorisno.appDataTool.model.ExperimentData;
import com.anorisno.appDataTool.model.SensorData;
import com.anorisno.appDataTool.utils.SensorCalibrationUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the supplied SensorCalibrationUtils(1).java.
 * Place at src/test/java/com/anorisno/appDataTool/utils/SensorCalibrationUtilsTest.java.
 * Requires JUnit Jupiter (BOM 6.1.3) and Mockito Core (e.g. 5.20.0), Java 17+.
 * No Spring context is needed. Production code is not mocked.
 *
 * This suite covers supported inputs and the existing documented rejections.
 * It does not endorse current acceptance of NaN/infinity or invalid quaternions;
 * those inputs need an explicit validation contract in the production class.
 */
class SensorCalibrationUtilsTest {
    private static final double EPS = 1e-12;
    private static final double HALF_SQRT_TWO = Math.sqrt(0.5);

    @TempDir
    Path tempDir;

    // Known axis values; no dependency on ExperimentData's constructor or setters.
    private static ExperimentData sample(double x, double y, double z) {
        ExperimentData data = mock(ExperimentData.class);
        when(data.getAccx()).thenReturn(x);
        when(data.getAccy()).thenReturn(y);
        when(data.getAccz()).thenReturn(z);
        return data;
    }

    private static List<ExperimentData> samples() {
        return List.of(sample(1, 10, 100), sample(2, 12, 103), sample(3, 14, 106));
    }

    @ParameterizedTest(name = "axis {0} returns {1}")
    @CsvSource({"x, 1.25", "X, 1.25", "y, -2.5", "Y, -2.5", "z, 3.75", "Z, 3.75"})
    void getAxisValueSupportsEachAxisAndUppercase(String axis, double expected) {
        assertEquals(expected, SensorCalibrationUtils.getAxisValue(sample(1.25, -2.5, 3.75), axis), EPS);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "w", "accx"})
    void getAxisValueRejectsUnknownAxis(String axis) {
        assertThrows(IllegalArgumentException.class,
                () -> SensorCalibrationUtils.getAxisValue(sample(1, 2, 3), axis));
    }

    @ParameterizedTest(name = "sample standard deviation on {0} is {1}")
    @CsvSource({"x, 1.0", "y, 2.0", "z, 3.0"})
    void standardDeviationUsesSampleVariance(String axis, double expected) {
        // The n-1 denominator gives 1 for [1,2,3]; a population denominator would not.
        assertEquals(expected, SensorCalibrationUtils.calculateStandardDeviation(samples(), axis), EPS);
    }

    @Test
    void standardDeviationOfConstantSamplesIsZero() {
        var data = List.of(sample(7, 0, 0), sample(7, 0, 0), sample(7, 0, 0));
        assertEquals(0, SensorCalibrationUtils.calculateStandardDeviation(data, "x"), EPS);
    }

    @Test
    void standardDeviationIsUnchangedByConstantOffset() {
        var data = List.of(sample(1000001, 0, 0), sample(1000002, 0, 0), sample(1000003, 0, 0));
        assertEquals(1, SensorCalibrationUtils.calculateStandardDeviation(data, "x"), EPS);
    }

    @Test
    void standardDeviationRejectsEmptyInput() {
        assertThrows(IllegalArgumentException.class,
                () -> SensorCalibrationUtils.calculateStandardDeviation(List.of(), "x"));
    }

    @Test
    void standardDeviationRejectsSingleSample() {
        assertThrows(IllegalArgumentException.class,
                () -> SensorCalibrationUtils.calculateStandardDeviation(List.of(sample(1, 2, 3)), "x"));
    }

    @ParameterizedTest(name = "axis={0}, tolerance={1}, required={2}")
    @CsvSource({"x, 0.1, 385", "x, 1.0, 4", "y, 0.1, 1537"})
    void requiredSamplesRoundsUpConfidenceEstimate(String axis, double tolerance, int expected) {
        var utils = new SensorCalibrationUtils();
        assertEquals(expected, utils.calculateRequiredSamples(samples(), axis, tolerance));
    }

    @Test
    void requiredSamplesHasMinimumOfTwoForConstantData() {
        var data = List.of(sample(2, 0, 0), sample(2, 0, 0));
        assertEquals(2, new SensorCalibrationUtils().calculateRequiredSamples(data, "x", 0.01));
    }

    @Test
    void requiredSamplesHasMinimumOfTwoForLargeFiniteTolerance() {
        assertEquals(2, new SensorCalibrationUtils().calculateRequiredSamples(samples(), "x", 100));
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.01, -1.0})
    void requiredSamplesRejectsNonPositiveTolerance(double tolerance) {
        assertThrows(IllegalArgumentException.class,
                () -> new SensorCalibrationUtils().calculateRequiredSamples(samples(), "x", tolerance));
    }

    @Test
    void requiredSamplesRejectsInsufficientData() {
        assertThrows(IllegalArgumentException.class,
                () -> new SensorCalibrationUtils().calculateRequiredSamples(List.of(), "x", 0.1));
    }

    @Test
    void histogramCountsPositiveNegativeAndZeroBins() {
        var values = List.of(0.0011, 0.0014, 0.0016, -0.0011, -0.0014, -0.0016, 0.0);
        var histogram = SensorCalibrationUtils.getHistogram(values);
        assertEquals(Map.of(-0.002, 1, -0.001, 2, 0.0, 1, 0.001, 2, 0.002, 1), histogram);
        assertEquals(values.size(), histogram.values().stream().mapToInt(Integer::intValue).sum());
    }

    @Test
    void histogramReturnsBinsInNumericOrder() {
        var histogram = SensorCalibrationUtils.getHistogram(List.of(0.3, -0.2, 0.1));
        assertEquals(List.of(-0.2, 0.1, 0.3), new ArrayList<>(histogram.keySet()));
    }

    @Test
    void histogramOfEmptyDataIsEmpty() {
        assertTrue(SensorCalibrationUtils.getHistogram(List.of()).isEmpty());
    }

    @ParameterizedTest(name = "vector norm case {index}: {1}")
    @MethodSource("normCases")
    void normMatchesKnownLengths(double[] vector, double expected) {
        assertEquals(expected, SensorCalibrationUtils.getNorm(vector), EPS);
    }

    static Stream<Arguments> normCases() {
        return Stream.of(
                Arguments.of(new double[]{3, 4, 0}, 5.0),
                Arguments.of(new double[]{-3, -4}, 5.0),
                Arguments.of(new double[]{0, 0, 0}, 0.0),
                Arguments.of(new double[]{}, 0.0),
                Arguments.of(new double[]{1, 2, 2, 4}, 5.0),
                Arguments.of(new double[]{0.3, 0.4}, 0.5));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("rotationCases")
    void rotationMatrixMatchesKnownRotations(String description, double[] quaternion, double[] expected) {
        // Android rotation-vector order is [x, y, z, w]; the matrix is row-major.
        assertArrayEquals(expected, SensorCalibrationUtils.getRotationMatrixFromVector(quaternion), EPS);
    }

    static Stream<Arguments> rotationCases() {
        double h = HALF_SQRT_TWO;
        double[] identity = {1, 0, 0, 0, 1, 0, 0, 0, 1};
        return Stream.of(
                Arguments.of("three-component identity", new double[]{0, 0, 0}, identity),
                Arguments.of("four-component identity", new double[]{0, 0, 0, 1}, identity),
                Arguments.of("+90 degrees around X", new double[]{h, 0, 0, h},
                        new double[]{1, 0, 0, 0, 0, -1, 0, 1, 0}),
                Arguments.of("+90 degrees around Y", new double[]{0, h, 0, h},
                        new double[]{0, 0, 1, 0, 1, 0, -1, 0, 0}),
                Arguments.of("+90 degrees around Z", new double[]{0, 0, h, h},
                        new double[]{0, -1, 0, 1, 0, 0, 0, 0, 1}),
                Arguments.of("reconstructed positive w", new double[]{0, 0, h},
                        new double[]{0, -1, 0, 1, 0, 0, 0, 0, 1}),
                Arguments.of("negative supplied w", new double[]{0, 0, h, -h},
                        new double[]{0, 1, 0, -1, 0, 0, 0, 0, 1}),
                Arguments.of("180 degrees around X", new double[]{1, 0, 0, 0},
                        new double[]{1, 0, 0, 0, -1, 0, 0, 0, -1}));
    }

    @Test
    void rotationMatrixIsUnchangedWhenQuaternionSignIsFlipped() {
        var positive = SensorCalibrationUtils.getRotationMatrixFromVector(new double[]{0.5, 0.5, 0.5, 0.5});
        var negative = SensorCalibrationUtils.getRotationMatrixFromVector(new double[]{-0.5, -0.5, -0.5, -0.5});
        assertArrayEquals(positive, negative, EPS);
    }

    @Test
    void rotationMatrixPreservesOrthonormalityAndHandedness() {
        double[] r = SensorCalibrationUtils.getRotationMatrixFromVector(
                new double[]{0.1, 0.2, 0.3, Math.sqrt(0.86)});
        assertEquals(9, r.length);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                double dot = 0;
                for (int k = 0; k < 3; k++) dot += r[3*i+k] * r[3*j+k];
                assertEquals(i == j ? 1.0 : 0.0, dot, EPS);
            }
        }
        double determinant = r[0]*(r[4]*r[8]-r[5]*r[7])
                - r[1]*(r[3]*r[8]-r[5]*r[6]) + r[2]*(r[3]*r[7]-r[4]*r[6]);
        assertEquals(1, determinant, EPS);
    }

    @Test
    void rotationMatrixIgnoresOptionalFifthAccuracyComponent() {
        double h = HALF_SQRT_TWO;
        var matrix = SensorCalibrationUtils.getRotationMatrixFromVector(new double[]{0, 0, h, h, 0.25});
        assertArrayEquals(new double[]{0, -1, 0, 1, 0, 0, 0, 0, 1}, matrix, EPS);
    }

    @Test
    void csvParsesHeaderWhitespaceAndBothSensorTypes() throws IOException {
        Path csv = writeCsv("""

                  sensortype;timestamp;x;y;z
                TYPE_LINEAR_ACCELERATION;1000000000;1.25;-2.5;0.03

                 TYPE_ROTATION_VECTOR ; 1000000001 ; 0.0 ; 0.0 ; 0.7071067811865476
                """);
        var records = SensorCalibrationUtils.parseCsvFile(csv.toFile());
        assertEquals(2, records.size());
        assertRecord(records.get(0), SensorData.SensorType.TYPE_LINEAR_ACCELERATION,
                1000000000, new double[]{1.25, -2.5, 0.03});
        assertRecord(records.get(1), SensorData.SensorType.TYPE_ROTATION_VECTOR,
                1000000001, new double[]{0, 0, HALF_SQRT_TWO});
    }

    @Test
    void csvWithoutHeaderKeepsFirstRecordAndAcceptsScientificNotation() throws IOException {
        Path csv = writeCsv("TYPE_LINEAR_ACCELERATION;1000000000;1e-3;-2e-3;3e-3\n");
        var records = SensorCalibrationUtils.parseCsvFile(csv.toFile());
        assertEquals(1, records.size());
        assertRecord(records.get(0), SensorData.SensorType.TYPE_LINEAR_ACCELERATION,
                1000000000, new double[]{0.001, -0.002, 0.003});
    }

    @Test
    void csvSkipsMalformedRowsAndContinuesWithValidRows() throws IOException {
        Path csv = writeCsv("""
                sensortype;timestamp;x;y;z
                TYPE_LINEAR_ACCELERATION;1;0;0;0
                TOO_FEW;1;2
                UNKNOWN_SENSOR;2;0;0;0
                TYPE_LINEAR_ACCELERATION;bad-time;0;0;0
                TYPE_LINEAR_ACCELERATION;3;bad-x;0;0
                TYPE_LINEAR_ACCELERATION;4;0;0;
                TYPE_LINEAR_ACCELERATION;5;1;2;3
                """);
        var records = SensorCalibrationUtils.parseCsvFile(csv.toFile());
        assertEquals(2, records.size());
        assertRecord(records.get(0), SensorData.SensorType.TYPE_LINEAR_ACCELERATION, 1, new double[3]);
        assertRecord(records.get(1), SensorData.SensorType.TYPE_LINEAR_ACCELERATION, 5, new double[]{1, 2, 3});
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "\n  \n", "sensortype;timestamp;x;y;z\n"})
    void csvWithoutMeasurementsReturnsEmptyList(String contents) throws IOException {
        assertTrue(SensorCalibrationUtils.parseCsvFile(writeCsv(contents).toFile()).isEmpty());
    }

    @Test
    void csvPreservesFileOrder() throws IOException {
        Path csv = writeCsv("""
                TYPE_LINEAR_ACCELERATION;20;1;0;0
                TYPE_LINEAR_ACCELERATION;10;2;0;0
                """);
        var records = SensorCalibrationUtils.parseCsvFile(csv.toFile());
        assertEquals(20, records.get(0).timestamp(), EPS);
        assertEquals(10, records.get(1).timestamp(), EPS);
    }

    @Test
    void csvMissingFileThrowsIOException() {
        assertThrows(IOException.class,
                () -> SensorCalibrationUtils.parseCsvFile(tempDir.resolve("missing.csv").toFile()));
    }

    private Path writeCsv(String contents) throws IOException {
        return Files.writeString(tempDir.resolve("samples.csv"), contents);
    }

    private static void assertRecord(SensorData actual, SensorData.SensorType type,
                                     double timestamp, double[] values) {
        assertAll(
                () -> assertEquals(type, actual.type()),
                () -> assertEquals(timestamp, actual.timestamp(), EPS),
                () -> assertArrayEquals(values, actual.data(), EPS));
    }
}
