package dev.lone.resourcepackdevtool;

import net.fabricmc.api.ModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.text.Text;

public class ResourcepackDevTool implements ModInitializer
{
    @Override
    public void onInitialize()
    {
        if(Configuration.inst().hasPackCached())
        {
            MinecraftClient.getInstance().getResourcePackProvider().loadServerPack(Configuration.inst().getLastPack(), ResourcePackSource.PACK_SOURCE_SERVER)
                    .thenRun(ResourcepackDevTool::showLoadServerPackFromCacheToast);
        }
    }

    public static void showLoadServerPackFromCacheToast()
    {
        CustomToast.show(MinecraftClient.getInstance().getToastManager(), Text.literal("Resourcepack"), Text.literal("Loaded server resourcepack from cache."));
    }
}