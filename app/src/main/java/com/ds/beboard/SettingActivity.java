package com.ds.beboard;

import android.annotation.SuppressLint;
import android.app.AppComponentFactory;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.provider.Settings;
import android.util.Log;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import java.util.List;

import static android.widget.Toast.LENGTH_LONG;
import static com.ds.beboard.R.string.elseinputtext;


public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getFragmentManager().beginTransaction().replace(android.R.id.content,new MainSettingFragment()).commit();

        //   MainSettingFragment mainSettingFragment = new MainSettingFragment();

        //   FragmentTransaction transaction = getSupportFragmentManager().beginTransaction().add(R.id.content,mainSettingFragment, null);
        //  transaction.commit();

    }

    @SuppressLint("ValidFragment")
    public static class MainSettingFragment extends PreferenceFragment{

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.beboard_preferences);

            Preference pEnable = (Preference) findPreference("penable");
            pEnable.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivityForResult(new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS), 0);
                    return false;
                }
            });


            Preference pinput = (Preference) findPreference("pinput");
            pinput.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if (isInputEnabled(getActivity().getApplication())) {
                        ((InputMethodManager) getActivity().getApplication() .getSystemService(Context.INPUT_METHOD_SERVICE)).showInputMethodPicker();
                    } else {
                        Toast.makeText(getActivity().getApplication() , getString(elseinputtext), Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }

                private boolean isInputEnabled(Context context) {
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    List<InputMethodInfo> mInputMethodProperties = imm.getEnabledInputMethodList();

                    final int N = mInputMethodProperties.size();
                    boolean isInputEnabled = false;

                    for (int i = 0; i < N; i++) {

                        InputMethodInfo imi = mInputMethodProperties.get(i);
                        Log.d("INPUT ID", String.valueOf(imi.getId()));
                        if (imi.getId().contains(getActivity().getPackageName())) {
                            isInputEnabled = true;
                        }
                    }

                    if (isInputEnabled) {
                        return true;
                    } else {
                        return false;
                    }
                }

            });

           Preference ptheme = (Preference) findPreference("ptheme");
            ptheme.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                  startActivity(new Intent(getActivity().getApplication(),ThemeActivity.class));
                    return false;
                }

            });

            Preference pabout = (Preference) findPreference("pabout");
            pabout.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    startActivity(new Intent(getActivity().getApplication(),AboutActivity.class));
                    return false;
                }

            });


        }

    }
}
