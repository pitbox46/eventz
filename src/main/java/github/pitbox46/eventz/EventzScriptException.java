package github.pitbox46.eventz;

public class EventzScriptException extends Exception {
    public EventzScriptException(String string) {
        super(string);
    }

    public EventzScriptException(String string, Throwable throwable) {
        super(string, throwable);
    }
}
