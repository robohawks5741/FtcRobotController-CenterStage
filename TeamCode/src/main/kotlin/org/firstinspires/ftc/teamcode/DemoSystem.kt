package org.firstinspires.ftc.teamcode

/*
 * The Clear BSD License
 *
 * Copyright (c) 2023 RoboHawks 5741
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted (subject to the limitations in the disclaimer
 * below) provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *
 *      * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *
 *      * Neither the name of the copyright holder nor the names of its
 *      contributors may be used to endorse or promote products derived from this
 *      software without specific prior written permission.
 *
 * NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY
 * THIS LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND
 * CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

import android.util.Base64
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonNull
import com.qualcomm.robotcore.eventloop.opmode.Autonomous
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode
import com.qualcomm.robotcore.eventloop.opmode.OpMode
import com.qualcomm.robotcore.eventloop.opmode.TeleOp
import com.qualcomm.robotcore.hardware.Gamepad
import org.firstinspires.ftc.teamcode.*
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.lang.reflect.Method
import kotlin.concurrent.thread
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

/**
 * # RoboHawks 5741 Demo System v2
 * Created in a few hours before a scrimmage. Allows any team with a functioning TeleOp to use that
 * same code in Autonomous by recording their gameplay (like demos from Quake and Source).
 *
 * It's a bit clunky, you can "reprogram" our autonomous without having to recompile code.
 *
 *
 * ## How it Works:
 * Using reflection and a bit of other trickery, the OpModes in this file "pretend" to be the FTC
 * SDK. When the Demo OpModes run, they pass what the FTC SDK passes to them to an
 * OpMode that they instantiated (sort of like an emulator).
 * When recording, the OpMode will intercept the current state of the gamepads and record it.
 * After the OpMode stops, it will write that data to a file, stored as JSON and Base64.
 * During playback, the OpMode will ignore gamepad input, and will send the previously recorded
 * input to the OpMode it is "emulating," thus recreating what was previously recorded.
 * There is no error correction in the demo system. The robot does not try to retrace its steps,
 * it simply repeats the inputs that you, the human player, gave it when it was running.
 * As such, we HIGHLY recommend programming your autonomous manually, as you have fine grained
 * control over the robot's behavior.
 * The DemoSystem has no knowledge of anything your robot does, but YOU do.
 * Maybe you could even extend the DemoSystem to meet your more specific needs?
 *
 *
 * ## How to Customize the DemoSystem (aka. Going Forward)
 * We highly recommend a system for recording and playing back multiple takes;
 * as-is, **the DemoSystem only stores one recording,** and there is no "undo."
 * The filenames of the currently active demos are stored as global variables
 * ([inputFileName] and [outputFileName]), and you could create a way to cycle them out.
 * Demos are persistent, and can only be fully removed or moved around by using ADB/Android Studio.
 * If you want to know where they're being stored, open LogCat before you run a DemoSystem OpMode.
 *
 *
 * ## Setup Requirements:
 * - A project with Kotlin support (see [Using the Kotlin Programming Language - FIRST Tech Challenge](https://ftc-docs.firstinspires.org/en/latest/programming_resources/shared/installing_kotlin/Installing-Kotlin.html))
 * - Kotlin's reflection library (org.jetbrains.kotlin:kotlin-reflect)
 * - An OpMode controlled with gamepad input (which may be a LinearOpMode)
 */
@Suppress("unused")
object DemoSystem {
    /** Replace this with your own Driver Control. */
    var playbackOpmode: KClass<out OpMode> = ClayJanuaryDriverControl::class
    val TICK_RATE: Double = 60.0
    val DEMO_DIRECTORY: String = "demos"
    var outputFileName: String = "0.replay"
    var inputFileName: String = "0.replay"

    var frames1: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())
    var frames2: ArrayList<ByteArray?> = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())

    fun ByteArray.toBase64(): String = String(Base64.encode(this, Base64.DEFAULT or Base64.NO_WRAP))

    // TODO: port to LinearOpMode for more control and to drop less frames
    @Autonomous(name = "Play Recorded Demo", group = "DemoSystem")
    open class DemoPlayback : OpMode() {

        private var timeOffset: Double = 0.0
        private val emulatedOpMode: OpMode = playbackOpmode.createInstance()
        private var opThread: Thread? = null

        final override fun init() {
            val context = hardwareMap.appContext
            val dir = File(context.filesDir, DEMO_DIRECTORY)
            if (!dir.exists()) {
                dir.mkdir()
            }
            val file = File(dir, inputFileName)
            val fileReader = FileReader(file)
            val reader = BufferedReader(fileReader)
            val lines = StringBuilder(file.length().toInt())
            while (true) {
                val line = reader.readLine()
                if (line == null) break; else lines.append(line)
            }
            val gson = Gson()
            val jsonAllFrames: Array<Array<String?>> = gson.fromJson(lines.toString(), Array<Array<String?>>::class.java)

//            var totalSize = 0
            val jsonFrames1 = jsonAllFrames[0]
            val jsonFrames2 = jsonAllFrames[1]
            for ((i, e) in jsonFrames1.withIndex()) frames1[i] = if (e is String) Base64.decode(e, Base64.DEFAULT or Base64.NO_WRAP) else null
            for ((i, e) in jsonFrames2.withIndex()) frames2[i] = if (e is String) Base64.decode(e, Base64.DEFAULT or Base64.NO_WRAP) else null

            println()

            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
//            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.time = this.time

//            try {
//
//                hackedInternalRunOpMode             = emulatedOpMode::class.java.getMethod("internalRunOpMode")
//                hackedInternalOnStart               = emulatedOpMode::class.java.getMethod("internalOnStart")
//                hackedInternalOnEventLoopIteration  = emulatedOpMode::class.java.getMethod("internalOnEventLoopIteration")
//                hackedInternalOnStopRequested       = emulatedOpMode::class.java.getMethod("internalOnStopRequested")
//                // we don't need newGamepadDataAvailable because we do that manually
//            } catch (e: Exception) {
//                telemetry.addLine("error getting \"hacked\" method: ${e.localizedMessage}")
//            }

            if (emulatedOpMode is LinearOpMode) {
                opThread = thread(
                    start = true,
                    isDaemon = false,
                    contextClassLoader = null,
                    name = "Addie's Emulated OpMode Thread",
                    priority = -1
                ) {
                    emulatedOpMode.runOpMode()
                    // TODO: this is not how this actually works. the thread may end up being killed abruptly
                    opThread!!.interrupt()
                    emulatedOpMode.requestOpModeStop()
                }
            }
            emulatedOpMode.init()
        }

        final override fun start() {
            this.timeOffset = this.time
            emulatedOpMode.time = this.time - timeOffset

            if (emulatedOpMode is LinearOpMode) opThread!!.interrupt() else emulatedOpMode.start()
        }

        final override fun loop() {
            emulatedOpMode.time = this.time - timeOffset

            val index = floor((this.time - timeOffset) * TICK_RATE).toInt()

            if (index < frames1.size && frames1[index] != null) {
                emulatedOpMode.gamepad1.fromByteArray(frames1[index])
                emulatedOpMode.gamepad2.fromByteArray(frames2[index])
                telemetry.addLine("frame #${index}")
                Log.i("DemoSystem", "playing frame #$index")
            } else {
                Log.i("DemoSystem", "skipping frame read #$index (max frames = ${frames1.size}, frame@index = ${frames1[index]})")
            }

            if (emulatedOpMode !is LinearOpMode) emulatedOpMode.loop()
        }

        final override fun stop() {
            if (emulatedOpMode !is LinearOpMode) emulatedOpMode.requestOpModeStop()
            emulatedOpMode.stop()

            Thread.sleep(50)
            opThread?.interrupt()
        }
    }


    // TODO: port to LinearOpMode for more control and to drop less frames
    @TeleOp(name = "Record Demo", group = "DemoSystem")
    class DemoRecorder : OpMode() {

        private var timeOffset: Double = 0.0
        private val emulatedOpMode: OpMode = playbackOpmode.createInstance() as OpMode
        private var opThread: Thread? = null

        override fun init() {
            frames1 = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())
            frames2 = arrayOfNulls<ByteArray?>(ceil(TICK_RATE * 30.0).toInt()).toCollection(ArrayList())

            emulatedOpMode.gamepad1 = Gamepad()
            emulatedOpMode.gamepad2 = Gamepad()
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            frames1[0] = emulatedOpMode.gamepad1.toByteArray()
            frames2[0] = emulatedOpMode.gamepad2.toByteArray()

            emulatedOpMode.telemetry = this.telemetry
            emulatedOpMode.hardwareMap = this.hardwareMap
//            emulatedOpMode.internalOpModeServices = this.internalOpModeServices
            emulatedOpMode.time = this.time


            if (emulatedOpMode is LinearOpMode) {
                opThread = thread(
                    start = true,
                    isDaemon = false,
                    contextClassLoader = null,
                    name = "Addie's Emulated OpMode Thread",
                    priority = -1
                ) {
                    emulatedOpMode.runOpMode()
                    // TODO: this is not how this actually works. the thread may end up being killed abruptly
                    opThread!!.interrupt()
                    emulatedOpMode.requestOpModeStop()
                }
            }
            emulatedOpMode.init()
        }

        override fun start() {
            this.timeOffset = this.time
            emulatedOpMode.time = this.time - timeOffset
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)
            Log.i("DemoSystem", "Started recording.")

            if (emulatedOpMode is LinearOpMode) opThread!!.interrupt() else emulatedOpMode.start()
        }

        override fun loop() {
            emulatedOpMode.time = this.time - timeOffset
            emulatedOpMode.gamepad1.copy(this.gamepad1)
            emulatedOpMode.gamepad2.copy(this.gamepad2)

            val index = floor((this.time - timeOffset) * TICK_RATE).toInt()

            if (index < frames1.size && frames1[index] == null) {
                frames1[index] = emulatedOpMode.gamepad1.toByteArray()
                frames2[index] = emulatedOpMode.gamepad2.toByteArray()
                Log.i("DemoSystem", "recorded frame #$index")
            } else {
                Log.i("DemoSystem", "skipping frame write #$index (max frames = ${frames1.size}, frame@index = ${frames1[index]})")
            }

            if (emulatedOpMode !is LinearOpMode) emulatedOpMode.loop()
        }

        override fun stop() {
            if (emulatedOpMode !is LinearOpMode) emulatedOpMode.requestOpModeStop()
            emulatedOpMode.stop()

            Thread.sleep(50)
            opThread?.interrupt()

            Log.i("DemoSystem", "Demo recorded, frames:")
            for ((i, e) in frames1.withIndex()) {
                if (e == null) continue
                Log.i("DemoSystem", "#$i: $e")
            }
            Log.i("DemoSystem", "No more frames.")

            val context = hardwareMap.appContext
            val dir = File(context.filesDir, DEMO_DIRECTORY)
            if (!dir.exists()) dir.mkdir()
            val file = File(dir, outputFileName)
            val jsonAllFrames = JsonArray()
            val jsonFrames1 = JsonArray()
            val jsonFrames2 = JsonArray()
            val writer = FileWriter(file)
//            var totalSize = 0
            for (e in frames1) if (e == null) jsonFrames1.add(JsonNull.INSTANCE); else jsonFrames1.add(e.toBase64())
            for (e in frames2) if (e == null) jsonFrames2.add(JsonNull.INSTANCE); else jsonFrames2.add(e.toBase64())
            jsonAllFrames.add(jsonFrames1)
            jsonAllFrames.add(jsonFrames2)
            writer.write(jsonAllFrames.toString())
            writer.close()
            Log.i("DemoSystem", "Wrote demo to ${file.absolutePath}")
        }
    }
}
