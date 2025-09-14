package logbackc;

import ch.qos.logback.classic.*;
import ch.qos.logback.classic.spi.*;
import ch.qos.logback.core.pattern.color.*;

public class CustomLevelColor extends ForegroundCompositeConverterBase<ILoggingEvent> {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        Level level = event.getLevel();

        return switch (level.toInt()) {
            case Level.ERROR_INT -> ANSIConstants.BOLD + ANSIConstants.RED_FG;
            case Level.WARN_INT -> ANSIConstants.BOLD + ANSIConstants.YELLOW_FG;
            case Level.INFO_INT -> ANSIConstants.BOLD + ANSIConstants.BLUE_FG;
            case Level.DEBUG_INT -> ANSIConstants.BOLD + ANSIConstants.CYAN_FG;
            default -> ANSIConstants.RESET;
        };
    }
    
}
