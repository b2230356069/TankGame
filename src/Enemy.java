import javafx.animation.AnimationTimer;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import java.util.ArrayList;
import java.util.Random;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

public class Enemy {

    private ImageView enemyTankView;
    private Image enemyTankImage;
    private double x, y;
    private double dx, dy;
    private double speed = 1;
    private Random random = new Random();
    private Pane pane;
    private ArrayList<ImageView> walls;
    private EnemyAnimation animation;
    private ArrayList<ImageView> bullets = new ArrayList<>();
    private AnimationTimer movementTimer;
    private Timeline shootingTimer;
    private static final double bulletSpeed = 3;
    private static final Image bulletImage = new Image(Enemy.class.getResource("/bullet.png").toExternalForm());
    private boolean isAlive = true;
    private static ArrayList<ImageView> enemyBullets = new ArrayList<>();
    private boolean isMoving = false;

    public Enemy(Pane pane, double x, double y, ArrayList<ImageView> walls) {
        this.pane = pane;
        this.x = x;
        this.y = y;
        this.walls = walls;
        enemyTankImage = new Image(getClass().getResource("/whiteTank1.png").toExternalForm());
        enemyTankView = new ImageView(enemyTankImage);


        enemyTankView.setFitWidth(25);
        enemyTankView.setFitHeight(25);
        enemyTankView.setX(x);
        enemyTankView.setY(y);

        pane.getChildren().add(enemyTankView);

        animation = new EnemyAnimation(enemyTankView);
        animation.start();

        setRandomDirection();
        startMoving();
        startShooting();
    }

    private void setRandomDirection() {
        int direction = random.nextInt(4);
        switch (direction) {
            case 0 -> { dx = 0; dy = -1.25; enemyTankView.setRotate(270); }
            case 1 -> { dx = 0; dy = 1.25; enemyTankView.setRotate(90); }
            case 2 -> { dx = -1.25; dy = 0; enemyTankView.setRotate(180); }
            case 3 -> { dx = 1.25; dy = 0; enemyTankView.setRotate(0); }
        }
    }

    private void startMoving() {
        movementTimer = new AnimationTimer() {

            private int changeIntervalMillis = (int) (getRandomIntervalSeconds() * 1000);
            private long lastDirectionChangeTimeMillis = System.currentTimeMillis();

            public void handle(long now) {
                double nextX = x + dx * speed;
                double nextY = y + dy * speed;

                enemyTankView.setX(nextX);
                enemyTankView.setY(nextY);

                boolean blocked = false;
                for (ImageView wall : walls) {
                    if (enemyTankView.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                        blocked = true;
                        break;
                    }
                }

                if (blocked) {
                    enemyTankView.setX(x);
                    enemyTankView.setY(y);
                    setRandomDirection();
                    lastDirectionChangeTimeMillis = now;
                    changeIntervalMillis = (int) (getRandomIntervalSeconds() * 1000);
                } else if (nextX < 0 || nextX > pane.getWidth() - enemyTankView.getFitWidth()
                        || nextY < 0 || nextY > pane.getHeight() - enemyTankView.getFitHeight()) {
                    enemyTankView.setX(x);
                    enemyTankView.setY(y);
                    setRandomDirection();
                    lastDirectionChangeTimeMillis = now;
                    changeIntervalMillis = (int) (getRandomIntervalSeconds() * 1000);
                } else {
                    x = nextX;
                    y = nextY;
                }

                if (System.currentTimeMillis() - lastDirectionChangeTimeMillis > changeIntervalMillis) {
                    setRandomDirection();
                    lastDirectionChangeTimeMillis = System.currentTimeMillis();
                    changeIntervalMillis = (int) (getRandomIntervalSeconds() * 1000);
                }
            }
        };
        movementTimer.start();
    }

    private void startShooting() {
        if (shootingTimer != null) {
            shootingTimer.stop();
        }
        shootingTimer = new Timeline(
                new KeyFrame(Duration.seconds(getRandomIntervalSeconds()), e -> shoot())
        );
        shootingTimer.setCycleCount(Timeline.INDEFINITE);
        shootingTimer.play();
    }

    private double getRandomIntervalSeconds() {
        return 1 + random.nextDouble() * 4;
    }

    public ImageView getView() {
        return enemyTankView;
    }

    public static ArrayList<ImageView> getEnemyBullets() {
        return enemyBullets;
    }

    private void shoot() {
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(5);
        bullet.setFitHeight(5);

        double tankCenterX = enemyTankView.getX() + enemyTankView.getFitWidth() / 2;
        double tankCenterY = enemyTankView.getY() + enemyTankView.getFitHeight() / 2;

        bullet.setX(tankCenterX - bullet.getFitWidth() / 2);
        bullet.setY(tankCenterY - bullet.getFitHeight() / 2);

        enemyBullets.add(bullet);
        pane.getChildren().add(bullet);

        double bulletAngle = enemyTankView.getRotate();

        AnimationTimer bulletTimer = new AnimationTimer() {
            private boolean active = true;

            public void handle(long now) {
                if (!active || !Moving.isMovementEnabled()) {
                    cleanupBullet(bullet);
                    this.stop();
                    return;
                }

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

                        Image explosionImage = new Image(Enemy.class.getResource("/smallExplosion.png").toExternalForm());
                        ImageView explosion = new ImageView(explosionImage);
                        explosion.setFitWidth(10);
                        explosion.setFitHeight(10);
                        explosion.setX(bullet.getX());
                        explosion.setY(bullet.getY());
                        pane.getChildren().add(explosion);

                        new Timeline(new KeyFrame(Duration.millis(300), e -> {
                            pane.getChildren().remove(explosion);
                        })).play();

                        cleanupBullet(bullet);
                        this.stop();
                        return;
                    }
                }
            }
        };
        bulletTimer.start();
    }

    private void cleanupBullet(ImageView bullet) {
        pane.getChildren().remove(bullet);
        bullets.remove(bullet);
        enemyBullets.remove(bullet);
    }

    private void respawn() {
        Random random = new Random();
        boolean validPosition = false;
        double newX = 0, newY = 0;

        while (!validPosition) {
            newX = random.nextInt((int)(pane.getWidth() - enemyTankView.getFitWidth()));
            newY = random.nextInt((int)(pane.getHeight() - enemyTankView.getFitHeight()));

            validPosition = true;
            ImageView tempView = new ImageView();
            tempView.setX(newX);
            tempView.setY(newY);
            tempView.setFitWidth(enemyTankView.getFitWidth());
            tempView.setFitHeight(enemyTankView.getFitHeight());

            for (ImageView wall : walls) {
                if (tempView.getBoundsInParent().intersects(wall.getBoundsInParent())) {
                    validPosition = false;
                    break;
                }
            }
        }

        this.x = newX;
        this.y = newY;
        enemyTankView.setX(newX);
        enemyTankView.setY(newY);
        isAlive = true;
        pane.getChildren().add(enemyTankView);
        animation.start();
        startShooting();
    }


    public void destroy() {
        if (!isAlive) return;

        isAlive = false;

        pane.getChildren().remove(enemyTankView);
        if (shootingTimer != null) {
            shootingTimer.stop();
        }

        Image explosionImage = new Image(getClass().getResource("/explosion.png").toExternalForm());
        ImageView explosion = new ImageView(explosionImage);
        explosion.setFitWidth(40);
        explosion.setFitHeight(40);
        explosion.setX(x - 7.5);
        explosion.setY(y - 7.5);
        pane.getChildren().add(explosion);

        TankGame2025.updateScore(100);

        new Timeline(new KeyFrame(Duration.millis(500), e -> {
            pane.getChildren().remove(explosion);
            respawn();
        })).play();
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void stopMovement() {
        if (movementTimer != null) {
            movementTimer.stop();
        }
        if (shootingTimer != null) {
            shootingTimer.stop();
        }
        if (animation != null) {
            animation.stop();
        }
        isMoving = false;
    }

    public void stopAnimation() {
        if (animation != null) {
            animation.stop();
        }
    }

    public void startMovement() {
        if (!isMoving) {
            if (movementTimer != null) {
                movementTimer.start();
            }
            if (animation != null) {
                animation.start();
            }
            if (shootingTimer != null) {
                shootingTimer.play();
            }
            isMoving = true;
        }
    }
}



