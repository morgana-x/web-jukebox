package net.morgana.signedjukebox.mixin;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;


// UNUSED - update mixins.json if decide to implement
@Mixin(net.minecraft.client.gui.screen.ingame.SignEditScreen.class)
public class SignEditScreenMixin extends Screen {


    @Inject(at = @At("TAIL"), method = "init")
    public void init(CallbackInfo ci) {
        this.buttons.add(
                new ButtonWidget(1,
                        (this.width / 2) - 100, (this.height / 4) + 120,
                        "Paste Link"));

    }

    @Inject(at = @At("TAIL"), method = "buttonClicked")
    public void buttonClicked(ButtonWidget btn, CallbackInfo ci)
    {
        if (!btn.active || btn.id != 1)
            return;

        String clipboard = getClipboard();

    }


}
