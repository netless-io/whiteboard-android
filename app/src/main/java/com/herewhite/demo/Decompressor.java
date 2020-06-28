package com.herewhite.demo;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Decompressor {
    private String zipPath;
    private String targetPath;

    public Decompressor(String zipPath, String targetPath) {
        this.zipPath = zipPath;
        if (!targetPath.endsWith("/")) {
            this.targetPath = targetPath + "/";
        } else {
            this.targetPath = targetPath;
        }
        _dirChecker("");
    }

    public void unzip() {
        try  {
            FileInputStream fin = new FileInputStream(zipPath);
            ZipInputStream zin = new ZipInputStream(fin);
            ZipEntry ze = null;
            while ((ze = zin.getNextEntry()) != null) {
                Log.v("Decompress", "Unzipping " + ze.getName());

                if(ze.isDirectory()) {
                    _dirChecker(ze.getName());
                } else {
                    File target = new File(targetPath + ze.getName()).getParentFile();
                    if (!target.exists()) {
                        target.mkdirs();
                    }
                    FileOutputStream fout = new FileOutputStream(targetPath + ze.getName());
                    for (int c = zin.read(); c != -1; c = zin.read()) {
                        fout.write(c);
                    }

                    zin.closeEntry();
                    fout.close();
                }

            }
            zin.close();
        } catch(Exception e) {
            Log.e("Decompress", "unzip", e);
        }

    }

    private void _dirChecker(String dir) {
        File f = new File(targetPath + dir);

        if(!f.isDirectory()) {
            f.mkdirs();
        }
    }

}
