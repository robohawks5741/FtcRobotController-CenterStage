package org.firstinspires.ftc.teamcode

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import org.firstinspires.ftc.teamcode.BotShared.Companion.autoIsCameraRight
import org.firstinspires.ftc.teamcode.BotShared.Companion.autoShouldParkRight
import org.firstinspires.ftc.teamcode.BotShared.Companion.autoWaitTimeMs
import kotlin.math.roundToLong

@TeleOp(name = "## Configure Autonomous")
class AutoConfigurator : LinearOpMode() {

    private var didTogglePark = false
    private var didToggleCamera = false

    override fun runOpMode() {
        waitForStart()
        while (opModeIsActive()) {
            telemetry.addLine("Delay Time: $autoWaitTimeMs ms (right trigger increase, left trigger decrease)")
            autoWaitTimeMs += ((gamepad1.right_trigger - gamepad1.left_trigger) * 500f).roundToLong()
            telemetry.addLine("Parking Side: ${ if (autoShouldParkRight) "RIGHT" else "LEFT" } (press A to toggle)")
            telemetry.addLine("Camera Side: ${ if (autoIsCameraRight) "RIGHT" else "LEFT" } (press B to toggle)")

            if (gamepad1.a) {
                if (!didTogglePark) {
                    didTogglePark = true
                    autoShouldParkRight = !autoShouldParkRight
                }
            } else didTogglePark = false

            if (gamepad1.b) {
                if (!didToggleCamera) {
                    didToggleCamera = true
                    autoIsCameraRight = !autoIsCameraRight
                }
            } else didToggleCamera = false

            telemetry.update()
        }
    }
}