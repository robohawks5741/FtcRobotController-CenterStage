package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Vector2d
import com.acmerobotics.roadrunner.ftc.runBlocking
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.hardware.DcMotor

@Autonomous(name = "# Clay Red Right")
class AutoRedRight : AutoSuper() {
    override val alliance = Alliance.RED
    override val side = AllianceSide.BACKDROP_SIDE

    override fun runSpecialized() {
        runBlocking(when (placementZone) {
            SpikeMark.LEFT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(20.32, 9.60), Math.toRadians(44.51))
                .splineToConstantHeading(Vector2d(2.98, -7.4), Math.toRadians(44.51))
                .turnTo(Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(50.4, -10.67), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(31.70 , -33.90), Math.toRadians(90.0))
                .build()
            SpikeMark.CENTER -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(22.5, 5.97), Math.toRadians(3.0))
                .splineToConstantHeading(Vector2d(4.96, -20.0), 0.0)
                .splineToConstantHeading(Vector2d(49.75, -18.46), 0.0)
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(25.5

                    , -33.90), Math.toRadians(90.0))
                .build()
            SpikeMark.RIGHT -> drive.actionBuilder(beginPose)
                .splineTo(Vector2d(18.61, 0.0), Math.toRadians(0.0))
                .splineToConstantHeading(Vector2d(11.63, -11.35), Math.toRadians(0.0))
                .turnTo(Math.toRadians(90.0))
                .splineToConstantHeading(Vector2d(20.03, -33.90), Math.toRadians(90.0))
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
                .lineToY(-23.8)
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

        //right side
      /*  runBlocking(
            drive.actionBuilder(drive.pose)
                .strafeToConstantHeading(Vector2d(3.93, -29.9))
                .splineToConstantHeading(Vector2d(3.93, -45.99), Math.toRadians(90.0))
                .build()
        )*/

        //left side
        runBlocking(
            drive.actionBuilder(drive.pose)
                .strafeToConstantHeading(Vector2d(49.33, -29.9))
                .splineToConstantHeading(Vector2d(49.33, -45.99), Math.toRadians(90.0))
                .build()
        )


    }
}
