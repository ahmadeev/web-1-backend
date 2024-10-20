package org.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Validator {
    public static ArrayList<Double> validX = new ArrayList<>(Arrays.asList(-2.0, -1.5, -1.0, -0.5, 0.0, 0.5, 1.0, 1.5, 2.0));
    public static ArrayList<Double> validR = new ArrayList<>(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0));

    public static double lowValidY = -3;
    public static double highValidY = 3;

    public static boolean isInputValid(HashMap<String, Double> parsedStringByKeyValue) {
        double x = parsedStringByKeyValue.get("xType");
        double y = parsedStringByKeyValue.get("yType");
        double R = parsedStringByKeyValue.get("RType");

        return validX.contains(x) && validR.contains(R) && y >= lowValidY && y <= highValidY;
    }
}
