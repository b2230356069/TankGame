import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;

public class Main extends Application {

    //In this part you can see variables we are going to use.
    private static int score = 0;
    private static Text scoreText;
    private static ArrayList<Enemy> enemies = new ArrayList<>();
    private static int lives = 3;
    private static Text livesText;
    private static ImageView player;
    private static Scene gameScene;
    private static Text gameOverText;
    private static Text scoreFinalText;
    private static Text restartText;
    private static Pane gameOverPane;
    private static Animation animation;
    private static Pane mainPane;
    private static Pane uiPane;
    private static StackPane stackPane;

    public void start(Stage firstStage) {

        // Here, we create our main pane, our title, and our ui pane for score and lives.
        firstStage.setTitle("TankGame 2025");
        Pane main = new Pane();
        Pane ui = new Pane();

        //We combine them in here in a full StackPane
        StackPane all = new StackPane();
        all.getChildren().addAll(main, ui);

        mainPane = main;
        uiPane = ui;
        stackPane = all;

        //We create our map
        int sceneWidth = 1000;
        int sceneHeight = 1000;
        Map map = new Map(main, sceneWidth, sceneHeight);
        map.createExtraWalls(main);

        //Text parts for score, lives, game over scene.
        scoreText = new Text(20, 30, "Score: 0");
        scoreText.setFont(Font.font("Arial", 20));
        scoreText.setFill(Color.WHITE);

        livesText = new Text(20, 60, "Lives: 3");
        livesText.setFont(Font.font("Arial", 20));
        livesText.setFill(Color.WHITE);

        gameOverPane = new Pane();
        gameOverPane.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        gameOverPane.setVisible(false);

        gameOverText = new Text("GAME OVER");
        gameOverText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        gameOverText.setFill(Color.RED);

        scoreFinalText = new Text();
        scoreFinalText.setFont(Font.font("Arial", 30));
        scoreFinalText.setFill(Color.WHITE);

        restartText = new Text("Press R to Restart");
        restartText.setFont(Font.font("Arial", 20));
        restartText.setFill(Color.WHITE);

        gameOverText.setX(main.getWidth()/2 );
        gameOverText.setY(main.getHeight()/2 + 300);

        scoreFinalText.setX(main.getWidth()/2);
        scoreFinalText.setY(main.getHeight()/2 + 350);

        restartText.setX(main.getWidth()/2);
        restartText.setY(main.getHeight()/2 + 400);

        gameOverPane.getChildren().addAll(gameOverText, scoreFinalText, restartText);
        all.getChildren().add(gameOverPane);

        ui.getChildren().addAll(scoreText, livesText);

        //Creating our player tank.
        player = new ImageView();
        player.setImage(new Image(getClass().getResource("/yellowTank1.png").toExternalForm()));

        player.setX(250);
        player.setY(250);

        player.setFitHeight(25);
        player.setFitWidth(25);

        main.getChildren().add(player);

        animation = new Animation(player);

        // This is our pop-up window. It's a little bit smaller so you can see vertical and horizontal scrolling clearly.
        Scene scene = new Scene(all, 1000, 750);
        ArrayList<ImageView> bullets = new ArrayList<>();
        ArrayList<ImageView> walls = new ArrayList<>();

        all.setStyle("-fx-background-color: black;");

        // Our tank can start to move with this command.
        Moving.moveTank(scene, player, animation, map.getWalls(), main, bullets);

        //Adding enemies, they will respawn randomly if they get killed.
        enemies.add(new Enemy(main, 100, 700, map.getWalls()));
        enemies.add(new Enemy(main, 700, 100, map.getWalls()));
        enemies.add(new Enemy(main, 700, 700, map.getWalls()));
        enemies.add(new Enemy(main, 400, 400, map.getWalls()));
        enemies.add(new Enemy(main, 400, 600, map.getWalls()));
        enemies.add(new Enemy(main, 600, 400, map.getWalls()));
        enemies.add(new Enemy(main, 600, 600, map.getWalls()));

        gameScene = scene;
        firstStage.setScene(scene);
        firstStage.show();
    }

    // Our vertical and horizontal scrolling method.
    public static void updateCamera(Pane main, ImageView tank, Scene scene) {
        double newX = scene.getWidth() / 2 - (tank.getX() + tank.getFitWidth() / 2);
        double newY = scene.getHeight() / 2 - (tank.getY() + tank.getFitHeight() / 2);

        main.setTranslateX(newX);
        main.setTranslateY(newY);
    }

    // Method for updating score
    public static void updateScore(int points) {
        score += points;
        scoreText.setText("Score: " + score);
    }

    // Method for updating lives
    public static void updateLives(int change) {
        lives += change;
        livesText.setText("Lives: " + lives);

        if (lives <= 0) {
            gameOver();
        }
    }

    // Our game over part
    private static void gameOver() {
        Moving.setMovementEnabled(false);
        Moving.stopAllTimers();
        Moving.stopAllEnemies();

        if (Moving.getPauseMenu() != null) {
            Moving.getPauseMenu().setVisible(false);
        }

        for (Enemy enemy : enemies) {
            enemy.stopMovement();
            enemy.stopAnimation();
        }
        animation.stop();

        scoreFinalText.setText("Score: " + score);

        gameOverText.setX(gameScene.getWidth()/2 - gameOverText.getLayoutBounds().getWidth()/2);
        scoreFinalText.setX(gameScene.getWidth()/2 - scoreFinalText.getLayoutBounds().getWidth()/2);
        restartText.setX(gameScene.getWidth()/2 - restartText.getLayoutBounds().getWidth()/2);

        gameOverPane.setVisible(true);
    }

    // Our restart game function. Basically restarts everything.
    public static void restartGame() {

        Moving.setPaused(false);
        if (Moving.getPauseMenu() != null) {
            Moving.getPauseMenu().setVisible(false);
        }

        score = 0;
        lives = 3;
        scoreText.setText("Score: 0");
        livesText.setText("Lives: 3");

        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.stopMovement();
            enemy.stopAnimation();
            mainPane.getChildren().remove(enemy.getView());
        }
        enemies.clear();

        Moving.clearBullets();

        player.setX(250);
        player.setY(250);
        player.setRotate(0);
        player.setVisible(true);

        Moving.setMovementEnabled(true);
        Moving.stopAllTimers();
        animation.start();

        mainPane.getChildren().removeIf(node -> node instanceof ImageView && node != player);
        Map map = new Map(mainPane, 1000, 1000);
        map.createExtraWalls(mainPane);

        enemies.add(new Enemy(mainPane, 100, 700, map.getWalls()));
        enemies.add(new Enemy(mainPane, 700, 100, map.getWalls()));
        enemies.add(new Enemy(mainPane, 700, 700, map.getWalls()));
        enemies.add(new Enemy(mainPane, 400, 400, map.getWalls()));
        enemies.add(new Enemy(mainPane, 400, 600, map.getWalls()));
        enemies.add(new Enemy(mainPane, 600, 400, map.getWalls()));
        enemies.add(new Enemy(mainPane, 600, 600, map.getWalls()));

        gameOverPane.setVisible(false);

        Moving.moveTank(gameScene, player, animation, map.getWalls(), mainPane, new ArrayList<>());

        updateCamera(mainPane, player, gameScene);

        for (Node node : mainPane.getChildren()) {
            node.setOpacity(1.0);
        }
    }

    // A game over status for to use in other methods.
    public static boolean isGameOver() {
        return lives <= 0;
    }

    //  Getter methods.
    public static ImageView getPlayerTank() {
        return player;
    }

    public static Scene getScene() {
        return gameScene;
    }

    public static ArrayList<Enemy> getEnemies() {
        return enemies;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
