package com.ds.beboard.input;

/*
import hani.momanii.supernova_emoji_library.Helper.EmojiconGridView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconsPopup;
import hani.momanii.supernova_emoji_library.emoji.Emojicon;
*/


import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.CompletionInfo;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import android.view.textservice.SentenceSuggestionsInfo;
import android.view.textservice.SpellCheckerSession;
import android.view.textservice.SuggestionsInfo;
import android.view.textservice.TextServicesManager;
import android.widget.PopupWindow;

import com.ds.beboard.MainActivity;
import com.ds.beboard.R;
import com.ds.beboard.SettingActivity;

import java.util.List;

import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.KeyEvent.KEYCODE_WINDOW;
import static com.ds.beboard.ThemeActivity.THEME_KEY;

public class Beboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener{


    private  boolean mCapsLock;
    boolean mCompletionOn;

    private CompletionInfo[] mCompletions;
    private long mLastShiftTime;
    private LatinKeyboardView mInputView;
    private LatinKeyboardView mContextualkey;
    private LatinKeyboard mSymbolsKeyboard;
    private LatinKeyboard mSymbolsShiftedKeyboard;
    private LatinKeyboard mQwertyKeyboard;
    private LatinKeyboard mEmoji;
    private LatinKeyboard mcontext;
    private int mLastDisplayWidth;
    private InputMethodManager mInputMethodManager;
    private boolean mPredictionOn;
    private String mWordSeparators;
    private SpellCheckerSession mScs;
    private StringBuilder mComposing = new StringBuilder();
    private LatinKeyboard mCurKeyboard;
    private long mMetaState;

    //private EmojiconsPopup popupWindow = null;
    private PopupWindow popupWindow;
    private CandidateView mCandidateView;
    private List<String> mSuggestions;
    SharedPreferences sharedPreferences;

    int[] THE_LAYOUTS = {R.layout.keyboardview, R.layout.keyboardview2, R.layout.keyboardview3,
            R.layout.keyboardview4, R.layout.keyboardview5, R.layout.keyboardview6, R.layout.keyboardview7,
            R.layout.keyboardview8, R.layout.keyboardview9, R.layout.keyboardview10, R.layout.keyboardview11, R.layout.keyboardview12
            , R.layout.keyboardview13, R.layout.keyboardview14, R.layout.keyboardview15, R.layout.keyboardview16, R.layout.keyboardview17
            , R.layout.keyboardview18, R.layout.keyboardview19, R.layout.keyboardview20, R.layout.keyboardview21};

    /**
     * Initialisation principale du composant de méthode d'entrée. Assurez-vous d'appeler
     * en super classe.
     */
    @Override public void onCreate() {
        super.onCreate();
        mInputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        mWordSeparators = getResources().getString(R.string.word_separators);
        final TextServicesManager tsm = (TextServicesManager) getSystemService(Context.TEXT_SERVICES_MANAGER_SERVICE);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

    }

    /**
     *C'est le point où vous pouvez effectuer toute l'initialisation de votre interface utilisateur. Il
     * est appelé après la création et tout changement de configuration.
     */
    @Override public void onInitializeInterface() {
        if (mQwertyKeyboard != null) {

            /** Des changements de configuration peuvent se produire après la recréation du clavier,
             nous devons donc être en mesure de reconstruire les claviers si disponible
             l'espace a changé
             */

            int displayWidth = getMaxWidth();
            if (displayWidth == mLastDisplayWidth) return;
            mLastDisplayWidth = displayWidth;
        }
        mQwertyKeyboard = new LatinKeyboard(this, R.xml.fongbe);
        mSymbolsKeyboard = new LatinKeyboard(this, R.xml.symbole1);
        mSymbolsShiftedKeyboard = new LatinKeyboard(this, R.xml.symbole2);
        mcontext =new LatinKeyboard(this,R.xml.context1);
    }

    /**
     * Appelé par le framework lorsque votre vue pour créer une entrée doit
     * être généré. Ce sera appelé la première fois que votre méthode de saisie
     * est affiché, et chaque fois qu'il doit être recréé, par exemple en raison de
     * un changement de configuration.*/

    @Override
    public View onCreateInputView() {
        // Set custom theme to input view.
        mInputView = (LatinKeyboardView) getLayoutInflater().inflate(
                THE_LAYOUTS[sharedPreferences.getInt(THEME_KEY, 0)], null);
        mInputView.setOnKeyboardActionListener(this);
        setLatinKeyboard(mQwertyKeyboard);
        return mInputView;
    }


    private void setLatinKeyboard(LatinKeyboard nextKeyboard) {
        final boolean shouldSupportLanguageSwitchKey = mInputMethodManager.shouldOfferSwitchingToNextInputMethod(getToken());
        nextKeyboard.setLanguageSwitchKeyVisibility(shouldSupportLanguageSwitchKey);
        mInputView.setKeyboard(nextKeyboard);
    }

    /**
     *Appelé par le cadre lorsque votre vue pour montrer les candidats doit
     * être généré, comme {@link #onCreateInputView}.
     */
    @Override public View onCreateCandidatesView() {
        mCandidateView = new CandidateView(this);
        mCandidateView.setService(this);
        return mCandidateView;
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);

        // Restart the InputView to apply right theme selected.
        setInputView(onCreateInputView());

        // Reset our state.  We want to do this even if restarting, because
        // the underlying state of the text editor could have changed in any way.
        mComposing.setLength(0);


        if (!restarting) {
            // Clear shift states.
            mMetaState = 0;
        }

        mPredictionOn = false;
        mCompletionOn = false;
        mCompletions = null;

        // We are now going to initialize our state based on the type of
        // text being edited.
        switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
            case InputType.TYPE_CLASS_NUMBER:
            case InputType.TYPE_CLASS_DATETIME:
                // Numbers and dates default to the symbols keyboard, with
                // no extra features.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_PHONE:
                // Phones will also default to the symbols keyboard, though
                // often you will want to have a dedicated phone keyboard.
                mCurKeyboard = mSymbolsKeyboard;
                break;

            case InputType.TYPE_CLASS_TEXT:
                // This is general text editing.  We will default to the
                // normal alphabetic keyboard, and assume that we should
                // be doing predictive text (showing candidates as the
                // user types).
                mCurKeyboard = mQwertyKeyboard;
                mPredictionOn = true;

                // We now look for a few special variations of text that will
                // modify our behavior.
                int variation = attribute.inputType & InputType.TYPE_MASK_VARIATION;
                if (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                        variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD) {
                    // Do not display predictions / what the user is typing
                    // when they are entering a password.
                    mPredictionOn = false;
                }

                if (variation == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                        || variation == InputType.TYPE_TEXT_VARIATION_URI
                        || variation == InputType.TYPE_TEXT_VARIATION_FILTER) {
                    // Our predictions are not useful for e-mail addresses
                    // or URIs.
                    mPredictionOn = false;
                }

                if ((attribute.inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE) != 0) {
                    // If this is an auto-complete text view, then our predictions
                    // will not be shown and instead we will allow the editor
                    // to supply their own.  We only show the editor's
                    // candidates when in fullscreen mode, otherwise relying
                    // own it displaying its own UI.
                    mPredictionOn = false;
                    mCompletionOn = isFullscreenMode();
                }

                // We also want to look at the current state of the editor
                // to decide whether our alphabetic keyboard should start out
                // shifted.
                updateShiftKeyState(attribute);
                break;

            default:
                // For all unknown input types, default to the alphabetic
                // keyboard with no special features.
                mCurKeyboard = mQwertyKeyboard;
        }

        // Update the label on the enter key, depending on what the application
        // says it will do.
        mCurKeyboard.setImeOptions(getResources(), attribute.imeOptions);
    }




    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);

        mInputView.closing();
        final InputMethodSubtype subtype = mInputMethodManager.getCurrentInputMethodSubtype();
        mInputView.setSubtypeOnSpaceKey(subtype);
    }







    /**
     *Ceci est le point principal où nous faisons notre initialisation de la méthode d'entrée
     * pour commencer à fonctionner sur une application. À ce stade, nous avons été
     * lié au client, et reçoit maintenant toutes les informations détaillées
     * sur la cible de nos modifications.
     */


    private void commitTyped(InputConnection inputConnection) {
        if (mComposing.length() > 0) {
            inputConnection.commitText(mComposing, mComposing.length());
            mComposing.setLength(0);

        }
    }


    private void updateShiftKeyState(EditorInfo attr) {
        if (attr != null
                && mInputView != null && mQwertyKeyboard == mInputView.getKeyboard()) {
            int caps = 0;
            EditorInfo ei = getCurrentInputEditorInfo();
            if (ei != null && ei.inputType != InputType.TYPE_NULL) {
                caps = getCurrentInputConnection().getCursorCapsMode(attr.inputType);
            }

            mInputView.setShifted(mCapsLock || caps != 0);
        }
    }

    private void keyDownUp(int keyEventCode) {
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_DOWN, keyEventCode));
        getCurrentInputConnection().sendKeyEvent(
                new KeyEvent(KeyEvent.ACTION_UP, keyEventCode));
    }




    private void sendKey(int keyCode) {
        switch (keyCode) {
            case '\n':
                keyDownUp(KEYCODE_ENTER);
                break;
            default:
                if (keyCode >= '0' && keyCode <= '9') {
                    keyDownUp(keyCode - '0' + KeyEvent.KEYCODE_0);
                } else {
                    getCurrentInputConnection().commitText(String.valueOf((char) keyCode), 1);
                }
                break;
        }
    }



    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        Log.d("Test","KEYCODE: " + primaryCode);
        if (isWordSeparator(primaryCode)) {
            // Handle separator
            if (mComposing.length() > 0) {
                commitTyped(getCurrentInputConnection());
            }
            sendKey(primaryCode);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (primaryCode == Keyboard.KEYCODE_DELETE) {
            handleBackspace();
        } else if (primaryCode == Keyboard.KEYCODE_SHIFT) {
            handleShift();
        } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {
            handleClose();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_LANGUAGE_SWITCH) {
            handleLanguageSwitch();
            return;
        } else if (primaryCode == LatinKeyboardView.KEYCODE_OPTIONS) {
            // Show a menu or somethin'

        } else if ((primaryCode == LatinKeyboardView.KEYCODE_EMOJI_SWITCH) && (mInputView != null)) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mEmoji) {
                setLatinKeyboard(mQwertyKeyboard);
            } else {
                setLatinKeyboard(mEmoji);
                mEmoji.setShifted(false);
            }
        }
        else if (primaryCode == Keyboard.KEYCODE_MODE_CHANGE && mInputView != null) {
            Keyboard current = mInputView.getKeyboard();
            if (current == mSymbolsKeyboard || current == mSymbolsShiftedKeyboard) { setLatinKeyboard(mQwertyKeyboard);
            } else {
                setLatinKeyboard(mSymbolsKeyboard);
                mSymbolsKeyboard.setShifted(false);
            }
        } else if (primaryCode == -10000) {
            // Show Emoticons
            startActivity(new Intent(Beboard.this, SettingActivity.class));
            //showEmoticons();
        } else if (primaryCode == -10001) {
            // Zero Space
            mComposing.append("\u200C");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == -10002) {
            // ẋ
            mComposing.append("ẋ");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == -10003) {
            // Ẋ
            mComposing.append("\u1E8A");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else if (primaryCode == 1567) {
            // Question mark.
            mComposing.append("\u061F");
            getCurrentInputConnection().setComposingText(mComposing, 1);
        } else {
            handleCharacter(primaryCode, keyCodes);
        }




    }





    /**
     * This method displays Emoticons when called.

    public void showEmoticons() {
        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        if (layoutInflater != null) {
            View popupView = layoutInflater.inflate(R.layout.emoji_listview_layout, null);
            popupWindow = new EmojiconsPopup(popupView,this,null);
            ((EmojiconsPopup) popupWindow).setSizeForSoftKeyboard();
            ((EmojiconsPopup) popupWindow).setSize(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            popupWindow.showAtLocation(mInputView.getRootView(), Gravity.BOTTOM, 0, 0);
            // If the text keyboard closes, also dismiss the emoji popup
            ((EmojiconsPopup) popupWindow).setOnSoftKeyboardOpenCloseListener(new EmojiconsPopup.OnSoftKeyboardOpenCloseListener() {
                @Override
                public void onKeyboardOpen(int keyBoardHeight) {
                }

                @Override
                public void onKeyboardClose() {
                    if (popupWindow.isShowing())
                        popupWindow.dismiss();
                }
            });
            ((EmojiconsPopup) popupWindow).setOnEmojiconClickedListener(new EmojiconGridView.OnEmojiconClickedListener() {
                @Override
                public void onEmojiconClicked(Emojicon emojicon) {
                    mComposing.append(emojicon.getEmoji());
                    commitTyped(getCurrentInputConnection());
                }
            });
            ((EmojiconsPopup) popupWindow).setOnEmojiconBackspaceClickedListener(new EmojiconsPopup.OnEmojiconBackspaceClickedListener() {
                @Override
                public void onEmojiconBackspaceClicked(View v) {
                    KeyEvent event = new KeyEvent(0, 0, 0, KeyEvent.KEYCODE_DEL, 0, 0, 0, 0, KeyEvent.KEYCODE_ENDCALL);
                    handleBackspace();
                }
            });
        }
    }


    public void closeEmoticons() {
        if (popupWindow != null)
            popupWindow.dismiss();
    }
*/

    /**
     * Play sound when key is pressed.
     */
    private void playClick(int keyCode) {
        AudioManager am = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (am != null) {
            switch (keyCode) {
                case 32:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                    break;
                case Keyboard.KEYCODE_DONE:
                case 10:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                    break;
                case Keyboard.KEYCODE_DELETE:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                    break;
                default:
                    am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
            }
        }
    }




    public void setSuggestions(List<String> suggestions, boolean completions,
                               boolean typedWordValid) {
        if (suggestions != null && suggestions.size() > 0) {
            setCandidatesViewShown(true);
        } else if (isExtractViewShown()) {
            setCandidatesViewShown(true);
        }
        mSuggestions = suggestions;
        if (mCandidateView != null) {
            mCandidateView.setSuggestions(suggestions, completions, typedWordValid);
        }
    }




    private void handleBackspace() {
        final int length = mComposing.length();
        if (length > 1) {
            mComposing.delete(length - 1, length);
            getCurrentInputConnection().setComposingText(mComposing, 1);

        } else if (length > 0) {
            mComposing.setLength(0);
            getCurrentInputConnection().commitText("", 0);
        } else {
            keyDownUp(KeyEvent.KEYCODE_DEL);
        }
        updateShiftKeyState(getCurrentInputEditorInfo());
    }



    private void handleShift() {
        if (mInputView == null) {
            return;
        }

        Keyboard currentKeyboard = mInputView.getKeyboard();
        if (mQwertyKeyboard == currentKeyboard) {
            // Alphabet keyboard
            checkToggleCapsLock();
            mInputView.setShifted(mCapsLock || !mInputView.isShifted());
        } else if (currentKeyboard == mSymbolsKeyboard) {
            mSymbolsKeyboard.setShifted(true);
            setLatinKeyboard(mSymbolsShiftedKeyboard);
            mSymbolsShiftedKeyboard.setShifted(true);
        } else if (currentKeyboard == mSymbolsShiftedKeyboard) {
            mSymbolsShiftedKeyboard.setShifted(false);
            setLatinKeyboard(mSymbolsKeyboard);
            mSymbolsKeyboard.setShifted(false);
        }
    }

    private void handleCharacter(int primaryCode, int[] keyCodes) {
        if (isInputViewShown()) {
            if (mInputView.isShifted()) {
                primaryCode = Character.toUpperCase(primaryCode);
            }
        }
        if (mPredictionOn) { mComposing.append((char) primaryCode);
            getCurrentInputConnection().setComposingText(mComposing, 1);
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else {
            getCurrentInputConnection().commitText(
                    String.valueOf((char) primaryCode), 1);
        }
    }



    private void handleClose() {
        commitTyped(getCurrentInputConnection());
        requestHideSelf(0);
        mInputView.closing();
    }


    private IBinder getToken() {
        final Dialog dialog = getWindow();
        if (dialog == null) {
            return null;
        }
        final Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        return window.getAttributes().token;
    }



    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void handleLanguageSwitch() {
        mInputMethodManager.switchToNextInputMethod(getToken(), false /* onlyCurrentIme */);
    }


    private void checkToggleCapsLock() {
        long now = System.currentTimeMillis();

        if (mLastShiftTime + 800 > now) {
            mCapsLock = !mCapsLock;
            mLastShiftTime = 0;
        } else {
            mLastShiftTime = now;
        }
    }


    private String getWordSeparators() {
        return mWordSeparators;
    }



    public boolean isWordSeparator(int code) {
        String separators = getWordSeparators();
        return separators.contains(String.valueOf((char)code));
    }


    public void pickSuggestionManually(int index) {

        if (mCompletionOn && mCompletions != null && index >= 0
                && index < mCompletions.length) {
            CompletionInfo ci = mCompletions[index];
            getCurrentInputConnection().commitCompletion(ci);
            if (mCandidateView != null) {
                mCandidateView.clear();
            }
            updateShiftKeyState(getCurrentInputEditorInfo());
        } else if (mComposing.length() > 0) {

            if (mPredictionOn && mSuggestions != null && index >= 0) {
                mComposing.replace(0, mComposing.length(), mSuggestions.get(index));
            }
            commitTyped(getCurrentInputConnection());

        }
    }





    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }



    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }



}