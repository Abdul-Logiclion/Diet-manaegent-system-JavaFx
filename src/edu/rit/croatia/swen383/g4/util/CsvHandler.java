package edu.rit.croatia.swen383.g4.util;

import edu.rit.croatia.swen383.g4.food.*;
import edu.rit.croatia.swen383.g4.logger.Logger;
import edu.rit.croatia.swen383.g4.logs.DailyLog;
import edu.rit.croatia.swen383.g4.logs.LogCollection;
import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.*;
import  edu.rit.croatia.swen383.g4.dmview.DietManagerView;


/**
 * The type Csv handler.
 */
public class CsvHandler {

  public  static List<String> exercises=new ArrayList<String>();

  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(
    "yyyy-M-d"
  );
  private static final Logger LOGGER = new Logger();
  private final String foodFileName;
  private final String logFileName;
  private final String exerciseFileName;
  private Stream<String> lines = null;
  private PrintWriter writer = null;

  /**
   * Instantiates a new Csv handler.
   *
   * @param foodFileName the food file name
   * @param logFileName  the log file name
   */
  public CsvHandler(String foodFileName, String logFileName,String exerciseFileName) {
    this.foodFileName = foodFileName;
    this.logFileName = logFileName;
    this.exerciseFileName=exerciseFileName;

      }

  /**
   * Load food data.
   *
   * @param foodCollection the food collection
   */
  public void loadFoodData(FoodCollection foodCollection) {
    try {
      lines = Files.lines(Paths.get(foodFileName));

      lines
        .map(line -> line.split(","))
        .filter(row -> row.length > 0 && !row[0].isBlank())
        .forEach(row -> {
          FoodIdentifier identifier = FoodIdentifier.valueOf(
            String.valueOf(Character.toUpperCase(row[0].charAt(0)))
          );

          if (identifier == FoodIdentifier.B) {
            String foodName = row[1];
            double calories = Double.parseDouble(row[2]);
            double fat = Double.parseDouble(row[3]);
            double carbohydrates = Double.parseDouble(row[4]);
            double protein = Double.parseDouble(row[5]);

            BasicFood food = new BasicFood(
              foodName,
              calories,
              fat,
              carbohydrates,
              protein
            );
            foodCollection.addFood(food);
          } else if (identifier == FoodIdentifier.R) {
            String recipeName = row[1];
            Map<Food, Double> foodsMap = new LinkedHashMap<>();
            List<Food> foods = new ArrayList<>();
            for (int i = 2; i < row.length; i += 2) {
              String foodName = row[i];
              double quantity = Double.parseDouble(row[i + 1]);
              Food food = foodCollection
                .getFoods()
                .stream()
                .filter(f -> f.getName().equals(foodName))
                .findFirst()
                .orElse(null);

              if (food != null) {
                foodsMap.put(food, quantity);
                foods.add(food);
              }
            }
            Recipe recipe = new Recipe(recipeName, foodsMap, foods);
            foodCollection.addFood(recipe);
          }
        });
    } catch (IOException e) {
      LOGGER.log("Error reading food CSV file: " + foodFileName);
      e.printStackTrace();
    } finally {
      if (lines != null) lines.close();
    }
  }

  /**
   * Load log data.
   *
   * @param logCollection  the log collection
   * @param foodCollection the food collection
   */
  public void loadLogData(
          LogCollection logCollection,
          FoodCollection foodCollection
  ) {
    // Add user integration for weight, where I check for 'w' can also add an enum Identifier

    try {
      lines = Files.lines(Paths.get(logFileName));

      lines
              .map(line -> line.split(","))
              .forEach(row -> {
                   String dateString = String.format("%s-%s-%s", row[0], row[1], row[2]);
                LocalDate date = LocalDate.parse(dateString, DATE_FORMAT);


                if ("f".equals(row[3])) {
                  String foodName = row[4];
                  double count = Double.parseDouble(row[5]);
                  Food food = foodCollection
                          .getFoods()
                          .stream()
                          .filter(f -> f.getName().equals(foodName))
                          .findFirst()
                          .orElse(null);
                  if (food != null) {
                    DailyLog log = logCollection.getDailyLogByDate(date);
                    if (log == null) {
                      log = new DailyLog(date);
                      logCollection.addDailyLog(log);
                    }
                    log.addFood(food, count);
                  }
                }
                else {
                  exercises.add(String.join(",", row));

                }


              });


    } catch (IOException e) {
      LOGGER.log("Error reading log CSV file: " + logFileName);
      e.printStackTrace();
    } finally {
      if (lines != null) lines.close();
    }
  }

  /**
   * Save food data.
   *
   * @param foodCollection the food collection
   */
  public void saveFoodData(FoodCollection foodCollection) {
    try {
      writer =
        new PrintWriter(Files.newBufferedWriter(Paths.get(foodFileName)));

      for (Food food : foodCollection.getFoods()) {
        if (food instanceof BasicFood basicFood) {
          String line = String.join(
            ",",
            Character.toString(FoodIdentifier.B.getChar()),
            basicFood.getName(),
            Double.toString(basicFood.getCalories()),
            Double.toString(basicFood.getFat()),
            Double.toString(basicFood.getCarbs()),
            Double.toString(basicFood.getProtein())
          );
          writer.println(line);
        } else if (food instanceof Recipe recipe) {
          List<String> basicFoodNamesAndQuantities = new ArrayList<>();
          for (Map.Entry<Food, Double> entry : recipe.getRecipe().entrySet()) {
            basicFoodNamesAndQuantities.add(entry.getKey().getName());
            basicFoodNamesAndQuantities.add(Double.toString(entry.getValue()));
          }
          String line = String.join(
            ",",
            Character.toString(FoodIdentifier.R.getChar()),
            recipe.getName(),
            String.join(",", basicFoodNamesAndQuantities)
          );
          writer.println(line);
        }
      }
    } catch (IOException e) {
      LOGGER.log("Error writing food CSV file: " + foodFileName);
      e.printStackTrace();
    } finally {
      if (writer != null) writer.close();
    }
  }

  /**
   * Save log data.
   *
   * @param logCollection the log collection
   */
  public void saveLogData(LogCollection logCollection) {
    try {

      writer = new PrintWriter(Files.newBufferedWriter(Paths.get(logFileName)));


      for (DailyLog log : logCollection.getDailyLogs()) {
        LocalDate date = log.getDate();
        for (Map.Entry<Food, Double> entry : log
          .getIntakeAndServing()
          .entrySet()) {
          Food food = entry.getKey();

          String line = String.join(
            ",",
            Integer.toString(date.getYear()),
            Integer.toString(date.getMonthValue()),
            Integer.toString(date.getDayOfMonth()),
            "f",
            food.getName(),
            Double.toString(entry.getValue())
          );
          writer.println(line);
        }
        for (String exercise : exercises) {
          String[] parts = exercise.split(",");

          // Check if the exercise entry has the correct number of parts
          if (parts.length >= 4) {
            String[] dateParts = parts[0].split("-");

            // Check if the date has the correct format
            if (dateParts.length == 3) {
              // Extract year, month, and day from the date
              String year = dateParts[0];
              String month = dateParts[1];
              String day = dateParts[2];
              // Extract exercise name and calories
              String exerciseName = parts[2];
              String calories = parts[3];

              // Construct the formatted line
              String formattedLine = year + "," + month + "," + day + ",e," + exerciseName + "," + calories;
              // Write the formatted line to the log file
              writer.println(formattedLine);

            }
          }
        }

//        exercises.forEach(line->
//                writer.println(line)
//        );


      }
    } catch (IOException e) {
      LOGGER.log("Error writing log CSV file: " + logFileName);
      e.printStackTrace();
    } finally {
      if (writer != null) writer.close();
    }
  }
}