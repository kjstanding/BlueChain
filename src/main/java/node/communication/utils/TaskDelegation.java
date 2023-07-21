package node.communication.utils;

import node.blockchain.ml_verification.ModelData;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TaskDelegation {
    public List<Integer> getIntervals(ModelData modelData, String lastBlockHash) {
        List<Integer> indexes = IntStream.rangeClosed(0, 19).boxed().collect(Collectors.toList());
        System.out.println("Original list: " + indexes);

        List<Integer> subsetIndexes = weightAnalysis(modelData.getSnapshotsFilePath());
        System.out.println("Pre-selected subset: " + subsetIndexes);

        double percentage = 0.5;

        List<Integer> resultArray = createSubset(indexes, subsetIndexes, percentage, lastBlockHash);

        System.out.println("Resulting array: " + resultArray);
        return resultArray;
    }

    public List<Integer> weightAnalysis(String snapshotsFilePath) {
        try {
            List<String> command = new ArrayList<>();
            command.add("/Users/kjstanding/Projects/MNIST_NeuralNetwork/venv/bin/python");
            command.add("weight_analysis.py");

            File file = new File(snapshotsFilePath);
            if (!file.exists()) throw new FileNotFoundException(snapshotsFilePath);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            Map<String, String> env = processBuilder.environment();
            // Maybe remove
            env.put("KMP_DUPLICATE_LIB_OK", "TRUE");
            Process process = processBuilder.start();

            OutputStreamWriter writer = new OutputStreamWriter(process.getOutputStream());
            writer.write(snapshotsFilePath + "\n");
            writer.flush();

            InputStream inputStream = process.getInputStream();
            InputStream errorStream = process.getErrorStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream));
            String line;
            StringBuilder outputBuilder = new StringBuilder();
            StringBuilder errorBuilder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                outputBuilder.append(line);
            }
            while ((line = errorReader.readLine()) != null) {
                errorBuilder.append(line);
            }

            int exitCode = process.waitFor();

            String outputString = outputBuilder.toString().trim();
            String errorString = errorBuilder.toString().trim();

            System.out.println("Output: " + outputString);
            System.out.println("Error: " + errorString);

            if (exitCode != 0) {
                throw new RuntimeException("Python script execution failed with exit code: " + exitCode);
            }

            List<Integer> outputArray = new ArrayList<>();
            if (!outputString.isEmpty()) {
                String[] numberStrings = outputString.replaceAll("\\[|\\]", "").split(",");
                for (String numberString : numberStrings) {
                    outputArray.add(Integer.parseInt(numberString.trim()));
                }
            }

            return outputArray;
        } catch (IOException | InterruptedException | RuntimeException e) {
            System.out.println("Error executing python script");
            e.printStackTrace();
            return Arrays.asList(4, 10, 13);
        }
    }

    public static double[] createWeights(List<Integer> indexes) {
        double mean = indexes.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double variance = indexes.stream().mapToDouble(i -> Math.pow(i - mean, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);

        double[] weights = indexes.stream().mapToDouble(i -> calculateNormalDensity(i, mean, stdDev)).toArray();

        double maxWeight = Arrays.stream(weights).max().orElse(0.0);
        weights = Arrays.stream(weights).map(w -> Math.abs(w - maxWeight)).toArray();

        double sumWeights = Arrays.stream(weights).sum();
        weights = Arrays.stream(weights).map(w -> w / sumWeights).toArray();

        return weights;
    }

    private static double calculateNormalDensity(int x, double mean, double stdDev) {
        double exponent = Math.exp(-(Math.pow(x - mean, 2) / (2 * Math.pow(stdDev, 2))));
        return (1 / (Math.sqrt(2 * Math.PI) * stdDev)) * exponent;
    }

    public static List<Integer> createSubset(List<Integer> indexes, List<Integer> subsetIndexes,
                                             double percentage, String lastBlockHash) {
        List<Integer> resultArray = new ArrayList<>(subsetIndexes);

        List<Integer> remainingIndexes = new ArrayList<>(indexes);
        remainingIndexes.removeAll(subsetIndexes);

        int totalSize = (int) (indexes.size() * percentage);
        int remainingSize = totalSize - resultArray.size();

        if (remainingSize > 0) {
            double[] weights = createWeights(remainingIndexes);

            Random rand = new Random(generateSeedFromText(lastBlockHash));
            Set<Integer> selectedIndexes = new HashSet<>();

            for (int i = 0; i < remainingSize; i++) {
                int randomIndex = getRandomIndex(weights, rand);

                while (selectedIndexes.contains(randomIndex)) {
                    randomIndex = getRandomIndex(weights, rand);
                }

                selectedIndexes.add(randomIndex);
                resultArray.add(remainingIndexes.get(randomIndex));
            }
        }

        return resultArray;
    }

    public static int getRandomIndex(double[] weights, Random rand) {
        double total = Arrays.stream(weights).sum();
        double value = rand.nextDouble() * total;

        for (int i = 0; i < weights.length; i++) {
            value -= weights[i];
            if (value <= 0) return i;
        }
        return weights.length - 1;
    }

    public static long generateSeedFromText(String seedText) {
        byte[] seedBytes = seedText.getBytes(StandardCharsets.UTF_8);
        long seed = 0;

        for (byte seedByte : seedBytes) {
            seed = (seed << 8) | (seedByte & 0xFF);
        }

        return seed;
    }
}
