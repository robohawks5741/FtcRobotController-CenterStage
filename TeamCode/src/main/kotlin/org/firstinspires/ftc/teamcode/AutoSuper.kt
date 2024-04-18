package org.firstinspires.ftc.teamcode

import com.acmerobotics.roadrunner.Pose2d
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.hardware.DcMotor
import com.qualcomm.robotcore.hardware.DcMotorEx
import com.qualcomm.robotcore.hardware.IMU
import com.qualcomm.robotcore.hardware.Servo
import org.firstinspires.ftc.teamcode.Alliance.*
import org.firstinspires.ftc.teamcode.AllianceSide.*
import org.firstinspires.ftc.teamcode.botmodule.ModuleConfig
import org.firstinspires.ftc.teamcode.botmodule.Opticon

abstract class AutoSuper : LinearOpMode() {
    protected val intake: DcMotorEx by lazy { hardwareMap[DcMotorEx::class.java,    "intake"] }
    protected val slideR: DcMotorEx by lazy { hardwareMap[DcMotorEx::class.java,    "slideR"] }
    protected val slideL: DcMotorEx by lazy { hardwareMap[DcMotorEx::class.java,    "slideL"] }
    protected val trussL: Servo     by lazy { hardwareMap[Servo::class.java,        "trussL"] }
    protected val trussR: Servo     by lazy { hardwareMap[Servo::class.java,        "trussR"] }
    protected val armR: Servo       by lazy { hardwareMap[Servo::class.java,        "armR"] }
    protected val armL: Servo       by lazy { hardwareMap[Servo::class.java,        "armL"] }
    protected val clawR: Servo      by lazy { hardwareMap[Servo::class.java,        "clawR"] }
    protected val clawL: Servo      by lazy { hardwareMap[Servo::class.java,        "clawL"] }
    protected val drone: Servo      by lazy { hardwareMap[Servo::class.java,        "drone"] }
    protected val inlift: Servo     by lazy { hardwareMap[Servo::class.java,        "inlift"] }
    protected val imu: IMU          by lazy { hardwareMap[IMU::class.java,          "imu"] }

    protected lateinit var shared: BotShared
//    protected lateinit var opticon: Opticon

    protected lateinit var drive: MecanumDrive
    protected lateinit var autoSub: AutoSubsystem

    open val beginPose = Pose2d(0.0, 0.0, 0.0)
    protected var placementZone: SpikeMark = SpikeMark.RIGHT
    protected var liftPos = 0
    abstract val alliance: Alliance
    abstract val side: AllianceSide


    final override fun runOpMode() {
        clawR.position = 0.07
        clawL.position = 0.29
        inlift.position = 0.22

        // arm
        armR.position = 0.05
        armL.position = 0.95

        // mode settings
        slideR.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideR.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER
        slideL.mode = DcMotor.RunMode.STOP_AND_RESET_ENCODER
        slideL.mode = DcMotor.RunMode.RUN_WITHOUT_ENCODER

        autoSub = AutoSubsystem(this, when (side) {
            AUDIENCE_SIDE -> alliance == BLUE
            BACKDROP_SIDE -> alliance == RED
        })
        autoSub.setAlliance(alliance)

        shared = BotShared(this)
//        opticon = Opticon(ModuleConfig(this, shared, false))
        drive = MecanumDrive(hardwareMap, beginPose)

        /* for (i in 0..100) {
           Thread.sleep(20)
           elementSpikeMark = autoSub.elementDetection()
           telemetry.addData("getMaxDistance", autoSub.pipeline.getMaxDistance())
           if (isStopRequested){
               return
           }
        } */

        while ( !isStarted && !isStopRequested) {
            autoSub.detectElement()
            autoSub.setAlliance(alliance)
            telemetry.addData("Current Alliance Selected", alliance.name)
            placementZone = autoSub.spikeMark
            telemetry.update()
            sleep(30)
        }

        placementZone = autoSub.spikeMark

        waitForStart()
        sleep(BotShared.autoWaitTimeMs)
        runSpecialized()
        while (opModeIsActive()) {
            BotShared.wasLastOpModeAutonomous = true
            BotShared.storedPose = drive.pose
        }
    }

    abstract fun runSpecialized()
}