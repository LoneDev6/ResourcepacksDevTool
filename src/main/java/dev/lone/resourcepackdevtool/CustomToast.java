package dev.lone.resourcepackdevtool;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Environment(EnvType.CLIENT)
public class CustomToast implements Toast
{
    private static final Object DEFAULT_TYPE = new Object();

    private long displayDuration;
    private Text title;
    private List<OrderedText> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;

    public CustomToast(Text title, @Nullable Text description, long displayDuration)
    {
        this(
                title,
                getTextAsList(description),
                Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))),
                displayDuration
        );
    }

    private CustomToast(Text title, List<OrderedText> lines, int width, long displayDuration)
    {
        this.title = title;
        this.lines = lines;
        this.width = width;
        this.displayDuration = displayDuration;
    }

    private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text)
    {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    public int getWidth()
    {
        return this.width;
    }

    @Override
    public Object getType()
    {
        return DEFAULT_TYPE;
    }

    @Override
    public Visibility draw(MatrixStack matrices, ToastManager manager, long startTime)
    {
        if (this.justUpdated)
        {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        RenderSystem.setShaderTexture(0, TEXTURE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        int i = this.getWidth();
        int k;
        if (i == 160 && this.lines.size() <= 1)
        {
            manager.drawTexture(matrices, 0, 0, 0, 64, i, this.getHeight());
        }
        else
        {
            k = this.getHeight() + Math.max(0, this.lines.size() - 1) * 12;
            int m = Math.min(4, k - 28);
            this.drawPart(matrices, manager, i, 0, 0, 28);

            for (int n = 28; n < k - m; n += 10)
            {
                this.drawPart(matrices, manager, i, 16, n, Math.min(16, k - n - m));
            }

            this.drawPart(matrices, manager, i, 32 - m, k - m, m);
        }

        if (this.lines == null)
        {
            manager.getClient().textRenderer.draw(matrices, this.title, 18.0F, 12.0F, -256);
        }
        else
        {
            manager.getClient().textRenderer.draw(matrices, this.title, 18.0F, 7.0F, -256);

            for (k = 0; k < this.lines.size(); ++k)
            {
                manager.getClient().textRenderer.draw(matrices, this.lines.get(k), 18.0F, (float) (18 + k * 12), -1);
            }
        }

        return startTime - this.startTime < this.displayDuration ? Visibility.SHOW : Visibility.HIDE;
    }

    private void drawPart(MatrixStack matrices, ToastManager manager, int width, int textureV, int y, int height)
    {
        int i = textureV == 0 ? 20 : 5;
        int j = Math.min(60, width - i);
        manager.drawTexture(matrices, 0, y, 0, 64 + textureV, i, height);

        for (int k = i; k < width - j; k += 64)
        {
            manager.drawTexture(matrices, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
        }

        manager.drawTexture(matrices, width - j, y, 160 - j, 64 + textureV, j, height);
    }

    public void setContent(Text title, @Nullable Text description, long displayDuration)
    {
        this.title = title;
        this.lines = getTextAsList(description);
        this.justUpdated = true;
        this.displayDuration = displayDuration;
    }

    public static void add(ToastManager manager, Text title, @Nullable Text description, long displayDuration)
    {
        manager.add(new CustomToast(title, description, displayDuration));
    }

    public static void show(ToastManager manager, Text title, @Nullable Text description)
    {
        show(manager, title, description, 1000);
    }

    public static void show(ToastManager manager, Text title, @Nullable Text description, long displayDuration)
    {
        CustomToast toast = manager.getToast(CustomToast.class, DEFAULT_TYPE);
        if (toast == null)
            add(manager, title, description, displayDuration);
        else
            toast.setContent(title, description, displayDuration);
    }
}
