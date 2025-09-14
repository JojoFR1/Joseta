package logbackc;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.pattern.color.*;

public class CustomMessageColor extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();

        if (level.toInt() == Level.ERROR_INT) return ANSIConstants.RED_FG;
        return ANSIConstants.RESET;
    }
    
}
