package edu.rit.croatia.swen383.g4.dmmodel;

import edu.rit.croatia.swen383.g4.food.BasicFood;
import edu.rit.croatia.swen383.g4.food.Food;
import edu.rit.croatia.swen383.g4.food.FoodCollection;
import edu.rit.croatia.swen383.g4.food.Recipe;
import edu.rit.croatia.swen383.g4.logs.DailyLog;
import edu.rit.croatia.swen383.g4.logs.LogCollection;
import edu.rit.croatia.swen383.g4.user.User;
import edu.rit.croatia.swen383.g4.util.CsvHandler;

import java.time.LocalDate;
import java.util.List;

/**
 * The type Diet manager model.
 */
public class DietManagerModel {

  private final CsvHandler csvHandler;
  private final FoodCollection foodCollection;
  private final LogCollection logCollection;

  //private final ExerciseCollection exerciseCollection;

  private final User user;

  /**
   * Instantiates a new Diet manager model.
   *
   * @param foodCsvFile the food csv file
   * @param logCsvFile  the log csv file
   * @param exerciseCsvFile  the log csv file
   */
  public DietManagerModel(String foodCsvFile, String logCsvFile,String exerciseCsvFile) {
    foodCollection = new FoodCollection();
    logCollection = new LogCollection();
    csvHandler = new CsvHandler(foodCsvFile, logCsvFile,exerciseCsvFile);
    user = new User();
    loadCsvData();
  }

  /**
   * Add basic food.
   *
   * @param food the food
   */
  public void addBasicFood(BasicFood food) {
    foodCollection.addFood(food);
  }

  /**
   * Add recipe.
   *
   * @param recipe the recipe
   */
  public void addRecipe(Recipe recipe) {
    foodCollection.addFood(recipe);
  }

  /**
   * Gets food.
   *
   * @return the food
   */
  public List<Food> getFoods() {
    return foodCollection.getFoods();

  }

  /**
   * Gets food by name.
   *
   * @param foodName the food name
   * @return the food by name
   */
  public Food getFoodByName(String foodName) {
    return foodCollection.getFoodByName(foodName);
  }

  /**
   * Gets daily log for today.
   *
   * @return the daily log for today
   */
  public DailyLog getDailyLogForToday() {
    LocalDate today = LocalDate.now();
    DailyLog log = logCollection.getDailyLogByDate(today);
    if (log == null) {
      log = new DailyLog(today);
      logCollection.addDailyLog(log);
    }
    return log;
  }

  /**
   * Gets daily log by date.
   *
   * @param date the date
   * @return the daily log by date
   */
  public DailyLog getDailyLogByDate(LocalDate date) {
    if ((logCollection.getDailyLogByDate(date)) == null) {
      DailyLog log = new DailyLog(date);
      logCollection.addDailyLog(log);
    }
    return logCollection.getDailyLogByDate(date);
  }

  /**
   * Gets log collection.
   *
   * @return the log collection
   */
  public LogCollection getLogCollection() {
    return logCollection;

  }

  /**
   * Get daily calorie intake.
   */
  public double getDailyCalorieIntake() {
    return user.getDailyCalorieIntake();
  }

  public void addDailyCalorieIntake(double calorieIntake) {
    user.updateDailyCalorieIntake(calorieIntake);
  }

  private void loadCsvData() {
    csvHandler.loadFoodData(foodCollection);
    csvHandler.loadLogData(logCollection, foodCollection);
  }

  /**
   * Save csv data.
   */
  public void saveCsvData() {
    csvHandler.saveFoodData(foodCollection);
    csvHandler.saveLogData(logCollection);
  }

}