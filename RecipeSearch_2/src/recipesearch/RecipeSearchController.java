
package recipesearch;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Text;
import javafx.util.Callback;
import se.chalmers.ait.dat215.lab2.Recipe;
import se.chalmers.ait.dat215.lab2.RecipeDatabase;


public class RecipeSearchController implements Initializable {
    ToggleGroup difficultyToggleGroup;
    RecipeDatabase db = RecipeDatabase.getSharedInstance();
    @FXML
    private FlowPane recipeList;
    @FXML
    private ComboBox<String> ingredient;
    @FXML
    private ComboBox<String> cuisine;
    @FXML
    private RadioButton allDifficulty;
    @FXML
    private RadioButton easyDifficulty;
    @FXML
    private RadioButton betweenDifficulty;
    @FXML
    private RadioButton hardDifficulty;
    @FXML
    private Spinner<Integer> maxPrice;
    @FXML
    private Slider maxTime;
    @FXML
    private Label labelTime;
    @FXML
    private Label detailedViewLabel;
    @FXML
    private ImageView detailedViewImage;
    @FXML
    private ImageView detailedMainIngredient;
    @FXML
    private ImageView detailedDifficulty;
    @FXML
    private ImageView detailedTimeImage;
    @FXML
    private Text detailedTime;
    @FXML
    private Text detailedCost;
    @FXML
    private Label detailedIngredients;
    @FXML
    private Label detailedCooking;
    @FXML
    private Text detailedAboutArea;
    @FXML
    private Text detailedCookingArea;
    @FXML
    private Text detailedIngredientsArea;
    @FXML
    private Text detailedPortions;
    @FXML
    private SplitPane searchPane;
    @FXML
    private AnchorPane recipeDetailPane;
    @FXML
    private Button closeBtn;
    @FXML
    private ImageView detailedCuisineImage;
    @FXML
    private ImageView closeImageView;

    private Map<String, RecipeListItem> recipeListItemMap = new HashMap<String, RecipeListItem>();


    SpinnerValueFactory<Integer> valueFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 150, 0, 1);

    private RecipeBackendController backendControll = new RecipeBackendController();
    private List<Recipe> listRecipe;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        ingredient.getItems().addAll("Visa alla", "Kött", "Fisk", "Kyckling", "Vegetarisk");
        ingredient.getSelectionModel().select("Visa alla");
        ingredient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                backendControll.setMainIngredient(newValue);
                updateRecipeList();
            }
        });

        cuisine.getItems().addAll("Visa alla", "Sverige", "Grekland", "Indien", "Asien", "Afrika", "Frankrike");
        cuisine.getSelectionModel().select("Visa alla");
        cuisine.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                backendControll.setCuisine(newValue);
                updateRecipeList();
            }
        });

        difficultyToggleGroup = new ToggleGroup();

        allDifficulty.setSelected(true);
        allDifficulty.setToggleGroup(difficultyToggleGroup);
        easyDifficulty.setToggleGroup(difficultyToggleGroup);
        betweenDifficulty.setToggleGroup(difficultyToggleGroup);
        hardDifficulty.setToggleGroup(difficultyToggleGroup);

        difficultyToggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {

                if (difficultyToggleGroup.getSelectedToggle() != null) {
                    RadioButton selected = (RadioButton) difficultyToggleGroup.getSelectedToggle();
                    backendControll.setDifficulty(selected.getText());
                    updateRecipeList();
                }
            }
        });

        maxPrice.setValueFactory(valueFactory);
        maxPrice.valueProperty().addListener(new ChangeListener<Integer>() {
            @Override
            public void changed(ObservableValue<? extends Integer> observableValue, Integer integer, Integer t1) {
                if (maxPrice.getValue() != null) {
                    Integer selected = maxPrice.getValue();
                    backendControll.setMaxPrice(selected);
                    updateRecipeList();
                }
            }
        });

        maxPrice.focusedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if (newValue) {
                    //focusgained - do nothing
                } else {
                    Integer value = Integer.valueOf(maxPrice.getEditor().getText());
                    backendControll.setMaxPrice(value);
                    updateRecipeList();
                }

            }
        });

        maxTime.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                if (newValue != null && !newValue.equals(oldValue)) {
                    backendControll.setMaxTime((int) Math.rint(newValue.intValue() / 10) * 10);
                    updateRecipeList();
                }

                labelTime.setText(String.valueOf((int) Math.rint(newValue.intValue() / 10) * 10) + " minuter");
            }
        });

        for (Recipe recipe : backendControll.getRecipes()) {
            RecipeListItem recipeListItem = new RecipeListItem(recipe, this);
            recipeListItemMap.put(recipe.getName(), recipeListItem);
        }

        Platform.runLater(()->ingredient.requestFocus());
        populateMainIngredientComboBox();
        populateCuisinesComboBox();

        updateRecipeList();
    }

    private void updateRecipeList() {
        recipeList.getChildren().clear();
        listRecipe = backendControll.getRecipes();

        for (Recipe recipe : listRecipe) {
            RecipeListItem listItem = recipeListItemMap.get(recipe.getName());
            recipeList.getChildren().add(listItem);
        }
    }

    private void populateRecipeDetailView(Recipe recipe) {
        String result = recipe.getIngredients().stream()
                .map(n -> String.valueOf(n))
                .collect(Collectors.joining("\n", "", ""));

        detailedViewLabel.setText(recipe.getName());
        detailedViewImage.setImage(recipe.getFXImage());
        detailedMainIngredient.setImage(getMainIngredientImage(recipe.getMainIngredient()));
        detailedDifficulty.setImage(getDifficultyImage(recipe.getDifficulty()));
        detailedTime.setText(String.valueOf(recipe.getTime()) + " minuter");
        detailedCost.setText(String.valueOf(recipe.getPrice()) + " kr");
        detailedAboutArea.setText(recipe.getDescription());
        detailedCookingArea.setText(recipe.getInstruction());
        detailedPortions.setText(String.valueOf(recipe.getServings()) + " portioner");
        detailedIngredientsArea.setText(result);
        detailedCuisineImage.setImage(getCuisineImage(recipe.getCuisine()));

    }

    @FXML
    public void closeRecipeView() {
        searchPane.toFront();
    }

    public void openRecipeView(Recipe recipe) {
        populateRecipeDetailView(recipe);
        recipeDetailPane.toFront();
    }

    @FXML
    public void closeButtonMouseEntered(){
        closeImageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close_hover.png")));
    }

    @FXML
    public void closeButtonMousePressed(){
        closeImageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close_pressed.png")));
    }

    @FXML
    public void closeButtonMouseExited(){
        closeImageView.setImage(new Image(getClass().getClassLoader().getResourceAsStream(
                "RecipeSearch/resources/icon_close.png")));
    }

    @FXML
    public void mouseTrap(Event event){
        event.consume();
    }

    private void populateMainIngredientComboBox() {
        Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> p) {

                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(item);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            Image icon = null;
                            String iconPath;
                            try {
                                switch (item) {

                                    case "Kött":
                                        iconPath = "RecipeSearch/resources/icon_main_meat.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Fisk":
                                        iconPath = "RecipeSearch/resources/icon_main_fish.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Kyckling":
                                        iconPath = "RecipeSearch/resources/icon_main_chicken.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Vegetarisk":
                                        iconPath = "RecipeSearch/resources/icon_main_veg.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                }
                            } catch (NullPointerException ex) {
                                //This should never happen in this lab but could load a default image in case of a NullPointer
                            }
                            ImageView iconImageView = new ImageView(icon);
                            iconImageView.setFitHeight(12);
                            iconImageView.setPreserveRatio(true);
                            setGraphic(iconImageView);
                        }
                    }
                };
            }
        };
        ingredient.setButtonCell(cellFactory.call(null));
        ingredient.setCellFactory(cellFactory);
    }

    private void populateCuisinesComboBox() {
        Callback<ListView<String>, ListCell<String>> cellFactory = new Callback<ListView<String>, ListCell<String>>() {

            @Override
            public ListCell<String> call(ListView<String> p) {

                return new ListCell<String>() {

                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);

                        setText(item);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            Image icon = null;
                            String iconPath;
                            try {
                                switch (item) {

                                    case "Sverige":
                                        iconPath = "recipesearch/resources/icon_flag_sweden.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Grekland":
                                        iconPath = "recipesearch/resources/icon_flag_greece.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Indien":
                                        iconPath = "recipesearch/resources/icon_flag_india.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Asien":
                                        iconPath = "recipesearch/resources/icon_flag_asia.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Afrika":
                                        iconPath = "recipesearch/resources/icon_flag_africa.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                    case "Frankrike":
                                        iconPath = "recipesearch/resources/icon_flag_france.png";
                                        icon = new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
                                        break;
                                }
                            } catch (NullPointerException ex) {
                                //This should never happen in this lab but could load a default image in case of a NullPointer
                            }
                            ImageView iconImageView = new ImageView(icon);
                            iconImageView.setFitHeight(12);
                            iconImageView.setPreserveRatio(true);
                            setGraphic(iconImageView);
                        }
                    }
                };
            }
        };
        cuisine.setButtonCell(cellFactory.call(null));
        cuisine.setCellFactory(cellFactory);
    }

    public Image getCuisineImage(String cuisine) {
        String iconPath;
        switch (cuisine) {
            case "Sverige":
                iconPath = "RecipeSearch/resources/icon_flag_sweden.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Grekland":
                iconPath = "recipesearch/resources/icon_flag_greece.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Indien":
                iconPath = "recipesearch/resources/icon_flag_india.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Asien":
                iconPath = "recipesearch/resources/icon_flag_asia.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Afrika":
                iconPath = "recipesearch/resources/icon_flag_africa.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Frankrike":
                iconPath = "recipesearch/resources/icon_flag_france.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
        }
        return null;
    }

    public Image getMainIngredientImage(String ingredient) {
        String iconPath;
        switch (ingredient) {
            case "Kött":
                iconPath = "RecipeSearch/resources/icon_main_meat.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Fisk":
                iconPath = "RecipeSearch/resources/icon_main_fish.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Kyckling":
                iconPath = "RecipeSearch/resources/icon_main_chicken.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Vegetarisk":
                iconPath = "RecipeSearch/resources/icon_main_veg.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));
        }
        return null;
    }

    public Image getDifficultyImage(String difficulty) {
        String iconPath;
        switch (difficulty) {
            case "Lätt":
                iconPath = "recipesearch/resources/icon_difficulty_easy.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Svår":
                iconPath = "recipesearch/resources/icon_difficulty_hard.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            case "Mellan":
                iconPath = "recipesearch/resources/icon_difficulty_medium.png";
                return new Image(getClass().getClassLoader().getResourceAsStream(iconPath));

            }
        return null;
    }
}