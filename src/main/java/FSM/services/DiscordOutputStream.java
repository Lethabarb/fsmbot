package FSM.services;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class DiscordOutputStream extends PrintStream {

    public DiscordOutputStream() {
        super(new ByteArrayOutputStream());
    }
    public DiscordOutputStream(OutputStream out) {
        super(out);
    }

    private StringBuilder out = new StringBuilder(2000);
    private DiscordBot bot = DiscordBot.getInstance();

    @Override
    public void println(String x) {
        bot.sendLethabarbMessage(x);
    }

    // public static void main(String[] args) {
    //     StringBuilder out = new StringBuilder(2000);
    //     while (true) {
    //         out.append("x");
    //     }
    // }

}
