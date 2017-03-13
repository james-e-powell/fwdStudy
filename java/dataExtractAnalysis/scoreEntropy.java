import java.util.*;
import java.io.*;

public class scoreEntropy {
// mysql> alter table fwd_arxiv2 add column entropy float;

  public static void main(String args[]) {
  try {

    String originalFilename = args[0];
    String copyFilename = args[1];

    String test1 = "abcdefghijklmnop";
    System.out.println(ShannonEntropy(test1));
    String test2 = "Hello, World!";
    System.out.println(ShannonEntropy(test2));

    File originalFile = new File(originalFilename);
    File copyFile = new File(copyFilename);

    System.out.println("Entropy score for " + originalFilename);
    InputStream in = new FileInputStream(originalFile);
    String contents = fromStream(in);
    System.out.println(ShannonEntropy(contents));

    System.out.println("Entropy score for " + copyFilename);
    InputStream in2 = new FileInputStream(copyFile);
    String contents2 = fromStream(in2);
    System.out.println(ShannonEntropy(contents2));

  } catch (Exception e) {}


  }
  public static double ShannonEntropy(String s)
  {
    Map<Character,Integer> map = new HashMap<Character,Integer>();
    for (int count=0; count<s.length(); count++)
    {
        char c = s.charAt(count);
        if (!map.containsKey(s.charAt(count))) {
            map.put(s.charAt(count), 1);
        } else {
            int newVal = map.get(c) + 1;
            map.put(c, newVal);
        }
    }

    double result = 0.0;
    int len = s.length();
    for (Map.Entry<Character,Integer> entry: map.entrySet()) 
    {
        double frequency = (double)entry.getValue() / len;
        result -= frequency * (Math.log(frequency) / Math.log(2));
    }

    return result;
  }

  public static String fromStream(InputStream in) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
    StringBuilder out = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    String line;
    while ((line = reader.readLine()) != null) {
        out.append(line);
        out.append(newLine);
    }
    return out.toString();
  }
}
