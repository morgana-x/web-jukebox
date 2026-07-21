package net.morgana.signedjukebox.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.world.World;
import net.morgana.signedjukebox.SignPlayer;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystem;

import java.io.File;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {

    @Shadow
    private World world;

    @Shadow
    private Minecraft client;

    @Inject(at = @At("HEAD"), method = "playStreaming",  cancellable = true)
    private void playStreaming(String stream, int x, int y, int z, CallbackInfo ci) {
        System.out.println("Custom music disc event!!! " + stream);

        if (stream == null)
        {
            this.client.soundManager.playStreaming(null, x, y, z, 1.0f, 1.0f);
            ci.cancel();
            return;
        }

        if (stream.startsWith("music://"))
        {
            System.out.println("Custom record overlay");
            this.client.inGameHud.setRecordPlayingOverlay( new File(stream.replace("music://","")).getName());
            this.client.soundManager.playStreaming(stream, x, y, z, 1.0f, 1.0f);
            ci.cancel();
            return;
        }

        // Do custom music logic, if valid sign url attached to jukebox cancel original

        if (SignPlayer.DoCustomJukebox(this.world, x, y, z))
            ci.cancel();

    }
}
