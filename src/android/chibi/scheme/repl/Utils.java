package android.chibi.scheme.repl;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.PrintWriter;
import java.io.StringWriter;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.util.Log;

final public class Utils {

public static final String TAG = "chibi";

static public void copyFile(InputStream in, OutputStream out, byte buffer[]) {
    BufferedInputStream bin = new BufferedInputStream(in);
    BufferedOutputStream bout = new BufferedOutputStream(out);

    if (buffer == null) {
        buffer = new byte[1024];
    }
    int ret = 0;
    try {
        while ((ret = bin.read(buffer, 0, buffer.length)) != -1) {
            bout.write(buffer, 0, ret);
        }
        bout.flush();
    } catch (Exception e) {
        Log.d(TAG, "exception while copying files: " + e);
    } finally {
        close(bin);
        close(bout);
    }
}

static public boolean unpackZip(ZipInputStream archive, String destDir) {
    try {
        String filename;
        ZipEntry ze;
        byte[] buffer = new byte[1024];
        int count;

        while ((ze = archive.getNextEntry()) != null) {
            filename = destDir + File.separator + ze.getName();

            /* it is assumed that archive will contain directory entries */
            if (ze.isDirectory()) {
                new File(filename).mkdirs();
            } else {
                BufferedOutputStream fout =
                    new BufferedOutputStream(new FileOutputStream(filename));
                try {
                    while ((count = archive.read(buffer, 0, buffer.length)) != -1) {
                        fout.write(buffer, 0, count);
                    }
                    fout.flush();
                } finally {
                    close(fout);
                }
            }
            archive.closeEntry();
        }
        close(archive);
    } catch (Exception e) {
        Log.d(TAG, "exception while unzippnig file: " + e +
              "\nStacktrace:\n" + getStackTrace(e));
        return false;
    }

    return true;
}



static public String getStackTrace(Exception e) {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    e.printStackTrace(pw);
    return sw.toString();
}

static public void close(InputStream in) {
    try {
        in.close();
    } catch (Exception e) {
        Log.d(TAG, "exception while closing input stream: " + e);
    }
}

static public void close(Reader in) {
    try {
        in.close();
    } catch (Exception e) {
        Log.d(TAG, "exception while closing reader: " + e);
    }
}

static public void close(OutputStream out) {
    try {
        out.close();
    } catch (Exception e) {
        Log.d(TAG, "exception while closing output stream: " + e);
    }
}

static public void close(Writer out) {
    try {
        out.close();
    } catch (Exception e) {
        Log.d(TAG, "exception while closing writer: " + e);
    }
}

public static void trimCache(Context context) {
    try {
        File dir = context.getCacheDir();
        if (dir != null && dir.isDirectory()) {
            deleteDir(dir);
        }
    } catch (Exception e) {
        // TODO: handle exception
    }
}

public static boolean deleteDir(File dir) {
    if (dir != null && dir.isDirectory()) {
        String[] children = dir.list();
        for (int i = 0; i < children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
                return false;
            }
        }
    }

    // The directory is now empty so delete it
    return dir.delete();
}

}

