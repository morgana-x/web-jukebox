package net.morgana.signedjukebox.mixin;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundEntry;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundSystem;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;

@Mixin(net.minecraft.client.sound.SoundManager.class)
public class SoundManagerMixin {

	@Shadow
	private static SoundSystem soundSystem;
	@Shadow private GameOptions gameOptions;
	@Shadow
	private static boolean started;

	@Inject(at = @At("HEAD"), method = "playStreaming", cancellable = true)
    private void playStreaming(String id, float x, float y, float z, float volume, float pitch, CallbackInfo ci) throws MalformedURLException {
		if (!started || this.gameOptions.soundVolume == 0.0f) {
			return;
		}

		if (soundSystem.playing("streaming")) {
			soundSystem.stop("streaming");
		}

		if (id == null || !id.startsWith("music://"))
			return;


		id = id.replace("music://", "");

		System.out.println("Custom music!" + id);

		if (volume <= 0.0f)
			return;

		var fileURL = new  File(id).toURI().toURL();

		Sound sound = new Sound(fileURL.getFile(), fileURL); // new File(new File(id).getParent()).toURI().toURL());

		if (soundSystem.playing("BgMusic")) {
			soundSystem.stop("BgMusic");
		}

		soundSystem.newStreamingSource(true, "streaming", sound.soundFile, sound.id, false, x, y, z, 2, 16.0f * 4.0f);
		soundSystem.setVolume("streaming", 0.5f * this.gameOptions.soundVolume);
		soundSystem.play("streaming");

		ci.cancel();
	}
}
