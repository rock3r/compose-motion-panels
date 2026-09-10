package dev.letstri.motionpanels.demo

import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.sebastiano.spectre.core.ComposeAutomator
import dev.sebastiano.spectre.core.RobotDriver
import dev.sebastiano.spectre.testing.ComposeAutomatorExtension
import dev.sebastiano.spectre.testing.runSpectreTest
import java.awt.Rectangle
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.File
import java.util.concurrent.atomic.AtomicReference
import javax.imageio.ImageIO
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
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

        automator.swipe(
            startX = bounds.x + bounds.width / 2,
            startY = bounds.y + bounds.height / 2,
            endX = bounds.x + bounds.width / 2 + 70,
            endY = bounds.y + bounds.height / 2,
            steps = 8,
            duration = 180.milliseconds,
        )
        automator.waitForVisualIdle()
        val dragged = assertNotNull(automator.findOneByTestTag("files-size")).text
        assertNotEquals(initial, dragged, "horizontal dragging must update the panel size")

        automator.click(separator)
        automator.pressKey(KeyEvent.VK_RIGHT)
        automator.waitForVisualIdle()
        val keyed = assertNotNull(automator.findOneByTestTag("files-size")).text
        assertNotEquals(dragged, keyed, "arrow keys must resize a focused separator")

        automator.click(assertNotNull(automator.findOneByTestTag("toggle-files")))
        automator.waitForVisualIdle()
        val collapsedSeparator = assertNotNull(automator.findOneByTestTag("files-separator"))
        assertTrue(
            collapsedSeparator.boundsOnScreen.x < bounds.x - 100,
            "the animated fold must move the seam to the group's start edge",
        )
        assertNotNull(automator.findOneByText("Open files"), "collapse state must update the UI")

        automator.click(assertNotNull(automator.findOneByTestTag("toggle-files")))
        automator.waitForVisualIdle()
        assertNotNull(automator.findOneByText("Files"), "panel must reopen after its fold")
    }
}

private class DemoWindow {
    private val windowRef = AtomicReference<ComposeWindow?>()
    @Volatile private var exit: (() -> Unit)? = null
    private lateinit var thread: Thread

    fun start() {
        thread = Thread {
            application(exitProcessOnExit = false) {
                exit = ::exitApplication
                Window(onCloseRequest = ::exitApplication, title = "motion-panels-spectre") {
                    windowRef.compareAndSet(null, window)
                    MotionPanelsDemo()
                }
            }
        }.apply { isDaemon = true; start() }
    }

    fun awaitWindow(): ComposeWindow {
        repeat(400) {
            windowRef.get()?.let { return it }
            Thread.sleep(50)
        }
        error("Compose window did not start")
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
