package dev.letstri.motionpanels.demo

import androidx.compose.runtime.Composable
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import dev.sebastiano.spectre.core.ComposeAutomator
import dev.sebastiano.spectre.core.RobotDriver
import dev.sebastiano.spectre.recording.AutoRecorder
import dev.sebastiano.spectre.recording.RecordingOptions
import dev.sebastiano.spectre.recording.screencapturekit.asTitledWindow
import dev.sebastiano.spectre.testing.ComposeAutomatorExtension
import dev.sebastiano.spectre.testing.runSpectreTest
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.RegisterExtension

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MotionPanelsSpectreTest {
    private val window = DemoWindow()

    @JvmField
    @RegisterExtension
    val automatorExtension = ComposeAutomatorExtension(
        factory = {
            ComposeAutomator.inProcess(
                robotDriver = RobotDriver.synthetic(rootWindow = window.awaitWindow()),
            )
        },
    )

    @BeforeAll fun start() = window.start()
    @AfterAll fun stop() = window.stop()

    @Test
    fun `drag keyboard and animated collapse work in a live desktop window`(): Unit = runSpectreTest {
        val automator = automatorExtension.automator
        val separator = automator.waitForNode(tag = "files-separator", timeout = 20.seconds)
        val initial = assertNotNull(automator.findOneByTestTag("files-size")).text
        val bounds = separator.boundsOnScreen
        window.capture(File("build/reports/spectre/demo.png"))
        val recording = Path.of("build/reports/spectre/demo.mp4")
        Files.createDirectories(recording.parent)
        Files.deleteIfExists(recording)
        val recordingHandle = AutoRecorder().startWindow(
            window = window.awaitWindow().asTitledWindow(),
            output = recording,
            options = RecordingOptions(),
        )

        try {
            delay(700)
            automator.swipe(
                startX = bounds.x + bounds.width / 2,
                startY = bounds.y + bounds.height / 2,
                endX = bounds.x + bounds.width / 2 + 70,
                endY = bounds.y + bounds.height / 2,
                steps = 12,
                duration = 500.milliseconds,
            )
            automator.waitForVisualIdle()
            delay(500)
            val dragged = assertNotNull(automator.findOneByTestTag("files-size")).text
            assertNotEquals(initial, dragged, "horizontal dragging must update the panel size")

            automator.click(separator)
            automator.pressKey(KeyEvent.VK_RIGHT)
            automator.waitForVisualIdle()
            delay(500)
            val keyed = assertNotNull(automator.findOneByTestTag("files-size")).text
            assertNotEquals(dragged, keyed, "arrow keys must resize a focused separator")

            val terminalSeparator = assertNotNull(automator.findOneByTestTag("terminal-separator"))
            val initialTerminal = assertNotNull(automator.findOneByTestTag("terminal-size")).text
            val terminalBounds = terminalSeparator.boundsOnScreen
            automator.swipe(
                startX = terminalBounds.x + terminalBounds.width / 2,
                startY = terminalBounds.y + terminalBounds.height / 2,
                endX = terminalBounds.x + terminalBounds.width / 2,
                endY = terminalBounds.y - 90,
                steps = 12,
                duration = 700.milliseconds,
            )
            automator.waitForVisualIdle()
            delay(700)
            val draggedTerminal = assertNotNull(automator.findOneByTestTag("terminal-size")).text
            assertNotEquals(initialTerminal, draggedTerminal, "vertical dragging must update the panel size")

            automator.click(assertNotNull(automator.findOneByTestTag("toggle-files")))
            automator.waitForVisualIdle()
            delay(700)
            val collapsedSeparator = assertNotNull(automator.findOneByTestTag("files-separator"))
            assertTrue(
                collapsedSeparator.boundsOnScreen.x < bounds.x - 100,
                "the animated fold must move the seam to the group's start edge",
            )
            assertNotNull(automator.findOneByText("Open files"), "collapse state must update the UI")

            automator.click(assertNotNull(automator.findOneByTestTag("toggle-files")))
            automator.waitForVisualIdle()
            delay(900)
            assertNotNull(automator.findOneByText("Files"), "panel must reopen after its fold")
        } finally {
            recordingHandle.stop()
        }
        assertTrue(Files.size(recording) > 16 * 1024, "expected a non-empty demo recording")
    }
}

internal class DemoWindow(
    private val title: String = "motion-panels-spectre",
    private val content: @Composable () -> Unit = { MotionPanelsDemo() },
) {
    private val windowRef = AtomicReference<ComposeWindow?>()
    private val failureRef = AtomicReference<Throwable?>()
    @Volatile private var exit: (() -> Unit)? = null
    private lateinit var thread: Thread

    fun start() {
        thread = Thread {
            try {
                application(exitProcessOnExit = false) {
                    exit = ::exitApplication
                    Window(
                        onCloseRequest = ::exitApplication,
                        title = title,
                        state = rememberWindowState(width = 800.dp, height = 600.dp),
                    ) {
                        windowRef.compareAndSet(null, window)
                        content()
                    }
                }
            } catch (failure: Throwable) {
                failureRef.set(failure)
            }
        }.apply { isDaemon = true; start() }
    }

    fun awaitWindow(): ComposeWindow {
        repeat(400) {
            failureRef.get()?.let { throw IllegalStateException("Compose window failed to start", it) }
            windowRef.get()?.let { return it }
            Thread.sleep(50)
        }
        error("Compose window did not start; thread=${thread.state}\n${thread.stackTrace.joinToString("\n")}")
    }

    fun capture(destination: File) {
        val composeWindow = awaitWindow()
        destination.parentFile.mkdirs()
        ImageIO.write(Robot().createScreenCapture(Rectangle(composeWindow.locationOnScreen, composeWindow.size)), "png", destination)
    }

    fun stop() {
        exit?.invoke()
        thread.join(10_000)
        check(!thread.isAlive) { "Compose test window did not stop" }
    }
}
