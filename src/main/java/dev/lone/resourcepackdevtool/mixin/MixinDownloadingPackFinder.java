package dev.lone.resourcepackdevtool.mixin;

import dev.lone.resourcepackdevtool.Configuration;
import dev.lone.resourcepackdevtool.ResourcepackDevTool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

@Mixin(ClientBuiltinResourcePackProvider.class)
public abstract class MixinDownloadingPackFinder
{
    @Shadow
    @Final
    private ReentrantLock lock;

    @Shadow
    @Nullable
    private CompletableFuture<?> downloadTask;

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Nullable
    private ResourcePackProfile serverContainer;

    /**
     * @author LoneDev
     * @reason Do not remove server resource pack from memory on leave for faster re-join
     */
    @Overwrite
    public CompletableFuture<?> clear()
    {
        this.lock.lock();

        try
        {
            if (this.downloadTask != null)
                this.downloadTask.cancel(true);

            this.downloadTask = null;
        }
        finally
        {
            this.lock.unlock();
        }

        return CompletableFuture.completedFuture(null);
    }

    /**
     * @author LoneDev
     * @reason Do not remove server resource pack from memory on leave for faster re-join
     */
    @Overwrite
    public CompletableFuture<Void> loadServerPack(File packFile, ResourcePackSource packSource)
    {
        PackResourceMetadata packResourceMetadata;
        try (ZipResourcePack zipResourcePack = new ZipResourcePack(packFile))
        {
            packResourceMetadata = zipResourcePack.parseMetadata(PackResourceMetadata.READER);
        }
        catch (Throwable exc)
        {
            return Util.completeExceptionally(new IOException(String.format("Invalid resourcepack at %s", packFile), exc));
        }

        LOGGER.info("Applying server pack {}", packFile);
        ResourcePackProfile newServerPack = new ResourcePackProfile("server", true, () -> {
            return new ZipResourcePack(packFile);
        }, Text.translatable("resourcePack.server.name"), packResourceMetadata.getDescription(), ResourcePackCompatibility.from(packResourceMetadata, ResourceType.CLIENT_RESOURCES), ResourcePackProfile.InsertionPosition.TOP, false, packSource);

        if (this.serverContainer == null || !isPackCached(packFile))
        {
            this.serverContainer = newServerPack;
            Configuration.inst().cacheServerPack(packFile);
            return MinecraftClient.getInstance().reloadResourcesConcurrently();
        }
        else
        {
            ResourcepackDevTool.showLoadServerPackFromCacheToast();
        }

        return CompletableFuture.completedFuture(null);
    }

    private static boolean isPackCached(File packZip)
    {
        File lastPack = Configuration.inst().getLastPack();
        if(lastPack == null)
            return false;
        return packZip.getName().equals(lastPack.getName());
    }
}