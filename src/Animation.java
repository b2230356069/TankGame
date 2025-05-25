import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Animation {

    private Image tank1;
    private Image tank2;
    private final Timeline animation;
    private int currentFrame = 0;

    public Animation(ImageView tankView) {
        tank1 = new Image(getClass().getResource("/yellowTank1.png").toExternalForm());
        tank2 = new Image(getClass().getResource("/yellowTank2.png").toExternalForm());

        animation = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (currentFrame == 0) {
                tankView.setImage(tank2);
                currentFrame = 1;
            } else {
                tankView.setImage(tank1);
                currentFrame = 0;
            }
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

