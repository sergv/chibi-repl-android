package android.chibi.scheme.repl;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;

import android.util.Log;


/**
 * The main Scheme Droid home screen activity.
 *
 * @author daniel@meltingwax.net (Daniel da Silva)
 * @author Olexandr Tereshchuk - <a href="http://stanfy.com.ua">Stanfy LLC</a>
 * @author Sergey Vinokurov (serg.foo@gmail.com)
 */
public class ReplActivity extends FragmentActivity {

public static final String TAG = "chibi";

ReplFragment replFrag;

@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setTitle(R.string.title_repl);
    getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, 0);
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

    Intent intent = getIntent();
    String fileToLoad = intent != null && intent.getData() != null ?
        getIntent().getData().getPath() : null ;
    if (replFrag == null) {
        Log.d(TAG, "repl does not exist, constructing and loading file " + fileToLoad);
        final Bundle args = new Bundle(1);
        if (fileToLoad != null) {
            args.putString(ReplFragment.ARG_FILE, fileToLoad);
        }
        replFrag = new ReplFragment();
        replFrag.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(android.R.id.content,
                                                               replFrag,
                                                               ReplFragment.TAG).commit();
    } else if (fileToLoad != null) {
        Log.d(TAG, "repl exists, loading file " + fileToLoad);
        replFrag.loadFile(fileToLoad);
    }
}

}
