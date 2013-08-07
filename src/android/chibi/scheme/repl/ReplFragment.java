package android.chibi.scheme.repl;

import java.io.File;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.chibi.scheme.repl.R;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import android.util.Log;

/**
 * Read Eval Print Loop activity.
 *
 * @author daniel@meltingwax.net (Daniel da Silva)
 * @author Olexandr Tereshchuk - <a href="http://stanfy.com.ua">Stanfy LLC</a>
 * @author Sergey Vinokurov (serg.foo@gmail.com)
 */
public class ReplFragment extends Fragment {

/** Logging tag. */
public static final String TAG = "chibi";

/** Arguments. */
public static final String ARG_FILE = "file-to-open";

private static final int INTENT_LOAD_FILE = 1;

private ChibiInterpreter interp;
/** Console output. */
// private TextView console;
private ListView interaction_history;
private InteractionAdapter interaction_history_adapter;
/** Console input. */
private EditText entry;

/** File was loaded. */
private boolean initialized = false;

public class InteractionCell {
    public final String input;
    public final String output;

    public InteractionCell(String input, String output) {
        this.input = input;
        this.output = output;
    }
}

private class InteractionAdapter extends BaseAdapter {

    private List<InteractionCell> cells = new ArrayList<InteractionCell>();

    public int getCount() {
        return cells.size();
    }

    public InteractionCell getItem(int i) {
        return cells.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        final View cellView = LayoutInflater.from(viewGroup.getContext())
                              .inflate(R.layout.interaction_cell, viewGroup, false);
        final TextView inputView = (TextView) cellView.findViewById(R.id.interaction_cell_input);
        final TextView outputView = (TextView) cellView.findViewById(R.id.interaction_cell_output);

        final InteractionCell cell = cells.get(i);
        inputView.setText(cell.input);
        outputView.setText(cell.output);
        return cellView;
    }

    public void addCell(InteractionCell cell) {
        cells.add(cell);
        notifyDataSetChanged();
    }

    public void removeCell(int i) {
        cells.remove(i);
        notifyDataSetChanged();
    }

    public void clear() {
        cells.clear();
        notifyDataSetInvalidated();
    }
}


@Override
public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
    setRetainInstance(true);

    interp = ChibiInterpreter.initializeInterpreter(getActivity());
}

@Override
public void onDestroy() {
    super.onDestroy();
    ChibiInterpreter.resetLibraries(getActivity());
}

@Override
public View onCreateView(final LayoutInflater inflater,
                         final ViewGroup container,
                         final Bundle savedInstanceState) {
    return inflater.inflate(R.layout.repl, container, false);
}

@Override
public void onViewCreated(final View view, final Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    interaction_history_adapter = new InteractionAdapter();
    interaction_history = (ListView) view.findViewById(R.id.interaction_history);
    interaction_history.setAdapter(interaction_history_adapter);
    interaction_history.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        @SuppressWarnings({"unchecked"})
        public void onItemClick(AdapterView<?> adapterView,
                                View view,
                                int position,
                                long l) {
            InteractionCell cell = ((AdapterView<InteractionAdapter>) adapterView).getAdapter().getItem(position);
            entry.setText(cell.input);
        }
        });

    final FragmentActivity activity = getActivity();
    AdapterView.OnItemLongClickListener remove_cell = new AdapterView.OnItemLongClickListener() {
        @SuppressWarnings({"unchecked"})
        public boolean onItemLongClick(final AdapterView<?> adapterView,
                                       final View view,
                                       final int position,
                                       final long id) {
            final InteractionAdapter adapter = ((AdapterView<InteractionAdapter>) adapterView).getAdapter();
            final InteractionCell cell = adapter.getItem(position);

            final DialogInterface.OnClickListener on_ok =
                new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface iface, int which) {
                    adapter.removeCell(position);
                }
                };

            final String msg = String.format("Do you want to remove cell\n%s\n%s",
                                             cell.input,
                                             cell.output);

            DialogFragment dialog_manager = new DialogFragment() {
                @Override
                public Dialog onCreateDialog(Bundle saved_state) {
                    AlertDialog.Builder builder =
                        new AlertDialog.Builder(activity);
                    builder.setTitle("Confirm history item removal")
                        .setCancelable(true)
                        .setIcon(android.R.drawable.stat_sys_warning)
                        .setMessage(msg)
                        .setPositiveButton(android.R.string.ok, on_ok)
                        .setNegativeButton(android.R.string.cancel, null);
                    return builder.create();
                }
                };
            dialog_manager.show(activity.getSupportFragmentManager(),
                                "warning");

            return true;
        }
    };
    interaction_history.setOnItemLongClickListener(remove_cell);


    final EditText oldEntry = entry;
    entry = (EditText) view.findViewById(R.id.code_input);
    if (oldEntry != null) {
        entry.setText(oldEntry.getText());
    }

    view.findViewById(R.id.button_eval).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            evaluate(entry.getText().toString().trim());
        }
    });
}

@Override
public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    init();
    Bundle args = getArguments();
    if (args != null && args.containsKey(ARG_FILE)) {
        loadFile(args.getString(ARG_FILE));
    }
}

@Override
public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.menu_repl, menu);
}

@Override
public boolean onOptionsItemSelected(final MenuItem item) {
    final int itemId = item.getItemId();
    if (itemId == R.id.menu_clear) {
        clear();
        return true;
    } else if (itemId == R.id.menu_resources) {
        startActivity(new Intent(getActivity(), SchemeResources.class));
        return true;
    } else if (itemId == R.id.menu_interrupt) {
        // js.getEvaluator().interrupt(Thread.currentThread());
        return true;
    } else if (itemId == R.id.menu_load) {
        Intent get_file_intent = new Intent(Intent.ACTION_PICK);
        get_file_intent.setDataAndType(
            Uri.fromFile(Environment.getExternalStorageDirectory()),
            "vnd.android.cursor.dir/lysesoft.andexplorer.file");
        get_file_intent.putExtra("explorer_title", "Pick a file to load");
        get_file_intent.putExtra("browser_filter_extension_whitelist",
                                 "*.scm,*.sc,*.ss,*.stk,*.sch,*.oak");
        try {
            startActivityForResult(get_file_intent, INTENT_LOAD_FILE);
        } catch (ActivityNotFoundException e) {
            complainDialog("Error while picking file",
                           "Activity vnd.android.cursor.dir/lysesoft.andexplorer.file does not exists (i.e. AndExplorer probably is not installed)");
        }
        return true;
    }

    return false;
}

@Override
public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode == Activity.RESULT_OK &&
        data != null &&
        data.getData() != null) {
        if (requestCode == INTENT_LOAD_FILE) {
            loadFile(data.getData().getPath());
        }
    }
}

public void loadFile(final String filename) {
    Log.d(TAG, "loading " + filename);
    final File f = new File(filename);
    if (f.exists()) {
        String code = "(load \"" + f.getAbsolutePath() + "\")";
        if (entry.getText().toString().trim().length() == 0) {
              /* if there were no input then paste loading code so
                 user may see it in case of errors */
            entry.setText(code);
            evaluate(code, true);
        } else {
            evaluate(code, false);
        }
    } else {
        complainDialog("Error while loading",
                       String.format("File %s does not exists",
                                     filename));
    }

}

private void init() {
    if (! initialized) {
        /* import threads (srfi 18) that will be needed for interruptible
           evaluation later */
        initialized = true;
        evaluate("(import (scheme base) (srfi 18))")
    }
    Log.d(TAG, "init done");
}

private void clear() {
    if (interp != null) {
        interp.recycle();
    }
    interp = ChibiInterpreter.initializeInterpreter(getActivity());
    interaction_history_adapter.clear();
    initialized = false;
    init();
}

/* returns true if there was no error */
private boolean evaluate(final String input) {
    return evaluate(input, true);
}

private boolean evaluate(final String input, boolean resetInputView) {
    if (! initialized) {
        String msg = "error: attempting to evaluate " + input + " while not initialized";
        Log.d(TAG, msg);
        throw new RuntimeException(msg);
    }
    /* TODO: move this to separate evaluation thread to make interrupts
       work; */
    if (input.length() > 0) {
        ChibiInterpreter.EvalResult result = interp.evaluate(input);
        if (resetInputView  && !result.error) {
            entry.setText("");
        }
        interaction_history_adapter.addCell(new InteractionCell(input, result.result));
        return !result.error;
    } else {
        Toast.makeText(getActivity(), R.string.error_code_empty, Toast.LENGTH_SHORT).show();
        return false;
    }
}

void complainDialog(final String title,
                    final String message) {
    final FragmentActivity activity = getActivity();
    DialogFragment dialog_manager = new DialogFragment() {
        @Override
        public Dialog onCreateDialog(Bundle saved_state) {
            AlertDialog.Builder builder =
                new AlertDialog.Builder(activity);
            builder.setTitle(title)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null);
            return builder.create();
        }
        };
    dialog_manager.show(activity.getSupportFragmentManager(),
                        "warning");
}

}



