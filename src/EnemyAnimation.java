import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

//Animation class for enemy tanks.
public class EnemyAnimation {
    private Image enemyTank1;
    private Image enemyTank2;
    private final Timeline animation;
    private int currentFrame = 0;

    public EnemyAnimation(ImageView enemyTankView) {
        enemyTank1 = new Image(getClass().getResource("/whiteTank1.png").toExternalForm());
        enemyTank2 = new Image(getClass().getResource("/whiteTank2.png").toExternalForm());

        animation = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            enemyTankView.setImage((currentFrame == 0) ? enemyTank2 : enemyTank1);
            currentFrame = 1 - currentFrame;
        }));
        animation.setCycleCount(Timeline.INDEFINITE);
    }

    public void start() {
        animation.play();
    }

    public void stop() {
        animation.stop();
    }
}
