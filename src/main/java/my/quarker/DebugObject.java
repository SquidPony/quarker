package my.quarker;
public class DebugObject extends BaseObject {
    public static final DebugObject DEFAULT = new DebugObject();

    public DebugObject() {
        super("Something wrong has happened!", (char) 0x03A6, false);
    }    
}
