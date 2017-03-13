import java.util.*;
import java.io.*;

public class calculateKLD {
  public static void main(String args[]) {
  try {
    String filename = args[0];
    String filename2 = args[1];
    File file = new File(filename);
    File file2 = new File(filename2);
    InputStream in = new FileInputStream(file);
    InputStream in2 = new FileInputStream(file2);
    String contents = fromStream(in);
    String contents2 = fromStream(in2);
    String[] contentArray = contents.split(" ");
    String[] contentArray2 = contents2.split(" ");
    List<String> contentList = Arrays.asList(contentArray);
    List<String> contentList2 = Arrays.asList(contentArray2);

    List<String> list = new ArrayList<String>();
    List<String> list2 = new ArrayList<String>();
    list.add("Free"); list.add("Ringtones");  
    // list.add("The"); list.add("Quick"); list.add("Brown"); list.add("Phone"); list.add("Fox"); 

    list2.add("Free");list2.add("Ringtones");list2.add("for");list2.add("your");list2.add("Mobile");list2.add("Phone");list2.add("from");list2.add("PremieRingtones.com");

    System.out.println(calculateKLD(list, list2));
    System.out.println(calculateKLD(contentList, contentList2));
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

  public static Double calculateKLD(List<String> values,List<String> value2) {

    Map<String, Integer> map = new HashMap<String, Integer>();  
    Map<String, Integer> map2 = new HashMap<String, Integer>();  
    for (String sequence : values)  
    {  
        if (!map.containsKey(sequence))  
        {  
            map.put(sequence, 0);
        }
        map.put(sequence, map.get(sequence) + 1);
    }

    for (String sequence : value2)  
    {  
        if (!map2.containsKey(sequence)) {
            map2.put(sequence, 0);
        }
        map2.put(sequence, map2.get(sequence) + 1);
    }

    Double result = 0.0;
    Double frequency2=0.0;
    for (String sequence : map.keySet())  
    {

        Double frequency1 = (double) map.get(sequence) / values.size();
        System.out.println("Frequency1 "+ " " + sequence + " " + frequency1.toString());
        if(map2.containsKey(sequence))
        {

            frequency2 = (double) map2.get(sequence) / value2.size();                
        }
        result += frequency1 * (Math.log(frequency1/frequency2) / Math.log(2));         
    }  
    // return result/2.4;  
    // return result/2.718281828;  
    return result;
  }    

}
