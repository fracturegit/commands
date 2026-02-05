package net.mcbrawls.fracture.command

import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandManager
import net.minestom.server.command.ConsoleSender
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.LinkedBlockingQueue

class ConsoleCommandHandler(private val commandManager: CommandManager) {
    private val commandQueue = LinkedBlockingQueue<String>()
    private var isRunning = false

    fun start() {
        if (isRunning) return
        isRunning = true

        Thread {
            val reader = System.`in`.bufferedReader()
            while (isRunning) {
                try {
                    val line = reader.readLine() ?: continue
                    if (line.isEmpty()) continue
                    commandQueue.offer(line)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                // Process commands on the server thread
                MinecraftServer.getSchedulerManager().scheduleEndOfTick {
                    while (commandQueue.isNotEmpty()) {
                        val command = commandQueue.poll()
                        if (command != null) {
                            executeCommand(command)
                        }
                    }
                }
            }
        }.apply {
            isDaemon = true
            name = "Minestom-Console-Reader"
            start()
        }
    }

    fun stop() {
        isRunning = false
    }

    private fun executeCommand(input: String) {
        runCatching {
            commandManager.execute(ConsoleSender(), input)
        }.onFailure { throwable ->
            logger.error("Error executing console command", throwable)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(ConsoleCommandHandler::class.java)
    }
}
