package net.morgana.signedjukebox.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.nbt.NbtCompound;
import net.morgana.signedjukebox.SignUtil;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;


@Mixin(net.minecraft.client.gui.screen.ingame.SignEditScreen.class)
public class SignEditScreenMixin extends Screen {

    @Shadow
    @Mutable
    private SignBlockEntity sign;

    @Shadow
    private int currentRow;


    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        this.buttons.add(
                new ButtonWidget(809,
                        (this.width / 2) - 100, (this.height / 4) + 140,
                        "Paste Audio Link"));

    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    public void buttonClicked(ButtonWidget btn, CallbackInfo ci)
    {
        if (!btn.active || btn.id != 809)
            return;

        String clipboard = getClipboard();

        if (clipboard == null)
            return;

        if (!clipboard.contains("://") && !clipboard.contains("www.") && !clipboard.contains(".com") && !clipboard.contains(".org"))
            return;

        String previousText = SignUtil.getMultiSignText(minecraft.world, sign.x, sign.y, sign.z, true);
        if (previousText == null)
            previousText = "";

        if (!previousText.isEmpty() && !clipboard.startsWith(previousText))
            return;

        String remaining = clipboard.substring(previousText.length());
        remaining = remaining.substring(0, Math.min(60, remaining.length()));

        System.out.println("Clipboard: " + clipboard);
        System.out.println("Previous text: " + previousText);
        System.out.println("Remaining text: " + remaining);

        for (int i = 0; i < 4; i++)
        {
            currentRow = i;

            int sI = i*15;
            if (sI >= remaining.length())
                break;

            int eI = Math.min(remaining.length(), sI + 15);

            this.sign.texts[i] = remaining.substring(sI, eI);

            System.out.println(sign.texts[i]);
        }

        // Thanks MojangFixStationApi...
        if (FabricLoader.getInstance().isModLoaded("mojangfixstationapi")) {

            NbtCompound nbt = new NbtCompound();

            String[] copy = sign.texts.clone();

            this.sign.writeNbt(nbt);
            nbt.putString("Text1", copy[0]);
            nbt.putString("Text2", copy[1]);
            nbt.putString("Text3", copy[2]);
            nbt.putString("Text4", copy[3]);
            this.sign.readNbt(nbt);
        }

        this.sign.markDirty();
        this.minecraft.setScreen(null);

    }


}
