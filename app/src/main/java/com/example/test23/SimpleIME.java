package com.example.test23;

import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimpleIME extends InputMethodService
        implements KeyboardView.OnKeyboardActionListener {

    private KeyboardView kv;
    private String current;
    private List<CharSequence> labelToIgnore = Arrays.asList("SPACE", "abc", "ABC", "123", "--");
    private Map<String, Keyboard> keyboards = new HashMap<>();
    private Map<String, Map<CharSequence, CharSequence>> shortAndLongLabels = new HashMap<>();
    private Map<Character, Character> repeated = new HashMap<>();  // second letter to first letter

    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard, null);
        keyboards.put("qwerty", new Keyboard(this, R.xml.qwerty));
        keyboards.put("QWERTY", new Keyboard(this, R.xml.qwert_upper));
        keyboards.put("symbols", new Keyboard(this, R.xml.symbols1));
        shortAndLongLabels.put("qwerty", null);
        shortAndLongLabels.put("QWERTY", null);
        shortAndLongLabels.put("symbols", null);
        current = "qwerty";
        kv.setKeyboard(keyboards.get(current));
        kv.setOnKeyboardActionListener(this);
        for (Keyboard keyboard: keyboards.values()) {
            Log.i("MATHNOTELITE", "OK1");
            for (Keyboard.Key key : keyboard.getKeys()) {
//                Log.i("MATHNOTELITE", key.label.toString());
                if (key.label == null) continue;
                if (labelToIgnore.contains(key.label)) continue;
                if (key.label.length() < 2) continue;
                repeated.put(key.label.charAt(1), key.label.charAt(0));
            }
        }
        return kv;
    }


    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        InputConnection ic = getCurrentInputConnection();
        switch(primaryCode){
            case Keyboard.KEYCODE_DELETE :
                ic.deleteSurroundingText(1, 0);
                break;
            case -1:
                current = "QWERTY";
                kv.setKeyboard(keyboards.get(current));
                break;
            case -2:
                current = "symbols";
                kv.setKeyboard(keyboards.get(current));
                break;
            case -3:
                current = "qwerty";
                kv.setKeyboard(keyboards.get(current));
                break;
            case -6:
                if (shortAndLongLabels.get(current) == null) {
                    Map<CharSequence, CharSequence> map = new HashMap<>();
                    for (Keyboard.Key key: kv.getKeyboard().getKeys()) {
                        if (key.label == null) continue;
                        if (labelToIgnore.contains(key.label)) continue;
                        CharSequence newLabel = key.label.subSequence(0, 1);
                        map.put(newLabel, key.label);
                        key.label = newLabel;
                    }
                    shortAndLongLabels.put(current, map);
                } else {
                    for (Keyboard.Key key: kv.getKeyboard().getKeys()) {
                        if (key.label == null) continue;
                        if (labelToIgnore.contains(key.label)) continue;
                        key.label = shortAndLongLabels.get(current).get(key.label);
                    }
                    shortAndLongLabels.put(current, null);
                }

                kv.invalidateAllKeys();
                break;
            case -7:
                CharSequence surrounding = ic.getTextBeforeCursor(10, 0);
                String[] parts = surrounding.toString().split(" ");
                String oldText = parts[parts.length-1];
                Log.i("MATHNOTELITE", "Surrounding: " + oldText);
                StringBuilder newText = new StringBuilder();
                boolean replaced = false;
                for (char c: parts[parts.length-1].toCharArray()) {
                    if (repeated.containsKey(c)) {
                        replaced = true;
                        Log.i("MATHNOTELITE", "Replacing " + c + " with " + repeated.get(c));
                        newText.append(repeated.get(c));
                        newText.append(repeated.get(c));
                    } else {
                        newText.append(c);
                    }
                }
                if (replaced) {
                    newText.append(" ");  // Extra char that will be deleted
                    ic.deleteSurroundingText(oldText.length(), 0);
                    ic.commitText(newText, 1);
                }
                break;
            case Keyboard.KEYCODE_DONE:
                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                break;
            default:
                char code = (char)primaryCode;
//                if(Character.isLetter(code) && caps){
//                    code = Character.toUpperCase(code);
//                }
                ic.commitText(String.valueOf(code),1);
        }

    }

    @Override
    public void onPress(int primaryCode) {
    }
    @Override
    public void onRelease(int primaryCode) {
    }
    @Override
    public void onText(CharSequence text) {
    }
    @Override
    public void swipeDown() {
    }
    @Override
    public void swipeLeft() {
    }
    @Override
    public void swipeRight() {
    }
    @Override
    public void swipeUp() {
    }
}