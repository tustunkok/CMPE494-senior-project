package com.apoapsis.core;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PlayClip implements LineListener {
	private Clip clip;
	private boolean playing = false;

	public PlayClip(String clip) {
		loadClip(clip);
	}

	@Override
	public void update(LineEvent event) {
		if (event.getType() == LineEvent.Type.STOP) {
			playing = false;
			clip.stop();
			event.getLine().close();
		}
	}

	private void loadClip(String clipDir) {
		try {
			AudioInputStream stream = AudioSystem.getAudioInputStream(new File(clipDir));
			AudioFormat format = stream.getFormat();
			
			if (format.getEncoding() == AudioFormat.Encoding.ULAW || format.getEncoding() == AudioFormat.Encoding.ALAW) {
				AudioFormat newFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, format.getSampleRate(), format.getSampleSizeInBits() * 2, format.getChannels(), format.getFrameSize() * 2, format.getFrameRate(), true);
				stream = AudioSystem.getAudioInputStream(newFormat, stream);
				System.out.println("Converted audio format: " + newFormat);
				format = newFormat;
			}
			
			DataLine.Info info = new DataLine.Info(Clip.class, format);
			
			if (!AudioSystem.isLineSupported(info)) {
				System.out.println("Unsupported clip file: " + clipDir);
				System.exit(0);
			}
			
			clip = (Clip) AudioSystem.getLine(info);
			clip.addLineListener(this);
			clip.open(stream);
			stream.close();
			
			double duration = clip.getMicrosecondLength() / 1000000.0;
			System.out.println("Duration: " + duration + " secs");
		} catch (UnsupportedAudioFileException | LineUnavailableException | IOException ex) {
			ex.printStackTrace();
			System.exit(0);
		}
	}

	public void play() {
		if (clip != null) {
			clip.start();
			playing = true;
		}
	}
	
	public boolean isPlaying() {
		return playing;
	}
}