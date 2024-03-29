package net.md_5.bungee.log;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jline.console.ConsoleReader;

public class BungeeLogger extends Logger
{
	private final Formatter formatter = new ConciseFormatter();
    private final LogDispatcher dispatcher = new LogDispatcher( this );

    @SuppressFBWarnings("SC_START_IN_CTOR")
    public BungeeLogger(String loggerName, String filePattern, ConsoleReader reader)
    {
        super( loggerName, null );
        setLevel( Level.ALL );

        try
        {
            FileHandler fileHandler = new FileHandler( filePattern, 1 << 24, 8, true );
            fileHandler.setFormatter( formatter );
            addHandler( fileHandler );

            ColouredWriter consoleHandler = new ColouredWriter( reader );
            consoleHandler.setLevel( Level.INFO );
            consoleHandler.setFormatter( formatter );
            addHandler( consoleHandler );
        } catch ( IOException ex )
        {
            System.err.println( "Could not register logger!" );
            ex.printStackTrace();
        }

        dispatcher.start();
    }

    @Override
    public void log(LogRecord record)
    {
        dispatcher.queue( record );
    }

    void doLog(LogRecord record)
    {
        super.log( record );
    }
}