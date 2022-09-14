package utils;

import java.io.*;
import java.util.Scanner;
import java.util.StringJoiner;

public class IOUtils {
    /**
     * read from testfile.txt
     */
    public static String read(String filename) throws IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(filename));
        Scanner scanner = new Scanner(in);
        StringJoiner stringJoiner = new StringJoiner("\n");
        while (scanner.hasNextLine()) {
            stringJoiner.add(scanner.nextLine());
        }
        scanner.close();
        in.close();
        return stringJoiner.toString();
    }

    /**
     * output to output.txt
     */
    public static void write(String content, String filename) {
        File outputFile = new File(filename);
        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
