package system.start;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import system.core.Storage;

public class Start {

    public static void main(String[] args) throws InterruptedException, NativeHookException {
        Storage storage = new Storage();
        GlobalScreen.registerNativeHook();
        GlobalScreen.getInstance().addNativeKeyListener(storage);
        GlobalScreen.getInstance().addNativeMouseListener(storage);
        //GlobalScreen.getInstance().addNativeMouseWheelListener(storage);        
    }
}
