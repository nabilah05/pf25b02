import javax.sound.sampled.*;
import java.io.IOException;
import java.net.URL;

public class SoundEffect {
    public void playSound(String fileName) {
        try {
            URL soundURL = getClass().getClassLoader().getResource(fileName);
            if (soundURL == null) {
                System.err.println("File tidak ditemukan: " + fileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
}
