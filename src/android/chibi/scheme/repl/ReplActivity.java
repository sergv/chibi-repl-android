package android.chibi.scheme.repl;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;


/**
 * The main Scheme Droid home screen activity.
 *
 * @author daniel@meltingwax.net (Daniel da Silva)
 * @author Olexandr Tereshchuk - <a href="http://stanfy.com.ua">Stanfy LLC</a>
 * @author Sergey Vinokurov (serg.foo@gmail.com)
 */
public class ReplActivity extends FragmentActivity {

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.title_repl);
        getWindow().setFeatureInt(Window.FEATURE_NO_TITLE, 0);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 0);

        if (getSupportFragmentManager().findFragmentByTag(ReplFragment.TAG) == null) {
            final Bundle args = new Bundle(1);
            if (getIntent().getData() != null) {
                args.putString(ReplFragment.ARG_FILE, getIntent().getData().getPath());
            }
            final ReplFragment f = new ReplFragment();
            f.setArguments(args);
            getSupportFragmentManager().beginTransaction().replace(android.R.id.content, f, ReplFragment.TAG).commit();
        }
    }

}