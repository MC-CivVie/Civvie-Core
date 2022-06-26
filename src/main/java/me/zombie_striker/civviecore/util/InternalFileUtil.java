package me.zombie_striker.civviecore.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class InternalFileUtil {

    public static List<String> getPathsToInternalFiles(String folder) throws IOException {
        CodeSource src = InternalFileUtil.class.getProtectionDomain().getCodeSource();
        List<String> list = new ArrayList<String>();

        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            ZipEntry ze = null;

            while ((ze = zip.getNextEntry()) != null) {
                String entryName = ze.getName();
                if (entryName.startsWith(folder)) {
                    list.add(entryName);
                }
            }

        }
        return list;
    }

    public static void copyFilesOut(File outputDir, List<String> paths) throws IOException {
        if (!outputDir.exists())
            outputDir.mkdirs();
        for (String s : paths) {
            InputStream is = InternalFileUtil.class.getResourceAsStream(s);
            if(is==null)
                continue;
            String filename = s;
            if (filename.contains("\\")) {
                String[] k = filename.split("\\\\");
                filename = k[k.length - 1];
            }
            if (filename.contains("/")) {
                String[] k = filename.split("/");
                filename = k[k.length - 1];
            }

            File out = new File(outputDir, filename);
            if (out.exists()) {
                continue;
            } else {
                out.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(out);
            int c = 0;
            for (c = is.read(); c != -1; ) {
                fileWriter.write(c);
            }
            fileWriter.flush();
            fileWriter.close();
        }
    }
}
