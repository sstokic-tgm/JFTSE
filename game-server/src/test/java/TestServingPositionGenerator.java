import com.jftse.emulator.server.core.utils.ServingPositionGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TestServingPositionGenerator {
    @Test
    public void testRandomServingPositionYOffsetForX0() {
        int servingPositionXOffset = ServingPositionGenerator.LEFT_X_MIN;
        int servingPositionYOffset = ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        assertTrue((servingPositionYOffset >= ServingPositionGenerator.MIDDLE_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.MIDDLE_Y_MAX) ||
                        (servingPositionYOffset >= ServingPositionGenerator.RIGHT_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.RIGHT_Y_MAX),
                "Y offset is within the valid range for X offset 0");
    }

    @Test
    public void testRandomServingPositionYOffsetForX6() {
        int servingPositionXOffset = ServingPositionGenerator.RIGHT_X_MAX;
        int servingPositionYOffset = ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        assertTrue((servingPositionYOffset >= ServingPositionGenerator.MIDDLE_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.MIDDLE_Y_MAX) ||
                        (servingPositionYOffset >= ServingPositionGenerator.LEFT_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.LEFT_Y_MAX),
                "Y offset is within the valid range for X offset 6");
    }


    @Test
    public void testRandomServingPositionYOffsetForX4() {
        int servingPositionXOffset = ServingPositionGenerator.MIDDLE_X;
        int servingPositionYOffset = ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        assertTrue(servingPositionYOffset >= ServingPositionGenerator.LEFT_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.RIGHT_Y_MAX, "Y offset is within the valid range for X offset 4");
    }

    @Test
    public void testRandomServingPositionYOffsetForX123() {
        for (int x = ServingPositionGenerator.LEFT_X_MIN + 1; x <= ServingPositionGenerator.LEFT_X_MAX; x++) {
            int servingPositionXOffset = x;
            int servingPositionYOffset = ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

            assertTrue(servingPositionYOffset >= ServingPositionGenerator.LEFT_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.RIGHT_Y_MAX, "Y offset is within the valid range for X offset " + x);
        }
    }

    @Test
    public void testRandomServingPositionYOffsetForX5() {
        int servingPositionXOffset = ServingPositionGenerator.RIGHT_X_MIN;
        int servingPositionYOffset = ServingPositionGenerator.randomServingPositionYOffset(servingPositionXOffset);

        assertTrue(servingPositionYOffset >= ServingPositionGenerator.LEFT_Y_MIN && servingPositionYOffset <= ServingPositionGenerator.RIGHT_Y_MAX, "Y offset is within the valid range for X offset 5");
    }
}
