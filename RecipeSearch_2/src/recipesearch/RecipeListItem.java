package recipesearch;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import se.chalmers.ait.dat215.lab2.Recipe;

import java.io.IOException;

public class RecipeListItem extends AnchorPane {
    private RecipeSearchController parentController;
    private Recipe recipe;
    @FXML private ImageView itemImage;
    @FXML private Label itemName;
    @FXML private ImageView itemMainIngredient;
    @FXML private ImageView itemDifficulty;
    @FXML private Label itemTime;
    @FXML private Label itemCost;
    @FXML private ImageView itemCuisine;
    @FXML private Text itemDescription;

    public RecipeListItem(Recipe recipe, RecipeSearchController recipeSearchController){
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("recipe_listitem.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        this.recipe = recipe;
        this.parentController = recipeSearchController;

        itemImage.setImage(this.getSquareImage(recipe.getFXImage()));
        itemName.setText(recipe.getName());
        itemCuisine.setImage(parentController.getCuisineImage(recipe.getCuisine()));
        itemMainIngredient.setImage(parentController.getMainIngredientImage(recipe.getMainIngredient()));
        itemDescription.setText(recipe.getDescription());
        itemCost.setText(String.valueOf(recipe.getPrice()) + " kr");
        itemTime.setText(String.valueOf(recipe.getTime()) + " minuter");
        itemDifficulty.setImage(parentController.getDifficultyImage(recipe.getDifficulty()));
    }

    @FXML
    protected void onClick(Event event){
        parentController.openRecipeView(recipe);
    }

    public Image getSquareImage(Image image){

        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        if(image.getWidth() > image.getHeight()){
            width = (int) image.getHeight();
            height = (int) image.getHeight();
            x = (int)(image.getWidth() - width)/2;
            y = 0;
        }

        else if(image.getHeight() > image.getWidth()){
            width = (int) image.getWidth();
            height = (int) image.getWidth();
            x = 0;
            y = (int) (image.getHeight() - height)/2;
        }

        else{
            //Width equals Height, return original image
            return image;
        }
        return new WritableImage(image.getPixelReader(), x, y, width, height);
    }
}
