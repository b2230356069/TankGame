import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class Moving {

    private static boolean isShooting = false;
    private static final double bulletSpeed = 3;
    private static final Image bulletImage = new Image(Moving.class.getResource("/bullet.png").toExternalForm());
    private static AnimationTimer collisionTimer;
    private static AnimationTimer movementTimer;
    private static boolean movementEnabled = true;
    private static ArrayList<AnimationTimer> activeTimers = new ArrayList<>();
    private static final long SHOOT_COOLDOWN_MS = 1000; // 0.5 saniye cooldown
    private static long lastShotTime = 0;

    public static void moveTank(Scene scene, ImageView tank, Animation animation, ArrayList<ImageView> walls, Pane main, ArrayList<ImageView> bullets) {

        final Set<KeyCode> pressedKeys = new HashSet<>();

        scene.setOnKeyPressed(event -> {
            pressedKeys.add(event.getCode());

            if (event.getCode() == KeyCode.X && !isShooting && movementEnabled) {
                shoot(main, tank, bullets, walls, TankGame2025.getEnemies());
                isShooting = true;
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
                reduceHP(main, TankGame2025.getPlayerTank(), Enemy.getEnemyBullets());
            }
        };
        collisionTimer.start();

        movementTimer = new AnimationTimer() {
            public void handle(long now) {
                if (!movementEnabled) return;

                double dx = 0;
                double dy = 0;

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
                        TankGame2025.updateCamera(main, tank, scene);
                    }

                } else {
                    animation.stop();

                }
            }
        };movementTimer.start();

    }

    private static void shoot(Pane main, ImageView tank, ArrayList<ImageView> bullets, ArrayList<ImageView> walls, ArrayList<Enemy> enemies) {
        if (!isMovementEnabled()) return;

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastShotTime < SHOOT_COOLDOWN_MS) {
            return;
        }

        lastShotTime = currentTime;

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

    public static boolean isMovementEnabled() {
        return movementEnabled;
    }

    public static void setMovementEnabled(boolean enabled) {
        movementEnabled = enabled;
    }

    private static void reduceHP(Pane main, ImageView playerTank, ArrayList<ImageView> enemyBullets) {

        if (!movementEnabled) return;

        for (ImageView bullet : new ArrayList<>(enemyBullets)) {
            if (bullet.getBoundsInParent().intersects(playerTank.getBoundsInParent())) {
                for (ImageView eb : new ArrayList<>(enemyBullets)) {
                    eb.setVisible(false);
                    main.getChildren().remove(eb);
                    Enemy.getEnemyBullets().remove(eb);
                }
                TankGame2025.updateLives(-1);

                Image explosion = new Image(Moving.class.getResource("/explosion.png").toExternalForm());
                ImageView ex = new ImageView(explosion);
                ex.setFitWidth(40);
                ex.setFitHeight(40);
                ex.setX(playerTank.getX() + playerTank.getFitWidth() / 2 - 20);
                ex.setY(playerTank.getY() + playerTank.getFitHeight() / 2 - 20);
                main.getChildren().add(ex);

                playerTank.setVisible(false);
                setMovementEnabled(false);

                new Timeline(new KeyFrame(Duration.millis(1000), e -> {

                    main.getChildren().remove(ex);


                    playerTank.setX(250);
                    playerTank.setY(250);
                    playerTank.setVisible(true);
                    TankGame2025.updateCamera(main, playerTank, TankGame2025.getScene());

                    playerTank.setRotate(0);
                    setMovementEnabled(true);
                })).play();

                break;
            }
        }
    }

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

    public static void stopAllEnemies() {
        for (Enemy enemy : TankGame2025.getEnemies()) {
            enemy.stopMovement();
        }
    }
}
