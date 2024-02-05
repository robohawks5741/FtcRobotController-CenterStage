package org.firstinspires.ftc.teamcode;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.openftc.easyopencv.OpenCvPipeline;

import java.util.Arrays;
import java.util.List;


public class TeamElementPipeline extends OpenCvPipeline {
    List<Integer> ELEMENT_COLOR = Arrays.asList(0, 0, 255); //(red, green, blue)

    /**
     * TODO: document
     */
    static SpikeMark spikeMark = SpikeMark.RIGHT;

    // TODO: see if this needs to be atomic
    boolean toggleShow = true;

    Mat original;

    Mat zone1;
    Mat zone2;

    Scalar avgColor1;
    Scalar avgColor2;

    double distance1 = 1;
    double distance2 = 1;

    double maxDistance = 0;

    @Override
    public void init(Mat mat) {
        //Defining Zones
        //Rect(top left x, top left y, bottom right x, bottom right y)
        zone1 = mat.submat(new Rect(60, 170, 356, 285));
        zone2 = mat.submat(new Rect(735, 170, 253, 230));
    }

    @Override
    public Mat processFrame(Mat input) {

        //Creating duplicate of original frame with no edits
        original = input.clone();

//        input = input.submat(new Rect(0));
        // TODO: use OKLab/Lch for color delta
//        Imgproc.cvtColor(input, grey, Imgproc.COLOR_RGBALab);

        //Averaging the colors in the zones
        avgColor1 = Core.mean(zone1);
        avgColor2 = Core.mean(zone2);

        //Putting averaged colors on zones (we can see on camera now)
        zone1.setTo(avgColor1);
        zone2.setTo(avgColor2);

        distance1 = colorDistance(avgColor1, ELEMENT_COLOR);
        distance2 = colorDistance(avgColor2, ELEMENT_COLOR);

        if ((distance1 > 195) && (distance2 > 190)) {
            spikeMark = SpikeMark.LEFT;
            maxDistance = -1;
        } else {
            maxDistance = Math.min(distance1, distance2);

            if (maxDistance == distance1) {
                //telemetry.addData("Zone 1 Has Element", distance1);
                spikeMark = SpikeMark.RIGHT;

            } else {
                //telemetry.addData("Zone 2 Has Element", distance2);
                spikeMark = SpikeMark.CENTER;
            }
        }

        // Allowing for the showing of the averages on the stream
        if (toggleShow) {
            return input;
        } else {
            return original;
        }
    }

    private double colorDistance(Scalar color1, List<Integer> color2) {
        double r1 = color1.val[0];
        double g1 = color1.val[1];
        double b1 = color1.val[2];

        int r2 = color2.get(0);
        int g2 = color2.get(1);
        int b2 = color2.get(2);

//        deltaL = L1 - L2
//        C1 = √(a1² + b1²)
//        C2 = √(a2² + b2²)
//        ΔC = C1 - C2
//        Δa = a1 - a2
//        Δb = b1 - b2
//        ΔH = √(Δa² + Δb² - ΔC²)
//        ΔEOK = √(ΔL² + ΔC² + ΔH²)

        return Math.sqrt(Math.pow((r1 - r2), 2) + Math.pow((g1 - g2), 2) + Math.pow((b1 - b2), 2));
    }

    public void setAlliancePipe(Alliance alliance) {
        switch (alliance) {
            case RED: {
                ELEMENT_COLOR = Arrays.asList(255, 0, 0);
                break;
            }
            case BLUE: {
                ELEMENT_COLOR = Arrays.asList(0, 0, 255);
                break;
            }
        }
    }

    public SpikeMark getElementSpikeMark() {
        return spikeMark;
    }

    public double getMaxDistance() {
        return maxDistance;
    }

    public void toggleShowAverageZone() {
        toggleShow = !toggleShow;
    }

}