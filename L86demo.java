

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.base.evolution.coalescent.populationmodel.*;


import java.io.*;
import java.util.*;

public class L86demo {

    public static void main(String[] args) {

        // Input/Output paths
        String logFilePath   = "../L86.log";
        String outputCsvPath = "../output_results.csv";

        System.out.println("Starting L86Demographic analysis...");
        System.out.println("Reading file: " + logFilePath);

        // We read these parameters from the log file
        // (If i=0 => ConstantGrowth needs N0_con, so you might add "N0_con" if not already in your log.)
        // If i=1 => Exponential => might need b, N_max, NA, I_na
        // If i=2 => Logistic => might need b, N_max, NA, t50, I_na
        // If i=3 => GompertzGrowth_f0 => might need b, f0, NA, N0(?), I_na
        // Make sure these match your log's column names exactly.
        String[] parameterNames = {
                "I",
                "N0_con",   // for i=0 => ConstantGrowth
                "b",
                "N_max",
                "NA",
                "t50",
                "f0",
                "x",
                "tau",
                "psi.height",
                "I_na"
        };

        // Choose how to get maxTime from psi.height
        String treeHeightMethod = "fixed";

        // Number of time bins
        int binCount = 100;

        try {
            // 1. Read parameter samples
            Map<String, List<Double>> parameterSamples = readParameterSamples(logFilePath, parameterNames);
            System.out.println("Parameters loaded successfully.");

            // 2. Check psi.height
            if (parameterSamples.get("psi.height") == null || parameterSamples.get("psi.height").isEmpty()) {
                throw new IllegalStateException("Error: psi.height parameter missing or empty.");
            }

            // 3. Determine max time
            List<Double> treeHeightSamples = parameterSamples.get("psi.height");
            double maxTime = determineMaxTime(treeHeightMethod, treeHeightSamples);
            System.out.println("Max Time determined: " + maxTime);

            // 4. Generate time points
            List<Double> timePoints = generateTimePoints(binCount, 0.0, maxTime);

            // Prepare data structure for collecting population sizes
            List<List<Double>> populationSizesAtTimePoints = new ArrayList<>();
            for (int i = 0; i < binCount; i++) {
                populationSizesAtTimePoints.add(new ArrayList<>());
            }

            // 5. Loop over MCMC samples: choose model based on i=0..3
            int sampleCount = parameterSamples.get("I").size();
            for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {

                double I     = getValue(parameterSamples, "I",       sampleIndex);
                double N0Con = getValue(parameterSamples, "N0_con",  sampleIndex);
                double b     = getValue(parameterSamples, "b",       sampleIndex);
                double Nmax  = getValue(parameterSamples, "N_max",   sampleIndex);
                double NA    = getValue(parameterSamples, "NA",      sampleIndex);
                double t50   = getValue(parameterSamples, "t50",     sampleIndex);
                double f0    = getValue(parameterSamples, "f0",      sampleIndex);
                double x     = getValue(parameterSamples, "x",       sampleIndex);
                double tau   = getValue(parameterSamples, "tau",     sampleIndex);
                double I_na  = getValue(parameterSamples, "I_na",    sampleIndex);

                PopulationFunction selectedModel;

                // *** The only changes are here: the mapping i=0..3 => 4 different models. ***
                if (I == 0) {
                    // i=0 => ConstantGrowth
                    // e.g. new ConstantGrowth( N0_con )
                    selectedModel = new ConstantPopulation(N0Con);

                } else if (I == 1) {
                    // i=1 => ExponentialGrowth
                    // e.g. new ExponentialGrowth(b, N0=?, NA=?, int I_na=?)
                    selectedModel = new ExponentialPopulation(
                            b,
                            Nmax,        // or whichever param for "N0"
                            NA,
                            (int) I_na
                    );

                } else if (I == 2) {
                    // i=2 => LogisticGrowth
                    // e.g. new LogisticGrowth(t50, b, NCarryingCapacity, NA, int I_na)
                    selectedModel = new LogisticPopulation(
                            t50,
                            b,
                            Nmax,
                            NA,
                            (int) I_na
                    );

                } else if (I == 3) {
                    // i=3 => GompertzGrowth_f0
                    // e.g. new GompertzGrowth_f0(N0, f0, b, NA, int I_na)
                    // (If you want to use Nmax as "N0", do so, or if your log has separate "N0" col, use that.)
                    selectedModel = new GompertzPopulation_f0(
                            Nmax,   // let's say we treat "N_max" as present-day population
                            f0,
                            b,
                            NA,
                            (int) I_na
                    );

                } else {
                    // In case the log has i>3, throw error
                    throw new IllegalArgumentException("Invalid model indicator I: " + I);
                }

                // Evaluate population sizes for each time bin
                for (int iT = 0; iT < binCount; iT++) {
                    double time = timePoints.get(iT);
                    double popSize = selectedModel.getTheta(time);
                    populationSizesAtTimePoints.get(iT).add(popSize);
                }
            }

            // 6. Compute stats (mean, median, 2.5%, 97.5%)
            List<Double> means   = new ArrayList<>();
            List<Double> medians = new ArrayList<>();
            List<Double> lower95 = new ArrayList<>();
            List<Double> upper95 = new ArrayList<>();

            for (int iT = 0; iT < binCount; iT++) {
                List<Double> sizes = populationSizesAtTimePoints.get(iT);

                double meanVal   = calculateMean(sizes);
                double medianVal = calculateMedian(sizes);
                double lowerVal  = calculatePercentile(sizes, 2.5);
                double upperVal  = calculatePercentile(sizes, 97.5);

                means.add(meanVal);
                medians.add(medianVal);
                lower95.add(lowerVal);
                upper95.add(upperVal);
            }

            // 7. Write CSV
            System.out.println("Writing results to CSV: " + outputCsvPath);
            writeResultsToCSV(outputCsvPath, timePoints, lower95, upper95, means, medians);

            System.out.println("Analysis complete. Results saved to " + outputCsvPath);

        } catch (Exception e) {
            System.err.println("An error occurred during the analysis.");
            e.printStackTrace();
        }
    }

    // ------------------------------------------------------------------
    // The rest of methods remain the same as your original code
    // ------------------------------------------------------------------

    private static double getValue(Map<String, List<Double>> paramSamples, String key, int index) {
        List<Double> list = paramSamples.get(key);
        if (list == null || index >= list.size()) {
            return Double.NaN;
        }
        return list.get(index);
    }

    public static double determineMaxTime(String method, List<Double> treeHeightSamples) {
        switch (method) {
            case "mean":
                return calculateMean(treeHeightSamples);
            case "median":
                return calculateMedian(treeHeightSamples);
            case "lowerHPD":
                return calculateHPD(treeHeightSamples, 0.95)[0];
            case "upperHPD":
                return calculateHPD(treeHeightSamples, 0.95)[1];
            case "fixed":
                return 0.11;
            default:
                throw new IllegalArgumentException("Unknown treeHeightMethod: " + method);
        }
    }

    public static List<Double> generateTimePoints(int numPoints, double minTime, double maxTime) {
        List<Double> timePoints = new ArrayList<>();
        double deltaTime = (maxTime - minTime) / (numPoints - 1);
        for (int i = 0; i < numPoints; i++) {
            timePoints.add(minTime + i * deltaTime);
        }
        return timePoints;
    }

    public static Map<String, List<Double>> readParameterSamples(String filePath, String[] parameterNames) {
        Map<String, List<Double>> samples = new HashMap<>();
        for (String param : parameterNames) {
            samples.put(param, new ArrayList<>());
        }
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            String[] headers = null;

            String delimiter = "\\s+";

            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                if (headers == null) {
                    headers = line.split(delimiter);
                    continue;
                }
                String[] tokens = line.split(delimiter);
                if (tokens.length == headers.length) {
                    for (String param : parameterNames) {
                        int idx = Arrays.asList(headers).indexOf(param);
                        if (idx >= 0) {
                            String valueStr = tokens[idx];
                            if (valueStr != null && !valueStr.isEmpty()) {
                                samples.get(param).add(Double.parseDouble(valueStr));
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + filePath);
            e.printStackTrace();
        }
        return samples;
    }

    public static double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    public static double calculateMedian(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n == 0) {
            return Double.NaN;
        }
        if (n % 2 == 0) {
            return (sorted.get(n/2 - 1) + sorted.get(n/2)) / 2.0;
        } else {
            return sorted.get(n/2);
        }
    }

    public static double calculatePercentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return Double.NaN;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size()-1)));
    }

    public static double[] calculateHPD(List<Double> values, double credMass) {
        if (values.isEmpty()) {
            return new double[]{Double.NaN, Double.NaN};
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        int intervalIdxInc = (int) Math.floor(credMass * n);
        double minWidth = Double.MAX_VALUE;
        int hpdStartIdx = 0;

        for (int i = 0; i <= (n - intervalIdxInc); i++) {
            double width = sorted.get(i + intervalIdxInc - 1) - sorted.get(i);
            if (width < minWidth) {
                minWidth = width;
                hpdStartIdx = i;
            }
        }
        return new double[]{
                sorted.get(hpdStartIdx),
                sorted.get(hpdStartIdx + intervalIdxInc - 1)
        };
    }

    public static void writeResultsToCSV(String filePath,
                                         List<Double> timePoints,
                                         List<Double> lower95,
                                         List<Double> upper95,
                                         List<Double> means,
                                         List<Double> medians) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Time,Lower95,Upper95,Mean,Median");
            for (int i = 0; i < timePoints.size(); i++) {
                pw.printf("%f,%f,%f,%f,%f%n",
                        timePoints.get(i),
                        lower95.get(i),
                        upper95.get(i),
                        means.get(i),
                        medians.get(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
