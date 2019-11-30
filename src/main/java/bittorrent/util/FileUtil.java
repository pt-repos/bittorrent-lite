package bittorrent.util;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

public class FileUtil {

    private static final int CHUNK_SIZE = 100 * 1024; // 100KB

    public static int splitFile(File f) throws IOException {
        int partCounter = 0;

        byte[] buffer = new byte[CHUNK_SIZE];

        String fileName = f.getName();

        //try-with-resources to ensure closing stream
        try (FileInputStream fis = new FileInputStream(f);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            int bytesAmount = 0;
            while ((bytesAmount = bis.read(buffer)) > 0) {
                //write each chunk of data into separate file with different number in name
                String filePartName = String.format("%s.%03d", fileName, partCounter++);
                File newFile = new File(f.getParent(), filePartName);
                try (FileOutputStream out = new FileOutputStream(newFile)) {
                    out.write(buffer, 0, bytesAmount);
                }
            }
        }

        return partCounter;
    }

    public static int splitFile(String filePath) throws IOException {
        return splitFile(new File(filePath));
    }

    public static void mergeFiles(List<File> files, File into)
            throws IOException {
        try (FileOutputStream fos = new FileOutputStream(into);
             BufferedOutputStream mergingStream = new BufferedOutputStream(fos)) {
            for (File f : files) {
                Files.copy(f.toPath(), mergingStream);
            }
        }
    }

    public static List<File> listOfFilesToMerge(File oneOfFiles) {
        String tmpName = oneOfFiles.getName();//{name}.{number}
        String destFileName = tmpName.substring(0, tmpName.lastIndexOf('.'));//remove .{number}

        File[] files = oneOfFiles.getParentFile().listFiles(
                (File dir, String name) -> name.matches(destFileName + "[.]\\d+"));
        Arrays.sort(files);//ensuring order 001, 002, ..., 010, ...
        return Arrays.asList(files);
    }

    public static List<File> listOfFilesToMerge(String oneOfFiles) {
        return listOfFilesToMerge(new File(oneOfFiles));
    }
}
