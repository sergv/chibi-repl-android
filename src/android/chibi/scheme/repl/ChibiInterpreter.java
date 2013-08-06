package android.chibi.scheme.repl;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.zip.ZipInputStream;

import android.content.Context;
import android.content.res.AssetManager;

import android.util.Log;

public class ChibiInterpreter {

public static final String TAG = "chibi";

private static boolean librariesCopied = false;
/* path where all chibi's files reside */
private static final String ASSETS_INIT_ARCHIVE = "lib.zip";

static {
    System.loadLibrary("chibi-scheme");
    System.loadLibrary("chibi");
}

private long interpreterHandle;
private String eval_result[];
private boolean eval_error[];

private ChibiInterpreter(long interpreterHandle) {
    this.interpreterHandle = interpreterHandle;
    eval_result = new String[1];
    eval_error = new boolean[1];
}

private static native long init(String working_dir);
private static native void release(long interpreter_handle);
/* returns complete output of code including stdout, stderr
   and result of evaluation at the end (i.e. usual repl style) */
private static native void eval(long interpreter_handle, String code,
                                Object out_result[], boolean out_error[]);

public static ChibiInterpreter initializeInterpreter(Context context) {
    File cache = context.getCacheDir();
    final String cache_path = cache.getAbsolutePath();
    if (!librariesCopied) {
        /* populate cache dir */
        AssetManager assets = context.getAssets();
        try {
            Utils.unpackZip(
                new ZipInputStream(
                    new BufferedInputStream(assets.open(ASSETS_INIT_ARCHIVE))),
                cache_path);
            librariesCopied = true;
        } catch (IOException e) {
            Log.d(TAG,
                  "Error whie copynig chibi's files: " + e +
                  "\nStacktrace:\n" + Utils.getStackTrace(e));
        }
    }
    String chibi_files_path = cache_path + File.separator + "lib";
    Log.d(TAG, "Initializing with path " + chibi_files_path);
    long handle = init(chibi_files_path);
    if (handle == 0) {
        Log.d(TAG,
              "Error while initializing interpreter: invalid handle returned: " +
              handle);
    }
    return new ChibiInterpreter(handle);
}

public static class EvalResult {
    String result;
    boolean error;
}

public EvalResult evaluate(String input) {
    EvalResult res = new EvalResult();
    eval(interpreterHandle, input, eval_result, eval_error);
    res.result = eval_result[0];
    res.error = eval_error[0];
    return res;
}

@Override
protected void finalize() throws Throwable {
    recycle();
    super.finalize();
}

public synchronized void recycle() {
    if (interpreterHandle != 0) {
        release(interpreterHandle);
        interpreterHandle = 0;
    }
}

}
