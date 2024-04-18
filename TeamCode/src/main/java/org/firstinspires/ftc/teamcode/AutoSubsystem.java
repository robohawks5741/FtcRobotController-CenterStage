package org.firstinspires.ftc.teamcode;

import androidx.annotation.NonNull;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraFactory;
import org.openftc.easyopencv.OpenCvCameraRotation;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.HardwareMap;

public class AutoSubsystem {
    OpenCvCamera camera;
    public TeamElementPipeline pipeline;
    int scaleFactor = 2;
    int resolutionWidth = 1920 / scaleFactor;
    int resolutionHeight = 1080 / scaleFactor;
    SpikeMark spikeMark = SpikeMark.RIGHT;
    HardwareMap hardwareMap;
    OpMode opMode;
    Telemetry telemetry;

    private class EpicCameraListener implements OpenCvCamera.AsyncCameraOpenListener {
        HardwareMap hardwareMap;
        Telemetry telemetry;

        public EpicCameraListener(HardwareMap hardwareMap, Telemetry telemetry) {
            this.hardwareMap = hardwareMap;
            this.telemetry = telemetry;
        }

        @Override
        public void onOpened() {
            camera.startStreaming(resolutionWidth, resolutionHeight, OpenCvCameraRotation.UPRIGHT);
        }

        @Override
        public void onError(int errorCode) {
            telemetry.addLine("Camera open encountered an error! " + errorCode);
        }
    }

    public AutoSubsystem(@NonNull OpMode opMode, boolean isOnRightSide) {
        this.opMode = opMode;
        this.hardwareMap = opMode.hardwareMap;
        this.telemetry = opMode.telemetry;

        assert hardwareMap != null;
        camera = OpenCvCameraFactory.getInstance().createWebcam(hardwareMap.get(WebcamName.class, "Webcam 1"));
        pipeline = new TeamElementPipeline(isOnRightSide, scaleFactor);

        camera.setPipeline(pipeline);
        camera.openCameraDeviceAsync(new EpicCameraListener(hardwareMap, telemetry));
    }

    public void setAlliance(Alliance alliance) {
        pipeline.setAlliancePipe(alliance);
    }

    public void detectElement() {
        spikeMark = pipeline.getElementSpikeMark();
        telemetry.addData("Element Zone", spikeMark);
//        return spikeMark;
    }

}

