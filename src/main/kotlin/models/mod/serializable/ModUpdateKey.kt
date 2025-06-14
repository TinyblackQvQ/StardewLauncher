/**
 * Copyright (C) 2025 Miluko
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */


package models.mod.serializable

import kotlinx.serialization.Serializable
import models.mod.serializer.UpdateKeySerializer
import models.mod.enums.ModUpdateKeyType
import models.mod.exception.InvalidUpdateKeyPatternException
import models.mod.exception.UnknownUpdateKeyTypeException
import models.mod.observable.ObservableModUpdateKey

/**
 * ### IMPORTANT
 * TODO: Now update key doesn't support {Beta Version} & {Update subkeys}, features about them are not supported either
 *
 * TODO: [ModUpdateKey.UpdateManifest] is not supported for now, but it's scheduled to be supported in future
 *
 * SEE [APIs/Update_checks](https://stardewvalleywiki.com/Modding:Modder_Guide/APIs/Update_checks) to read more
 * */
@Serializable(with = UpdateKeySerializer::class)
data class ModUpdateKey(
    val type: ModUpdateKeyType,
    val value: String
) {
    companion object {
        fun parse(updateKeyString: String): ModUpdateKey {
            val split = updateKeyString.split(":")
            if (split.size != 2)
                throw InvalidUpdateKeyPatternException("\"$updateKeyString\" is not a valid update key.")
            val type = ModUpdateKeyType.fromString(split[0])
            if (type == null) throw UnknownUpdateKeyTypeException("\"${split[0]}\" is not a known update key type.")
            // TODO: Now [value] is not checked, it may should be checked, i guess
            return ModUpdateKey(type, split[1])
        }
    }

    override fun toString(): String {
        return "${type.name}:$value"
    }

    fun toObservable(): ObservableModUpdateKey {
        return ObservableModUpdateKey(type, value)
    }
}