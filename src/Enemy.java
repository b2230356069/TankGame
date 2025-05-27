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
    private Timeline shootingTimer;
    private static final double bulletSpeed = 3;
    private static final Image bulletImage = new Image(Enemy.class.getResource("/bullet.png").toExternalForm());

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
            case 0 -> { dx = 0; dy = -1; enemyTankView.setRotate(270); }
            case 1 -> { dx = 0; dy = 1; enemyTankView.setRotate(90); }
            case 2 -> { dx = -1; dy = 0; enemyTankView.setRotate(180); }
            case 3 -> { dx = 1; dy = 0; enemyTankView.setRotate(0); }
        }
    }

    private void startMoving() {
        AnimationTimer timer = new AnimationTimer() {

            private int directionChangeCounter = 0;

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
                } else if (nextX < 0 || nextX > pane.getWidth() - enemyTankView.getFitWidth()
                        || nextY < 0 || nextY > pane.getHeight() - enemyTankView.getFitHeight()) {
                    enemyTankView.setX(x);
                    enemyTankView.setY(y);
                    setRandomDirection();
                } else {
                    x = nextX;
                    y = nextY;
                }

                directionChangeCounter++;
                if (directionChangeCounter % 300 == 0) {
                    setRandomDirection();
                }
            }
        };
        timer.start();
    }

    private void startShooting() {
        shootingTimer = new Timeline(new KeyFrame(Duration.seconds(2), e -> shoot()));
        shootingTimer.setCycleCount(Timeline.INDEFINITE);
        shootingTimer.play();
    }

    private void shoot() {
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(5);
        bullet.setFitHeight(5);

        double tankCenterX = enemyTankView.getX() + enemyTankView.getFitWidth() / 2;
        double tankCenterY = enemyTankView.getY() + enemyTankView.getFitHeight() / 2;

        bullet.setX(tankCenterX - bullet.getFitWidth() / 2);
        bullet.setY(tankCenterY - bullet.getFitHeight() / 2);

        pane.getChildren().add(bullet);
        bullets.add(bullet);

        double bulletAngle = enemyTankView.getRotate();

        new AnimationTimer() {
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
                        pane.getChildren().remove(bullet);
                        bullets.remove(bullet);

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

                        stop();
                        return;
                    }
                }
            }
        }.start();
    }
}


