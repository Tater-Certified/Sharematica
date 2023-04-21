package com.github.tatercertified.sharematica.client.litematica.mixin;

import fi.dy.masa.malilib.gui.interfaces.ISelectionListener;
import fi.dy.masa.malilib.gui.widgets.WidgetDirectoryEntry;
import fi.dy.masa.malilib.gui.widgets.WidgetFileBrowserBase;
import fi.dy.masa.malilib.gui.widgets.WidgetListBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = WidgetFileBrowserBase.class, remap = false)
public abstract class WidgetFileBrowserBaseMixin extends WidgetListBase<WidgetFileBrowserBase.DirectoryEntry, WidgetDirectoryEntry> {

    public WidgetFileBrowserBaseMixin(int x, int y, int width, int height, ISelectionListener<WidgetFileBrowserBase.DirectoryEntry> selectionListener) {
        super(x, y, width, height, selectionListener);
    }

    @Inject(method = "refreshBrowserEntries", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    private void refreshBrowserEntries(CallbackInfo ci) {
        this.listContents.add(new WidgetFileBrowserBase.DirectoryEntry(WidgetFileBrowserBase.DirectoryEntryType.DIRECTORY, null, "Sharematics", "Sharematica"));
    }


    @Override
    protected WidgetDirectoryEntry createListEntryWidget(int i, int i1, int i2, boolean b, WidgetFileBrowserBase.DirectoryEntry directoryEntry) {
        return null;
    }
}
