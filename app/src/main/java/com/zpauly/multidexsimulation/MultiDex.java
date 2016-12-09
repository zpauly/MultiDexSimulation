package com.zpauly.multidexsimulation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by zpauly on 2016/12/9.
 */

public class MultiDex {
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor editor;

    public static void install(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);

            ClassLoader classLoader = context.getClassLoader();

            File dexDir = new File(context.getFilesDir(), Constants.SECONDARY_DEXES_NAME);

            List<File> files = load(context, applicationInfo.sourceDir, dexDir);

            if (FileUtils.checkValidZipFiles(files)) {
                installDexes(classLoader, files, dexDir);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static List<File> load(Context context, String apkFilePath, File dexDir) {
        sharedPreferences = context.getSharedPreferences(Constants.PREFS_FILE, Context.MODE_PRIVATE);

        List<File> files = new ArrayList<>();

        File file = new File(apkFilePath);

        long currentCrc = FileUtils.getZipCrc(file);
        String apkClassesPrefix = file.getName() + Constants.CLASSES_SUFFIX; //apkName.apk.classes

        if (isModified(file, currentCrc)) {
            files = loadExistingFiles(dexDir, apkClassesPrefix);
            return files;
        }
        prepare(dexDir, apkClassesPrefix);

        try {
            ZipFile zipFile = new ZipFile(file);

            int e = 2;

            ZipEntry zipEntry = zipFile.getEntry(Constants.CLASSES_PREFIX + e + Constants.DEX_SUFFIX); //classes*.dex
            while (zipEntry != null) {
                String extractFileName = apkClassesPrefix + e + Constants.ZIP_SUFFIX;//apkName.apk.classes*.zip
                File extractFile = new File(dexDir, extractFileName);
                FileUtils.unZip(zipFile, zipEntry, extractFile);
                files.add(extractFile);
                e++;
            }

            storeFiles(FileUtils.getTimeStamp(file), currentCrc, files.size() + 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    private static boolean isModified(File file, long crc) {
        return (sharedPreferences.getLong(Constants.KEY_TIME_STAMP, Constants.NO_VALUE) != FileUtils.getTimeStamp(file)
        || sharedPreferences.getLong(Constants.KEY_CRC, Constants.NO_VALUE) != crc);
    }

    private static List<File> loadExistingFiles(File dexDir, String extractPrefix) {
        List<File> files = new ArrayList<>();

        int dexNumber = sharedPreferences.getInt(Constants.KEY_DEX_NUMBER, 1);

        for (int i = 2; i < dexNumber; i ++) {
            File file = new File(dexDir, extractPrefix + i + Constants.ZIP_SUFFIX);
            if (file.isFile()) {
                files.add(file);
            }
        }

        return files;
    }

    private static void storeFiles(long timeStamp, long currentCrc, int totalDexNumber) {
        editor = sharedPreferences.edit();
        editor.putLong(Constants.KEY_TIME_STAMP, timeStamp);
        editor.putLong(Constants.KEY_CRC, currentCrc);
        editor.putInt(Constants.KEY_DEX_NUMBER, totalDexNumber);
        editor.apply();
    }

    private static void prepare(File dexDir, final String prefix) {
        if (!dexDir.isDirectory()) {
            return;
        }
        if (!dexDir.exists()) {
            dexDir.mkdirs();
            return;
        }
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                if (file.getName().startsWith(prefix)) {
                    return true;
                }
                return false;
            }
        };
        for (File file : dexDir.listFiles(filter)) {
            file.delete();
        }
    }

    private static void installDexes(ClassLoader loader, List<File> dexFiles, File dexDir) {
        try {
            Field pathListField = ClassUtils.getField(loader, "pathList");
            Object pathList = pathListField.get(pathListField);

            ArrayList<IOException> suppressedExceptions = new ArrayList<>();
            Method makeDexElementsMethod = ClassUtils.getMethod(pathList,
                    "makeDexElements",
                    ArrayList.class, File.class, ArrayList.class);
            Object[] extractedElements = (Object[]) makeDexElementsMethod.invoke(pathList, dexFiles, dexDir, suppressedExceptions);

            Field dexElementsField = ClassUtils.getField(pathList, "dexElements");
            Object[] originalElements = (Object[]) dexElementsField.get(pathList);

            Object[] newElements = (Object[]) Array.newInstance(originalElements.getClass().getComponentType(),
                    originalElements.length + extractedElements.length);
            System.arraycopy(originalElements, 0, newElements, 0, originalElements.length);
            System.arraycopy(extractedElements, 0, newElements, originalElements.length, newElements.length);

            dexElementsField.set(pathList, newElements);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
