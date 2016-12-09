package com.zpauly.multidexsimulation;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Created by zpauly on 2016/12/9.
 */

public class FileUtils {

    public static void unZip(ZipFile zipFile, ZipEntry zipEntry, File extractFile) throws IOException {
        InputStream in = null;
        ZipOutputStream out = null;
        try {
            in = zipFile.getInputStream(zipEntry);
            out = new ZipOutputStream(
                    new BufferedOutputStream(
                            new FileOutputStream(extractFile)
                    )
            );

            byte[] buffer = new byte[Constants.BUFFER_SIZE];

            int length;
            while ((length = in.read(buffer)) != -1) {
                out.write(buffer, 0, length);
            }

            out.closeEntry();
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
        }
    }

    public static long getTimeStamp(File file) {
        long timeStamp = file.lastModified();
        if (timeStamp == Constants.NO_VALUE) {
            return -- timeStamp;
        }
        return timeStamp;
    }

    public static long getZipCrc(File file) {
        try {
            InputStream in = new FileInputStream(file);
            CRC32 crc = new CRC32();
            int count;
            while ((count = in.read()) != -1) {
                crc.update(count);
            }
            long value = crc.getValue();
            if (value == Constants.NO_VALUE) {
                return -- value;
            }
            return value;
        } catch (IOException e) {
            e.printStackTrace();
            return Constants.NO_VALUE;
        }
    }

    public static boolean checkValidZipFiles(List<File> zipFiles) {
        boolean result = true;
        for (File file : zipFiles) {
            try {
                ZipFile zipFile = new ZipFile(file);
                zipFile.close();
                result = true;
            } catch (IOException e) {
                e.printStackTrace();
                result = false;
                break;
            }
        }
        return result;
    }
}
