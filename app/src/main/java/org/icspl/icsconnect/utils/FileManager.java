package org.icspl.icsconnect.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.icspl.icsconnect.R;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by ABC on 2/3/2016.
 */
public class FileManager {
    public static final String DOCUMENTS_DIR = "documents";
    public static String getFilePath(Context context, String file_name) {
        String file_path = "";
        try {
            File download_dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File[] downloaded_files = download_dir.listFiles();
            for (File f : downloaded_files) {
                if (f.isFile()) {
                    if (f.getName().equalsIgnoreCase(file_name)) {
                        return f.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return file_path;
    }

    public static void openFile(Context context, File url) throws IOException {
        try {

            // Create URI
            File file = url;
            Uri uri = Uri.fromFile(file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            // Check what kind of file you are trying to open, by comparing the url with extensions.
            // When the if condition is matched, plugin sets the correct intent (mime) type,
            // so Android knew what application to use to open the file

            String file_extension = url.toString().toLowerCase();

            if (file_extension.contains(".doc") || file_extension.contains(".docx")) {
                // Word document
                intent.setDataAndType(uri, "application/msword");
            } else if (file_extension.contains(".pdf")) {
                // PDF file
                intent.setDataAndType(uri, "application/pdf");
            } else if (file_extension.contains(".ppt") || file_extension.contains(".pptx")) {
                // Powerpoint file
                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (file_extension.contains(".xls") || file_extension.contains(".xlsx")) {
                // Excel file
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (file_extension.contains(".zip") || file_extension.contains(".rar")) {
                intent.setDataAndType(uri, "application/zip");
            } else if (file_extension.contains(".wav") || file_extension.contains(".mp3") || file_extension.contains(".m4a")) {
                // WAV audio file
                intent.setDataAndType(uri, "audio/x-wav");
            } else if (file_extension.contains(".gif")) {
                // GIF file
                intent.setDataAndType(uri, "image/gif");
            } else if (file_extension.contains(".jpg") || file_extension.contains(".jpeg") || file_extension.contains(".png")) {
                // JPG file
                intent.setDataAndType(uri, "image/jpeg");
            } else if (file_extension.contains(".txt")) {
                // Text file
                intent.setDataAndType(uri, "text/plain");
            } else if (file_extension.contains(".3gp") || file_extension.contains(".mpg") || file_extension.contains(".mpeg") || file_extension.contains(".mpe") || file_extension.contains(".mp4") || file_extension.contains(".avi")) {
                // Video files
                intent.setDataAndType(uri, "video/*");
            } else {
                throw new Exception("No preview available.");
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {

            //Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Toast.makeText(context, "No such App to open File", Toast.LENGTH_SHORT).show();
        }

    }

    public static String getPath(final Context context, final Uri uri) {

        //check here to KITKAT or new version
        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                // new added
                if (id != null && id.startsWith("raw:")) {
                    return id.substring(4);
                }

                String[] contentUriPrefixesToTry = new String[]{
                        "content://downloads/public_downloads",
                        "content://downloads/my_downloads",
                        "content://downloads/all_downloads"
                };

                for (String contentUriPrefix : contentUriPrefixesToTry) {
                    Uri contentUri = ContentUris.withAppendedId(Uri.parse(contentUriPrefix), Long.valueOf(id));
                    try {
                        String path = getDataColumn(context, contentUri, null, null);
                        if (path != null) {
                            return path;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
//
                // path could not be retrieved using ContentResolver, therefore copy file to accessible cache using streams
                String fileName = getFileName(context, uri);
                File cacheDir = getDocumentCacheDir(context);
                File file = generateFileName(fileName, cacheDir);
                String destinationPath = null;
                if (file != null) {
                    destinationPath = file.getAbsolutePath();
                    saveFileFromUri(context, uri, destinationPath);
                }

                return destinationPath;
            }
//


            //

             /*   final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
*/


            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }


        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    public static String getIconString(Context context, String filename) {
        String icon_value = "";
        String file_extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length());
        if (file_extension.equalsIgnoreCase("jpg")
                || file_extension.equalsIgnoreCase("jpeg")
                || file_extension.equalsIgnoreCase("gif")
                || file_extension.equalsIgnoreCase("png")) {
            icon_value = context.getResources().getString(R.string.fa_image);
        } else if (file_extension.equalsIgnoreCase("xls")
                || file_extension.equalsIgnoreCase("xlsx")) {
            icon_value = context.getResources().getString(R.string.fa_excel);
        } else if (file_extension.equalsIgnoreCase("doc")
                || file_extension.equalsIgnoreCase("docx")) {
            icon_value = context.getResources().getString(R.string.fa_word);
        } else if (file_extension.equalsIgnoreCase("pdf")) {
            icon_value = context.getResources().getString(R.string.fa_pdf);
        } else if (file_extension.equalsIgnoreCase("mp4")
                || file_extension.equalsIgnoreCase("3gp")
                || file_extension.equalsIgnoreCase("mkv")
                || file_extension.equalsIgnoreCase("mpeg")
                || file_extension.equalsIgnoreCase("mpg")
                || file_extension.equalsIgnoreCase("avi")) {
            icon_value = context.getResources().getString(R.string.fa_video);
        } else if (file_extension.equalsIgnoreCase("ppt") ||
                file_extension.equalsIgnoreCase("pptx")) {
            icon_value = context.getResources().getString(R.string.fa_ppt);
        } else if (file_extension.equalsIgnoreCase("rar")
                || file_extension.equalsIgnoreCase("zip")) {
            icon_value = context.getResources().getString(R.string.fa_zip);
        } else if (file_extension.equalsIgnoreCase("mp3")) {
            icon_value = context.getResources().getString(R.string.fa_audio);
        } else if (file_extension.equalsIgnoreCase("txt")) {
            icon_value = context.getResources().getString(R.string.fa_text);
        }
        return icon_value;
    }

    public static boolean isImageFile(String file) {
        boolean flag = false;
        try {
            String extension = file.substring(file.lastIndexOf(".") + 1, file.length());
            if (extension.equalsIgnoreCase("jpg")
                    || extension.equalsIgnoreCase("jpeg")
                    || extension.equalsIgnoreCase("png"))
                flag = true;
        } catch (Exception e) {

        }
        return flag;
    }


    public static String getFileName(@NonNull Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        String filename = null;

        if (mimeType == null && context != null) {
            String path = getPath(context, uri);
            if (path == null) {
                filename = getName(uri.toString());
            } else {
                File file = new File(path);
                filename = file.getName();
            }
        } else {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }

        return filename;
    }

    public static String getName(String filename) {
        if (filename == null) {
            return null;
        }
        int index = filename.lastIndexOf('/');
        return filename.substring(index + 1);
    }

    public static File getDocumentCacheDir(@NonNull Context context) {
        File dir = new File(context.getCacheDir(), DOCUMENTS_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
//        logDir(context.getCacheDir());
//        logDir(dir);

        return dir;
    }

    @Nullable
    public static File generateFileName(@Nullable String name, File directory) {
        if (name == null) {
            return null;
        }

        File file = new File(directory, name);

        if (file.exists()) {
            String fileName = name;
            String extension = "";
            int dotIndex = name.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = name.substring(0, dotIndex);
                extension = name.substring(dotIndex);
            }

            int index = 0;

            while (file.exists()) {
                index++;
                name = fileName + '(' + index + ')' + extension;
                file = new File(directory, name);
            }
        }

        try {
            if (!file.createNewFile()) {
                return null;
            }
        } catch (IOException e) {
            //Log.w(TAG, e);
            return null;
        }

        //logDir(directory);

        return file;
    }

    private static void saveFileFromUri(Context context, Uri uri, String destinationPath) {
        InputStream is = null;
        BufferedOutputStream bos = null;
        try {
            is = context.getContentResolver().openInputStream(uri);
            bos = new BufferedOutputStream(new FileOutputStream(destinationPath, false));
            byte[] buf = new byte[1024];
            is.read(buf);
            do {
                bos.write(buf);
            } while (is.read(buf) != -1);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
                if (bos != null) bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }
}
