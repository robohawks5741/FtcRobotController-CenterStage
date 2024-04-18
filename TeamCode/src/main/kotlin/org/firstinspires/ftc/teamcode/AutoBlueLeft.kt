package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo

@Autonomous(name = "# Clay Blue Left")
class AutoBlueLeft : AutoSuper() {
    override val alliance = Alliance.BLUE
    override val side = AllianceSide.BACKDROP_SIDE

    override fun runSpecialized() {
        Thread.sleep(5000)
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(22.50, 0.0), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(10.42, -3.43), Math.toRadians(0.0))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(19.85, 34.42), Math.toRadians(270.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo (Vector2d(24.4, -7.5), 0.0)
                .splineToConstantHeading(Vector2d(23.386, 0.0), 0.0)
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(25.94, 34.42), Math.toRadians(270.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(22.9, -9.1), Math.toRadians(-52.26))
                .splineToConstantHeading(Vector2d(11.85, 5.24), Math.toRadians(-40.26))
                .turnTo(Math.toRadians(270.0))
                .splineToConstantHeading(Vector2d(30.8, 34.42), Math.toRadians(270.0))
                .build()
        })

        liftPos = 600
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
        sleep(300)
        armL.position = 0.65
        armR.position = 0.35
        sleep(300)
        clawL.position = 0.0
        clawR.position = 0.36
        sleep(300)
        runBlocking(
            drive.actionBuilder(drive.pose)
                .lineToY(29.0)
                .build()
        )
        armR.position = 0.05
        armL.position = 0.95
        sleep(100)
        liftPos = 0
        slideR.targetPosition = -liftPos
        slideL.targetPosition = liftPos
        slideR.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideL.mode = DcMotor.RunMode.RUN_TO_POSITION
        slideR.power = 1.0
        slideL.power = 1.0
        inlift.position = 0.22
        sleep(200)
        //Left side
        runBlocking(
            drive.actionBuilder(drive.pose)
                .strafeToConstantHeading(Vector2d(1.81, 34.19))
                .splineToConstantHeading(Vector2d(1.38, 45.79), Math.toRadians(270.0))
                .build()
        )
        //Right side
      /*  runBlocking(
            drive.actionBuilder(drive.pose)
                .strafeToConstantHeading(Vector2d(48.98, 34.19))
                .splineToConstantHeading(Vector2d(48.98, 45.79), Math.toRadians(270.0))
                .build()
        )*/
    }

}
