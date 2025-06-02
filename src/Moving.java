import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;


public class Moving {

    //In this part you can see variables we are going to use.
    private static boolean isShooting = false;
    private static final double bulletSpeed = 3;
    private static final Image bulletImage = new Image(Moving.class.getResource("/bullet.png").toExternalForm());
    private static AnimationTimer collisionTimer;
    private static AnimationTimer movementTimer;
    private static boolean movementEnabled = true;
    private static ArrayList<AnimationTimer> activeTimers = new ArrayList<>();
    private static final long SHOOT_COOLDOWN_MS = 1000; // 0.5 saniye cooldown
    private static long lastShotTime = 0;
    private static boolean isPaused = false;
    private static Pane pauseMenu;
    private static ArrayList<ImageView> playerBullets;
    private static ArrayList<AnimationTimer> bulletTimers;


    // Function to move player's tank.
    public static void moveTank(Scene scene, ImageView tank, Animation animation, ArrayList<ImageView> walls, Pane main, ArrayList<ImageView> bullets) {

        createPauseMenu(main, scene);

        final Set<KeyCode> pressedKeys = new HashSet<>();

        // Creating key combinations that we are going to play game.
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                if (isPaused || Main.isGameOver()) {
                    System.exit(0);
                }
            } else if (event.getCode() == KeyCode.P) {
                togglePause(main);
            } else if (event.getCode() == KeyCode.R) {
                if (isPaused || Main.isGameOver()) {
                    Main.restartGame();
                }
            } else if (!isPaused) {
                pressedKeys.add(event.getCode());

                if (event.getCode() == KeyCode.X && !isShooting && movementEnabled) {
                    shoot(main, tank, bullets, walls, Main.getEnemies());
                    isShooting = true;
                }
            }
        });

        scene.setOnKeyReleased(event -> {
            pressedKeys.remove(event.getCode());

            if (event.getCode() == KeyCode.X) {
                isShooting = false;
            }
        });

        collisionTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                reduceHP(main, Main.getPlayerTank(), Enemy.getEnemyBullets());
            }
        };
        collisionTimer.start();

        movementTimer = new AnimationTimer() {
            public void handle(long now) {
                if (!movementEnabled) return;

                double dx = 0;
                double dy = 0;

                // In here we rotate our tank and make it move.
                if (pressedKeys.contains(KeyCode.UP)) {
                    tank.setRotate(270);
                    dy = -1.25;
                } else if (pressedKeys.contains(KeyCode.DOWN)) {
                    tank.setRotate(90);
                    dy = 1.25;
                } else if (pressedKeys.contains(KeyCode.LEFT)) {
                    tank.setRotate(180);
                    dx = -1.25;
                } else if (pressedKeys.contains(KeyCode.RIGHT)) {
                    tank.setRotate(0);
                    dx = 1.25;
                }

                if (dx != 0 || dy != 0) {
                    double nextX = tank.getX() + dx;
                    double nextY = tank.getY() + dy;

                    tank.setX(nextX);
                    tank.setY(nextY);

                    // It can't move if there is a wall.
                    boolean blocked = false;
                    for (ImageView wall : walls) {
                        if (tank.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                            blocked = true;
                            break;
                        }
                    }

                    if (blocked) {
                        tank.setX(tank.getX() - dx);
                        tank.setY(tank.getY() - dy);
                        animation.stop();
                    } else {
                        animation.start();
                        Main.updateCamera(main, tank, scene);
                    }

                } else {
                    animation.stop();

                }
            }
        };movementTimer.start();

    }

    //Shooting method for player's tank.
    private static void shoot(Pane main, ImageView tank, ArrayList<ImageView> bullets, ArrayList<ImageView> walls, ArrayList<Enemy> enemies) {
        if (!isMovementEnabled()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN_MS) {
            return;
        }

        // Cooldown part for shooting
        lastShotTime = currentTime;

        //Creating our bullet
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(5);
        bullet.setFitHeight(5);
        bullet.setVisible(true);

        double tankCenterX = tank.getX() + tank.getFitWidth() / 2;
        double tankCenterY = tank.getY() + tank.getFitHeight() / 2;

        bullet.setX(tankCenterX - bullet.getFitWidth() / 2);
        bullet.setY(tankCenterY - bullet.getFitHeight() / 2);


        main.getChildren().add(bullet);
        bullets.add(bullet);

        double bulletAngle = tank.getRotate();

        AnimationTimer bulletTimer = new AnimationTimer() {
            public void handle(long now) {

                double dx = 0;
                double dy = 0;

                // It shoots wherever tank's looking.
                if (bulletAngle == 0) dx = bulletSpeed;
                else if (bulletAngle == 180) dx = -bulletSpeed;
                else if (bulletAngle == 90) dy = bulletSpeed;
                else if (bulletAngle == 270) dy = -bulletSpeed;

                bullet.setX(bullet.getX() + dx);
                bullet.setY(bullet.getY() + dy);

                for (ImageView wall : walls) {
                    if (bullet.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                        main.getChildren().remove(bullet);
                        bullets.remove(bullet);

                        // Creating our small explosion for our bullets if they crash into walls.
                        Image explosionImage = new Image(Moving.class.getResource("/smallExplosion.png").toExternalForm());
                        ImageView explosion = new ImageView(explosionImage);
                        explosion.setFitWidth(10);
                        explosion.setFitHeight(10);
                        explosion.setX(bullet.getX());
                        explosion.setY(bullet.getY());
                        main.getChildren().add(explosion);

                        new Timeline(new KeyFrame(Duration.millis(300), e -> {
                            main.getChildren().remove(explosion);
                        })).play();

                        stop();
                        activeTimers.remove(this);
                        return;
                    }
                }

                for (Enemy enemy : enemies) {
                    if (enemy.isAlive() && bullet.getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {

                        enemy.destroy();
                        main.getChildren().remove(bullet);
                        bullets.remove(bullet);
                        stop();
                        activeTimers.remove(this);
                        return;
                    }
                }
            }
        };activeTimers.add(bulletTimer);
        bulletTimer.start();
    }

    //A status for give permission to tank for moving.
    public static boolean isMovementEnabled() {
        return movementEnabled;
    }

    //Method for to reduce hp of player's tank if it gets hit or crah to another tank.
    private static void reduceHP(Pane main, ImageView playerTank, ArrayList<ImageView> enemyBullets) {
        if (!movementEnabled || Main.isGameOver()) return;

        for (ImageView bullet : new ArrayList<>(enemyBullets)) {
            if (bullet.getBoundsInParent().intersects(playerTank.getBoundsInParent())) {

                main.getChildren().remove(bullet);
                enemyBullets.remove(bullet);

                Main.updateLives(-1);
                explodeTank(main, playerTank);
                break;
            }
        }


        for (Enemy enemy : Main.getEnemies()) {
            if (enemy.isAlive() && playerTank.getBoundsInParent().intersects(enemy.getView().getBoundsInParent())) {
                Main.updateLives(-1);
                explodeTank(main, playerTank);
                enemy.destroy();
                break;
            }
        }
    }

    //Explosion effect for deaths.
    public static void explodeTank(Pane main, ImageView tank) {
        Image explosion = new Image(Moving.class.getResource("/explosion.png").toExternalForm());
        ImageView ex = new ImageView(explosion);
        ex.setFitWidth(40);
        ex.setFitHeight(40);
        ex.setX(tank.getX() + tank.getFitWidth() / 2 - 20);
        ex.setY(tank.getY() + tank.getFitHeight() / 2 - 20);
        main.getChildren().add(ex);

        tank.setVisible(false);
        setMovementEnabled(false);

        new Timeline(new KeyFrame(Duration.millis(500), e -> {
            main.getChildren().remove(ex);

            if (!Main.isGameOver()) {
                tank.setX(250);
                tank.setY(250);
                tank.setVisible(true);
                Main.updateCamera(main, tank, Main.getScene());
                tank.setRotate(0);
                setMovementEnabled(true);
            }
        })).play();
    }

    // This method stops timers if game is paused or game is over.
    public static void stopAllTimers() {
        if (collisionTimer != null) {
            collisionTimer.stop();
        }
        if (movementTimer != null) {
            movementTimer.stop();
        }

        for (AnimationTimer timer : activeTimers) {
            timer.stop();
        }
        activeTimers.clear();
    }

    // This method stops enemies if game is paused or game is over.
    public static void stopAllEnemies() {
        for (Enemy enemy : Main.getEnemies()) {
            enemy.stopMovement();
        }
    }

    //In here we create our pause menu.
    private static void createPauseMenu(Pane main, Scene scene) {
        pauseMenu = new Pane();
        pauseMenu.setVisible(false);

        Rectangle bg = new Rectangle(scene.getWidth(), scene.getHeight(), Color.rgb(0, 0, 0, 0.7));

        Text pauseText = new Text("PAUSED");
        pauseText.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        pauseText.setFill(Color.WHITE);

        Text resumeText = new Text("Press P to Resume");
        resumeText.setFont(Font.font("Arial", 30));
        resumeText.setFill(Color.WHITE);

        Text exitText = new Text("Press ESC to Exit");
        exitText.setFont(Font.font("Arial", 30));
        exitText.setFill(Color.WHITE);

        pauseText.setLayoutX(scene.getWidth() / 2 - pauseText.getLayoutBounds().getWidth() / 2);
        pauseText.setLayoutY(scene.getHeight() / 2 - 50);

        resumeText.setLayoutX(scene.getWidth() / 2 - resumeText.getLayoutBounds().getWidth() / 2);
        resumeText.setLayoutY(scene.getHeight() / 2 + 20);

        exitText.setLayoutX(scene.getWidth() / 2 - exitText.getLayoutBounds().getWidth() / 2);
        exitText.setLayoutY(scene.getHeight() / 2 + 70);

        pauseMenu.getChildren().addAll(bg, pauseText, resumeText, exitText);
        main.getChildren().add(pauseMenu);
    }

    // And this part is for usage of pause menu.
    private static void togglePause(Pane main) {
        isPaused = !isPaused;
        pauseMenu.setVisible(isPaused);
        setMovementEnabled(!isPaused);

        if (isPaused) {
            for (Node node : main.getChildren()) {
                if (node != pauseMenu) {
                    node.setOpacity(0.5);
                }
            }
            stopAllTimers();
            for (Enemy enemy : Main.getEnemies()) {
                enemy.stopMovement();
                enemy.stopAnimation();
            }
        } else {
            for (Node node : main.getChildren()) {
                node.setOpacity(1.0);
            }
            collisionTimer.start();
            movementTimer.start();
            for (Enemy enemy : Main.getEnemies()) {
                enemy.startMovement();
            }
        }
    }

    // This method clears bullet after bullet crashes into something.
    public static void clearBullets() {
        if (playerBullets != null) {
            for (ImageView bullet : playerBullets) {
                if (bullet != null && bullet.getParent() != null) {
                    ((Pane) bullet.getParent()).getChildren().remove(bullet);
                }
            }
            playerBullets.clear();
        }

        ArrayList<ImageView> enemyBullets = Enemy.getEnemyBullets();
        if (enemyBullets != null) {
            for (ImageView bullet : enemyBullets) {
                if (bullet != null && bullet.getParent() != null) {
                    ((Pane) bullet.getParent()).getChildren().remove(bullet);
                }
            }
            enemyBullets.clear();
        }

        if (bulletTimers != null) {
            for (AnimationTimer timer : bulletTimers) {
                if (timer != null) {
                    timer.stop();
                }
            }
            bulletTimers.clear();
        }
    }

    // getter for pause menu
    public static Pane getPauseMenu() {
        return pauseMenu;
    }

    // Several setter methods.
    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    public static void setMovementEnabled(boolean enabled) {
        movementEnabled = enabled;
    }
}
