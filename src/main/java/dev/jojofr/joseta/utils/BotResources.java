package dev.jojofr.joseta.utils;

import dev.jojofr.joseta.JosetaBot;
import net.dv8tion.jda.api.entities.emoji.Emoji;

public class BotResources {
    public static final Emoji CHECK_EMOJI, CROSS_EMOJI;
    
    static {
        boolean debug = JosetaBot.debug;
        
        //                                                    Debug Emoji ID         Production Emoji ID
        CHECK_EMOJI = Emoji.fromCustom("yes", debug ? 1459377029328801832L : 1451286173791031337L, false);
        CROSS_EMOJI = Emoji.fromCustom("no", debug ? 1459377027747680266L : 1451286184817987719L, false);
    }
}
