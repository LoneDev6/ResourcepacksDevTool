package dev.lone.resourcepackdevtool;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.Nullable;

import java.io.File;

public class Configuration
{
    //<editor-fold desc="Singleton">
    static Configuration instance;

    static
    {
        inst();
    }

    public static Configuration inst()
    {
        if(instance == null)
            instance = new Configuration();
        return instance;
    }
    //</editor-fold>

    Gson gson;
    final File configFile;
    JsonObject json;

    @Nullable
    private File lastPack = null;

    Configuration()
    {
        gson = new Gson().newBuilder().setPrettyPrinting().create();

        configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), "resourcepacks-dev-tool.json");
        try
        {
            if (configFile.exists())
            {
                json = gson.fromJson(FileUtils.readFileToString(configFile, Charsets.UTF_8), JsonObject.class).getAsJsonObject();
                File packFile = new File(
                        new File(FabricLoader.getInstance().getGameDir().toFile(), "server-resource-packs"),
                        json.get("last_server_pack").getAsString()
                );

                if (packFile.exists())
                {
                    lastPack = packFile;
                }
                else
                {
                    cacheServerPack(null);
                }
            }
            else
            {
                json = new JsonObject();

                cacheServerPack(null);
            }
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    @Nullable
    public File getLastPack()
    {
        return lastPack;
    }

    public void cacheServerPack(@Nullable File packFile)
    {
        lastPack = packFile;

        if(packFile == null)
            json.remove("last_server_pack");
        else
            json.addProperty("last_server_pack", packFile.getName());

        try
        {
            FileUtils.writeStringToFile(configFile, json.toString(), Charsets.UTF_8);
        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }

    public boolean hasPackCached()
    {
        return lastPack != null;
    }
}