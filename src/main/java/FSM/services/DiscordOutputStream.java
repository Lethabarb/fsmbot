package FSM.services;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DiscordOutputStream extends OutputStream {
    private StringBuilder out = new StringBuilder(2000);
    private DiscordBot bot = DiscordBot.getInstance();

    @Override
    public void write(int b) throws IOException {
        int[] bytes = {b};
        try {
            out.append(new String(bytes, 0, bytes.length));
            
        } catch (Exception e) {
            bot.sendLethabarbMessage(out.toString());
            out = new StringBuilder(2000);
        }
    }


    
}
