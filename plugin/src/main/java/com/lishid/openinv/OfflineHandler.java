/*
 * Copyright (C) 2011-2022 lishid. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.lishid.openinv;

import com.lishid.openinv.internal.ISpecialInventory;
import com.lishid.openinv.util.Permissions;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

record OfflineHandler(
        @NotNull BiFunction<Map<UUID, ? extends ISpecialInventory>, UUID, ISpecialInventory> fetch,
        @NotNull Consumer<@NotNull ISpecialInventory> handle) {

    static final OfflineHandler REMOVE_AND_CLOSE = new OfflineHandler(
            Map::remove,
            inventory -> OpenInv.ejectViewers(inventory, viewer -> true)
    );

    static final OfflineHandler REQUIRE_PERMISSIONS = new OfflineHandler(
            Map::get,
            inventory -> OpenInv.ejectViewers(inventory, viewer -> !Permissions.ACCESS_OFFLINE.hasPermission(viewer))
    );

}
