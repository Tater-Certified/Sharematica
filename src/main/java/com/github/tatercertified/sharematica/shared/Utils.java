package com.github.tatercertified.sharematica.shared;

import com.github.tatercertified.sharematica.Sharematica;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Utils {
    public static Path path = Path.of(FabricLoader.getInstance().getGameDir() + "/schematics/sharematica");
    public static Path server_path = Path.of(FabricLoader.getInstance().getGameDir() + "/server_schematics");
    public static void generateSharematicaFolder() {
        if (Objects.equals(Sharematica.ENVIRONMENT, "client")) {
            if (Files.notExists(path)) {
                try {
                    Files.createDirectories(path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            if (Files.notExists(server_path)) {
                try {
                    Files.createDirectories(server_path);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }



    public static byte[] compress(File input) throws IOException {

        // Create a FileInputStream for the input file
        FileInputStream fileInputStream = new FileInputStream(input);

        // Create a ByteArrayOutputStream to hold the compressed data
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        // Create a GZIPOutputStream to compress the data and write it to the ByteArrayOutputStream
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream);

        // Read the input data and write the compressed data to the GZIPOutputStream
        byte[] buffer = new byte[1024];
        int len;
        while ((len = fileInputStream.read(buffer)) > 0) {
            gzipOutputStream.write(buffer, 0, len);
        }

        // Close the streams
        gzipOutputStream.close();
        fileInputStream.close();

        // Get the compressed data as a byte array
        return byteArrayOutputStream.toByteArray();
    }

    public static void decompress(byte[] input, Path path, String name) throws IOException {

        // Create a ByteArrayInputStream for the compressed data
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(input);

        // Create a GZIPInputStream to decompress the data
        GZIPInputStream gzipInputStream = new GZIPInputStream(byteArrayInputStream);

        // Create a FileOutputStream for the output file
        File outputFile = new File(path + "/" + name + ".litematic");
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);

        // Read the decompressed data and write it to the output file
        byte[] buffer = new byte[1024];
        int len;
        while ((len = gzipInputStream.read(buffer)) > 0) {
            fileOutputStream.write(buffer, 0, len);
        }

        // Close the streams
        gzipInputStream.close();
        fileOutputStream.close();
    }
}
