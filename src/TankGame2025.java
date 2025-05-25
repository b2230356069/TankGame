import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class TankGame2025 extends Application {

    public void start(Stage firstStage) {

        Pane main = new Pane();
        main.setStyle("-fx-background-color: black;");

        int sceneWidth = 1000;
        int sceneHeight = 1000;
        Map map = new Map(main, sceneWidth, sceneHeight);

        ImageView tank = new ImageView();
        tank.setImage(new Image(getClass().getResource("/yellowTank1.png").toExternalForm()));

        tank.setX(250);
        tank.setY(250);

        tank.setFitHeight(25);
        tank.setFitWidth(25);

        main.getChildren().add(tank);

        Scene scene = new Scene(main, 500, 500);

        Animation animation = new Animation(tank);
        Move.moveTank(scene, tank, animation, map.getWalls());
        firstStage.setScene(scene);
        firstStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
