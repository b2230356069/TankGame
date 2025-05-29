import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;

public class TankGame2025 extends Application {

    private static int score = 0;
    private static Text scoreText;
    private static ArrayList<Enemy> enemies = new ArrayList<>();

    public void start(Stage firstStage) {

        Pane main = new Pane();
        Pane ui = new Pane();

        StackPane all = new StackPane();
        all.getChildren().addAll(main, ui);

        int sceneWidth = 1000;
        int sceneHeight = 1000;
        Map map = new Map(main, sceneWidth, sceneHeight);
        map.createExtraWalls(main);

        scoreText = new Text(20, 30, "Score: 0");
        scoreText.setFont(Font.font("Arial", 20));
        scoreText.setFill(Color.WHITE);
        ui.getChildren().add(scoreText);

        ImageView tank = new ImageView();
        tank.setImage(new Image(getClass().getResource("/yellowTank1.png").toExternalForm()));

        tank.setX(250);
        tank.setY(250);

        tank.setFitHeight(25);
        tank.setFitWidth(25);

        main.getChildren().add(tank);

        Scene scene = new Scene(all, 1000, 750);
        ArrayList<ImageView> bullets = new ArrayList<>();
        ArrayList<ImageView> walls = new ArrayList<>();
        Animation animation = new Animation(tank);

        scene.setFill(Color.BLACK);

        Moving.moveTank(scene, tank, animation, map.getWalls(), main, bullets);

        enemies.add(new Enemy(main, 100, 700, map.getWalls()));
        enemies.add(new Enemy(main, 700, 100, map.getWalls()));
        enemies.add(new Enemy(main, 700, 700, map.getWalls()));
        enemies.add(new Enemy(main, 400, 400, map.getWalls()));
        enemies.add(new Enemy(main, 400, 600, map.getWalls()));
        enemies.add(new Enemy(main, 600, 400, map.getWalls()));
        enemies.add(new Enemy(main, 600, 600, map.getWalls()));

        firstStage.setScene(scene);
        firstStage.show();
    }

    public static ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public static void updateCamera(Pane main, ImageView tank, Scene scene) {
        double newX = scene.getWidth() / 2 - (tank.getX() + tank.getFitWidth() / 2);
        double newY = scene.getHeight() / 2 - (tank.getY() + tank.getFitHeight() / 2);

        main.setTranslateX(newX);
        main.setTranslateY(newY);
    }

    public static void updateScore(int points) {
        score += points;
        scoreText.setText("Score: " + score);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
