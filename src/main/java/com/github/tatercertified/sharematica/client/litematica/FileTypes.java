package com.github.tatercertified.sharematica.client.litematica;

import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;

import java.io.File;

public enum FileTypes {
    INVALID,
    DIRECTORY,
    SHAREMATICA,
    FILE;

    FileTypes() {
    }

    public static FileTypes fromFile(File file) {
        if (!file.exists()) {
            return INVALID;
        } else {
            return file.isDirectory() ? DIRECTORY : FILE;
        }
    }
}