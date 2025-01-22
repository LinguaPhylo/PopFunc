

import lphy.base.evolution.coalescent.PopulationFunction;
import lphy.base.evolution.coalescent.populationmodel.*;

import java.io.*;
import java.util.*;

public class HCVdemo {

    public static void main(String[] args) {

        String modelType = "SVS";

        // Input/Output paths
        String logFilePath = "../hcv.log";
        String outputCsvPath = "../output_results.csv";

        System.out.println("Starting SVSAnalysis...");
        System.out.println("Reading file: " + logFilePath);

        // Parameters to be read from the log file
        String[] parameterNames = {
                "I",
                "b",
                "N_max",
                "NA",
                "t50",
                "f0",
                "x",
                "tau",
                "Tree.height",
                "I_na"
        };

        // Method for determining max time
        String treeHeightMethod = "fixed";

        // Number of time bins
        int binCount = 100;

        try {
            // 1. Read parameter samples from the log file
            Map<String, List<Double>> parameterSamples = readParameterSamples(logFilePath, parameterNames);
            System.out.println("Parameters loaded successfully.");

            // 2. Check Tree.height
            if (parameterSamples.get("Tree.height") == null || parameterSamples.get("Tree.height").isEmpty()) {
                throw new IllegalStateException("Error: Tree.height parameter is missing or empty in the log file.");
            }

            // 3. Determine max time
            List<Double> treeHeightSamples = parameterSamples.get("Tree.height");
            double maxTime = determineMaxTime(treeHeightMethod, treeHeightSamples);
            System.out.println("Max Time determined: " + maxTime);

            // 4. Generate time points
            List<Double> timePoints = generateTimePoints(binCount, 0.0, maxTime);

            // Prepare list for population size samples at each time bin
            List<List<Double>> populationSizesAtTimePoints = new ArrayList<>();
            for (int i = 0; i < binCount; i++) {
                populationSizesAtTimePoints.add(new ArrayList<>());
            }

            // 5. Loop over samples: choose the model and evaluate population sizes
            int sampleCount = parameterSamples.get("I").size();
            for (int sampleIndex = 0; sampleIndex < sampleCount; sampleIndex++) {
                double I     = parameterSamples.get("I").get(sampleIndex);
                double b     = getValue(parameterSamples, "b",        sampleIndex);
                double Nmax  = getValue(parameterSamples, "N_max",    sampleIndex);
                double NA    = getValue(parameterSamples, "NA",       sampleIndex);
                double t50   = getValue(parameterSamples, "t50",      sampleIndex);
                double f0    = getValue(parameterSamples, "f0",       sampleIndex);
                double x     = getValue(parameterSamples, "x",        sampleIndex);
                double tau   = getValue(parameterSamples, "tau",      sampleIndex);
                double I_na  = getValue(parameterSamples, "I_na",     sampleIndex);

                PopulationFunction selectedModel;
                if (I == 0) {
                    // Exponential
                    selectedModel = new ExponentialPopulation(
                            /* GrowthRate = */ b,
                            /* N0         = */ Nmax,
                            /* NA         = */ NA,
                            /* I_na       = */ (int) I_na
                    );
                } else if (I == 1) {
                    // Logistic
                    selectedModel = new LogisticPopulation(
                            /* t50               = */ t50,
                            /* nCarryingCapacity = */ Nmax,
                            /* b                 = */ b,
                            /* NA                = */ NA,
                            /* I_na             = */ (int) I_na
                    );
                } else if (I == 2) {
                    // Gompertz: decide f0 vs. t50
                    boolean hasF0 = parameterSamples.containsKey("f0")
                            && sampleIndex < parameterSamples.get("f0").size()
                            && !Double.isNaN(f0);

                    if (hasF0) {
                        selectedModel = new GompertzPopulation_f0(
                                /* N0   = */ Nmax,
                                /* f0   = */ f0,
                                /* b    = */ b,
                                /* NA   = */ NA,
                                /* I_na = */ (int) I_na
                        );
                    } else {
                        selectedModel = new GompertzPopulation_t50(
                                /* t50       = */ t50,
                                /* b         = */ b,
                                /* NInfinity = */ Nmax,
                                /* NA        = */ NA,
                                /* I_na      = */ (int) I_na
                        );
                    }
                } else if (I == 3) {
                    // Expansion
                    selectedModel = new ExpansionPopulation(
                            /* NA    = */ Nmax,
                            /* x     = */ NA,
                            /* r     = */ b,
                            /* NC    = */ x,
                            /* I_na  = */ (int) I_na
                    );
                } else if (I == 4) {
                    // Cons_Exp_Cons
                    selectedModel = new Cons_Exp_ConsPopulation(
                            /* tau = */ tau,
                            /* NC  = */ Nmax,
                            /* r   = */ b,
                            /* x   = */ x
                    );
                } else {
                    throw new IllegalArgumentException("Invalid model indicator I: " + I);
                }

                // Wrap in SVSPopulation or other aggregator
                SVSPopulation svsPop = new SVSPopulation(selectedModel);

                // Evaluate population size at each time bin
                for (int iT = 0; iT < binCount; iT++) {
                    double time = timePoints.get(iT);
                    double popSize = svsPop.getTheta(time);
                    populationSizesAtTimePoints.get(iT).add(popSize);
                }
            }

            // 6. Compute statistics (mean, median, 2.5th percentile, 97.5th percentile)
            List<Double> means = new ArrayList<>();
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

            // 7. Write results to CSV
            System.out.println("Writing results to CSV: " + outputCsvPath);
            writeResultsToCSV(outputCsvPath, timePoints, lower95, upper95, means, medians);

            System.out.println("Analysis complete. Results saved to " + outputCsvPath);

        } catch (Exception e) {
            System.err.println("An error occurred during the analysis.");
            e.printStackTrace();
        }
    }

    // Helper method to get a parameter value safely
    private static double getValue(Map<String, List<Double>> paramSamples, String key, int index) {
        List<Double> list = paramSamples.get(key);
        if (list == null || index >= list.size()) {
            return Double.NaN;
        }
        return list.get(index);
    }

    // Decide how to determine max time from Tree.height
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
                return 150.0;
            default:
                throw new IllegalArgumentException("Unknown treeHeightMethod: " + method);
        }
    }

    // Generate a list of time points
    public static List<Double> generateTimePoints(int numPoints, double minTime, double maxTime) {
        List<Double> timePoints = new ArrayList<>();
        double deltaTime = (maxTime - minTime) / (numPoints - 1);
        for (int i = 0; i < numPoints; i++) {
            timePoints.add(minTime + i * deltaTime);
        }
        return timePoints;
    }

    // Read parameter samples from file
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
            System.err.println("Error reading the file: " + filePath);
            e.printStackTrace();
        }
        return samples;
    }

    // Calculate mean
    public static double calculateMean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    // Calculate median
    public static double calculateMedian(List<Double> values) {
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int n = sorted.size();
        if (n == 0) {
            return Double.NaN;
        }
        if (n % 2 == 0) {
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            return sorted.get(n / 2);
        }
    }

    // Calculate a given percentile (e.g., 2.5 or 97.5)
    public static double calculatePercentile(List<Double> values, double percentile) {
        if (values.isEmpty()) {
            return Double.NaN;
        }
        List<Double> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    // Calculate HPD interval
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

    // Write results to CSV
    public static void writeResultsToCSV(String filePath,
                                         List<Double> timePoints,
                                         List<Double> lower95,
                                         List<Double> upper95,
                                         List<Double> means,
                                         List<Double> medians) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Time,Lower95,Upper95,Mean,Median");
            for (int i = 0; i < timePoints.size(); i++) {
                pw.printf(
                        "%f,%f,%f,%f,%f%n",
                        timePoints.get(i),
                        lower95.get(i),
                        upper95.get(i),
                        means.get(i),
                        medians.get(i)
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
