package net.mcbrawls.fracture.command

import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.entity.Player

class SyntaxDsl(val basePermission: String) {
    var permission: String? = null
    var permissionLevel: Int? = null

    private val args: MutableList<Argument<*>> = mutableListOf()
    val arguments: List<Argument<*>> get() = args.toList()

    private lateinit var _executor: CommandExecutor
    val executor: CommandExecutor get() = _executor

    fun require(permissionLevel: Int, permission: String? = null) {
        this.permissionLevel = permissionLevel

        permission?.let {
            this.permission = "$basePermission.$permission"
        }
    }

    fun require(permission: String? = null) {
        require(2, permission)
    }

    fun requireBase(permissionLevel: Int = 2) {
        this.permissionLevel = permissionLevel
        permission = basePermission
    }

    fun executor(executor: CommandExecutor) {
        _executor = executor
    }

    fun playerExecutor(executor: (Player, CommandContext) -> Unit) {
        _executor = CommandExecutor { sender, context ->
            if (sender !is Player) error("Must be a player")
            executor.invoke(sender, context)
        }
    }

    fun args(vararg arguments: Argument<*>) {
        args.addAll(arguments)
    }
}
