package cz.nitramek.vsb.gui;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import cz.nitramek.vsb.Tuple;

public class FileData {
    public static final Pattern SINGLE_INPUT_PATTERN = Pattern.compile("\\s*\\[([0-9]+(o?)),([0-9]+)]\\s*");
    public static final Pattern INPUT_SEPARATOR_PATTERN = Pattern.compile(";");

    static List<Tuple<double[], double[]>> parseInputFile(File selectedFile) {
        try {
            return Files.lines(selectedFile.toPath())
                    .map(line -> {
                        String[] nodes = INPUT_SEPARATOR_PATTERN.split(line);
                        List<Tuple<String, Double>> inputs = new ArrayList<>();
                        List<Tuple<String, Double>> outputs = new ArrayList<>();
                        for (String node : nodes) {
                            Matcher matcher = SINGLE_INPUT_PATTERN.matcher(node);
                            boolean matches = matcher.find();
                            if (matches) {
                                throw new RuntimeException("Error in single node - " + node);
                            }
                            String id = matcher.group(1);
                            boolean output = !matcher.group(2).isEmpty();
                            double value = Double.parseDouble(matcher.group(3));
                            if (output) {
                                outputs.add(Tuple.make(id, value));
                            } else {
                                inputs.add(Tuple.make(id, value));
                            }
                        }
                        inputs.sort(Comparator.comparing(Tuple::getFirst));
                        outputs.sort(Comparator.comparing(Tuple::getFirst));
                        double[] inputsArray = inputs.stream().map(Tuple::getSecond).mapToDouble(Double::doubleValue)
                                .toArray();
                        double[] ouputsArray = outputs.stream().map(Tuple::getSecond).mapToDouble(Double::doubleValue)
                                .toArray();
                        return Tuple.make(inputsArray, ouputsArray);
                    }).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
