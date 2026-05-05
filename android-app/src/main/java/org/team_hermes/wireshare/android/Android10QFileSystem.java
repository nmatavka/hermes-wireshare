/*
 *     Created by Angel Leon (@gubatron)
 *     Copyright (c) 2011-2026, FrostWire(R). All rights reserved.
 * 
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.team_hermes.wireshare.android;

import android.app.Application;
import android.os.Build;

import androidx.annotation.RequiresApi;

import org.team_hermes.wireshare.android.gui.Librarian;
import org.team_hermes.wireshare.android.util.SystemUtils;
import org.team_hermes.wireshare.platform.DefaultFileSystem;

import java.io.File;

public class Android10QFileSystem extends DefaultFileSystem {
    private final Application app;

    public Android10QFileSystem(Application app) {
        this.app = app;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    public boolean copy(File src, File dest) {
        return Librarian.mediaStoreSaveToDownloads(src, dest, SystemUtils.hasAndroid10());
    }
}
