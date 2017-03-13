import java.util.*;
import java.io.*;

public class calculateEntropy {
  public static void main(String args[]) {
  try {
    String test1 = "abcdefghijklmnop";
    System.out.println(ShannonEntropy(test1));
    String test2 = "Hello, World!";
    System.out.println(ShannonEntropy(test2));
    String filename = args[0];
    File file = new File(filename);
    InputStream in = new FileInputStream(file);
    String contents = fromStream(in);
    System.out.println(ShannonEntropy(contents));
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
