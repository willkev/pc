package system.core;

import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_9;
import static java.awt.event.KeyEvent.VK_A;
import static java.awt.event.KeyEvent.VK_BACK_SLASH;
import static java.awt.event.KeyEvent.VK_BACK_SPACE;
import static java.awt.event.KeyEvent.VK_BEGIN;
import static java.awt.event.KeyEvent.VK_CANCEL;
import static java.awt.event.KeyEvent.VK_CAPS_LOCK;
import static java.awt.event.KeyEvent.VK_CLEAR;
import static java.awt.event.KeyEvent.VK_CLOSE_BRACKET;
import static java.awt.event.KeyEvent.VK_COMMA;
import static java.awt.event.KeyEvent.VK_COMPOSE;
import static java.awt.event.KeyEvent.VK_CONTEXT_MENU;
import static java.awt.event.KeyEvent.VK_DELETE;
import static java.awt.event.KeyEvent.VK_DIVIDE;
import static java.awt.event.KeyEvent.VK_DOWN;
import static java.awt.event.KeyEvent.VK_END;
import static java.awt.event.KeyEvent.VK_ENTER;
import static java.awt.event.KeyEvent.VK_EQUALS;
import static java.awt.event.KeyEvent.VK_ESCAPE;
import static java.awt.event.KeyEvent.VK_HOME;
import static java.awt.event.KeyEvent.VK_LEFT;
import static java.awt.event.KeyEvent.VK_MINUS;
import static java.awt.event.KeyEvent.VK_MULTIPLY;
import static java.awt.event.KeyEvent.VK_NUMPAD0;
import static java.awt.event.KeyEvent.VK_NUMPAD9;
import static java.awt.event.KeyEvent.VK_NUM_LOCK;
import static java.awt.event.KeyEvent.VK_OPEN_BRACKET;
import static java.awt.event.KeyEvent.VK_PAGE_DOWN;
import static java.awt.event.KeyEvent.VK_PAGE_UP;
import static java.awt.event.KeyEvent.VK_PAUSE;
import static java.awt.event.KeyEvent.VK_PERIOD;
import static java.awt.event.KeyEvent.VK_RIGHT;
import static java.awt.event.KeyEvent.VK_SCROLL_LOCK;
import static java.awt.event.KeyEvent.VK_SEMICOLON;
import static java.awt.event.KeyEvent.VK_SLASH;
import static java.awt.event.KeyEvent.VK_SPACE;
import static java.awt.event.KeyEvent.VK_UP;
import static java.awt.event.KeyEvent.VK_Z;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

public class Storage implements NativeKeyListener, NativeMouseListener {

    private boolean showLog = true;

    public enum EventType {

        Mouse, Text, Control, None;

        public String getCode() {
            switch (this) {
                case Mouse:
                    return "M";
                case Text:
                    return "T";
                case Control:
                    return "C";
            }
            return "X";
        }

        public static EventType getEventType(String name) {
            if (name.equals("M")) {
                return Mouse;
            }
            if (name.equals("T")) {
                return Text;
            }
            if (name.equals("C")) {
                return Control;
            }
            return None;
        }
    }

    private long time = 0, lastTime = 0;
    private EventType lastEvent = EventType.None;
    private FileWriter writer;

    public Storage() throws NativeHookException {
        GlobalScreen.registerNativeHook();
        GlobalScreen.getInstance().addNativeKeyListener(this);
        GlobalScreen.getInstance().addNativeMouseListener(this);
        //GlobalScreen.getInstance().addNativeMouseWheelListener(this);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nke) {
        addKey(nke.getKeyCode());
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent nke) {
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent nke) {
    }

    @Override
    public void nativeMouseClicked(NativeMouseEvent nme) {
        addMouse(nme.getX(), nme.getY(), nme.getButton());
    }

    @Override
    public void nativeMousePressed(NativeMouseEvent nme) {
    }

    @Override
    public void nativeMouseReleased(NativeMouseEvent nme) {
    }

    private void addMouse(int x, int y, int button) {
        storeMouse(x, y, button);
        lastEvent = EventType.Mouse;
    }

    private void addKey(int keyCode) {
        if (addText(keyCode)) {
            lastEvent = EventType.Text;
            return;
        }
        switch (keyCode) {
            // Virgula é o token de separação no arquivo .dat
            case VK_COMMA:
            case VK_ENTER:
            case VK_DELETE:
            case VK_BACK_SPACE:
            case VK_CANCEL:
            case VK_CLEAR:
            case VK_COMPOSE:
            case VK_PAUSE:
            case VK_CAPS_LOCK:
            case VK_ESCAPE:
            case VK_PAGE_UP:
            case VK_PAGE_DOWN:
            case VK_END:
            case VK_HOME:
            case VK_LEFT:
            case VK_UP:
            case VK_RIGHT:
            case VK_DOWN:
            case VK_BEGIN:
            case VK_NUM_LOCK:
            case VK_SCROLL_LOCK:
                storeTextNewLine();
                lastEvent = EventType.Control;
                break;
            default:
                if (lastEvent != EventType.Text) {
                    storeTextNewLine();
                }
                lastEvent = EventType.Text;
        }
        if (keyCode == 0) {
            storeTextUndefined();
        } else {
            storeTextControl(keyCode);
            // "Scroll Lock" gera um evento de "Menu de Contexto"
            if (keyCode == VK_SCROLL_LOCK) {
                storeTextControl(VK_CONTEXT_MENU);
            }
        }
    }

    private boolean addText(int keyCode) {
        if (keyCode >= VK_0 && keyCode <= VK_9
                || keyCode >= VK_A && keyCode <= VK_Z
                || keyCode >= VK_NUMPAD0 && keyCode <= VK_NUMPAD9
                || keyCode == VK_SPACE
                || keyCode >= VK_MULTIPLY && keyCode <= VK_DIVIDE) {
            if (keyCode >= VK_NUMPAD0 && keyCode <= VK_NUMPAD9) {
                keyCode -= 48;
            }
            if (keyCode >= VK_MULTIPLY && keyCode <= VK_DIVIDE) {
                keyCode -= 64;
            }
            if (lastEvent != EventType.Text) {
                storeTextNewLine();
            }
            storeText(keyCode);
            return true;
        }
        switch (keyCode) {
            case VK_PERIOD:
            case VK_SLASH:
            case VK_SEMICOLON:
            case VK_EQUALS:
            case VK_OPEN_BRACKET:
            case VK_BACK_SLASH:
            case VK_CLOSE_BRACKET:
            case VK_MINUS:
                if (lastEvent != EventType.Text) {
                    storeTextNewLine();
                }
                storeText(keyCode);
                return true;
        }
        return false;
    }

    private void storeMouse(int x, int y, int button) {
        if (showLog) {
            System.out.format("\n[%03d,%03d] button-%d", x, y, button);
        }
        write(EventType.Mouse, x + "," + y + "," + button);
    }

    private void storeText(int keyCode) {
        if (showLog) {
            System.out.print((char) keyCode);
        }
        write(EventType.Text, keyCode);
    }

    private void storeTextControl(int keyCode) {
        if (showLog) {
            System.out.print("[" + KeyEvent.getKeyText(keyCode) + "]");
        }
        write(EventType.Control, keyCode);
    }

    private void storeTextNewLine() {
        if (showLog) {
            System.out.println();
        }
    }

    private void storeTextUndefined() {
        if (showLog) {
            System.out.print("[?]");
        }
        write(EventType.None, "[?]");
    }

    private void write(EventType eventType, int keyCode) {
        write(eventType, "" + keyCode);
    }

    private void write(EventType eventType, String text) {
        if (openFile()) {
            try {
                // type, time, [keycode | x, y, button]
                writer.write(eventType.getCode() + "," + calculateRangeTime() + "," + text + "\n");
                writer.flush();
            } catch (Exception ex) {
                System.out.println("ERROR writeFile:" + ex.getMessage());
            }
        }
    }

    private long calculateRangeTime() {
        lastTime = time;
        time = System.currentTimeMillis();
        if (lastTime == 0) {
            return 0;
        }
        return time - lastTime;
    }

    private boolean openFile() {
        if (writer != null) {
            return true;
        }
        try {
            File dir = new File(System.getProperty("user.dir", ""), "inf");
            dir.mkdir();
            writer = new FileWriter(new File(dir, System.currentTimeMillis() + ".dat"));
            return true;
        } catch (IOException ex) {
            System.out.println("ERROR openFile:" + ex.getMessage());
            try {
                writer.close();
            } catch (IOException ex2) {
            }
            writer = null;
        }
        return false;
    }
}
