package com.junkfood.seal.database

import kotlinx.serialization.Serializable

@Serializable
data class Backup(val templates: List<CommandTemplate>, val shortcuts: List<OptionShortcut>)