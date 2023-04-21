package com.github.tatercertified.sharematica.client.litematica.mixin;

import fi.dy.masa.malilib.gui.interfaces.IDirectoryNavigator;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListEntryBase;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;

@Mixin(value = WidgetDirectoryEntry.class, remap = false)
public abstract class WidgetDirectoryEntryMixin extends WidgetListEntryBase<WidgetFileBrowserBase.DirectoryEntry> {

    @Shadow @Final protected WidgetFileBrowserBase.DirectoryEntry entry;

    @Shadow @Final protected IDirectoryNavigator navigator;

    public WidgetDirectoryEntryMixin(int x, int y, int width, int height, WidgetFileBrowserBase.DirectoryEntry entry, int listIndex) {
        super(x, y, width, height, entry, listIndex);
    }

    /**
     * @author QPCrummer
     * @reason Hijacked to add a new type of "fake" folder for sharematics
     */
    @Overwrite
    protected boolean onMouseClickedImpl(int mouseX, int mouseY, int mouseButton) {
        if (this.entry.getType() == WidgetFileBrowserBase.DirectoryEntryType.DIRECTORY) {
            this.navigator.switchToDirectory(new File(this.entry.getDirectory(), this.entry.getName()));
            return true;
        }
        //TODO add SHAREMATICA button and open SHAREMATICA GUI

        return super.onMouseClickedImpl(mouseX, mouseY, mouseButton);
    }


}

