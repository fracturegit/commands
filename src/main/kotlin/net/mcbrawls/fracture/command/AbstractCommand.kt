package net.mcbrawls.fracture.command

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandExecutor
import net.minestom.server.command.builder.condition.CommandCondition
import net.minestom.server.command.builder.suggestion.Suggestion
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

abstract class AbstractCommand(name: String, vararg aliases: String) : Command(name, *aliases) {
    open val permission: String = name

    private val additionalPermissions: MutableSet<String> = mutableSetOf()
    val allPermissions: Set<String> get() = setOf(permission) + additionalPermissions

    init {
        defaultExecutor = exceptionalExecutor { _, _ ->
            error("Command not found")
        }
    }

    fun addSyntax(factory: SyntaxDsl.() -> Unit) {
        val syntax = SyntaxDsl(permission).apply(factory)

        val perm = syntax.permission
        perm?.let(additionalPermissions::add)

        val permissionCondition = CommandCondition { sender, _ ->
            if (sender !is Player) return@CommandCondition true

            syntax.permissionLevel?.let { permissionLevel ->
                if (sender.permissionLevel >= permissionLevel) return@CommandCondition true
            }

            perm == null || checkPermission(sender, perm)
        }

        addConditionalSyntax(
            permissionCondition,
            exceptionalExecutor { sender, context ->
                if (!permissionCondition.canUse(sender, context.input)) {
                    error("You do not have permission to use this command")
                }

                syntax.executor.apply(sender, context)
            },
            *syntax.arguments.toTypedArray(),
        )
    }

    /**
     * Checks a permission when present.
     */
    open fun checkPermission(player: Player, permission: String): Boolean {
        return true
    }

    companion object {
        fun exceptionalExecutor(executor: CommandExecutor): CommandExecutor {
            return CommandExecutor { sender, context ->
                try {
                    executor.apply(sender, context)
                } catch (throwable: Throwable) {
                    sender.sendMessage(
                        Component.text(
                            throwable.message ?: throwable.javaClass.simpleName,
                            NamedTextColor.RED
                        )
                    )

                    if (throwable !is IllegalStateException) {
                        throwable.printStackTrace()
                    }
                }
            }
        }

        fun suggestInstances(instances: Collection<Instance>, suggestion: Suggestion) {
            suggest(instances, { it.uuid.toString() }, suggestion)
        }

        fun suggest(collection: Collection<String>, suggestion: Suggestion) {
            suggest(collection, { it }, suggestion)
        }

        fun <T> suggest(collection: Collection<T>, transformer: (T) -> String, suggestion: Suggestion) {
            val suggestions = collection.map(transformer).map(::SuggestionEntry)
            suggestions.forEach(suggestion::addEntry)
        }
    }

    fun interface Factory<T : AbstractCommand> {
        fun create(name: String, vararg aliases: String): T
    }
}
