import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalTldCustom {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar intervalTld fwd_plos2 1 laststatus

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  // private static String db_password = "remember"; 
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String outCode = args[1];
    String fieldOfInterest = args[2];
    // 1 is counts, 2 is percent matrix, 3 is a column name
    // Interval, 2xx, 3xx 4xx, 5xx, none
    // 0,0.882279,0.015394,0.0971719,0.0020897,0.01650878

    try { 
        Class.forName(db_driver);
        String url = db_url;
        // Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        BufferedWriter writer = null;
        String outFilename = "summarized/" + database + ".csv";
        File outFile = new File(outFilename);
        // writer = new BufferedWriter(new FileWriter(outFile));

        // output data column headers
        // writer.write("Interval, 2xx, 3xx 4xx, 5xx, none\n");

        Map<String,Integer> linkIntervalCount = new HashMap<String,Integer>();
        ArrayList<String> Vertices = new ArrayList<String>();

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        // rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        rs = stmt.executeQuery("select distinct(tld) from " + database);
        while (rs.next()) {
          String thisTld = rs.getString("tld");
          String nestedQuery = "select * from " + database + " where tld = '" + thisTld + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          String lastValue = "";
          String lastInterval = "";
          String lastTld = "";
          while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            String thisInterval = rs2.getString("retrieve_interval");
            if (thisValue.equals("") || thisValue.equals(null)) {
              thisValue = "none";
            } else {
              thisValue = thisValue.substring(0,1) + "xx";
            }
            if (!(lastValue.equals("")) && (!(lastInterval.equals(""))) && (thisValue.equals("4xx"))) {
              String hashIndex = "{\"source\":\"" + thisTld + "\",\"target\":\"" + thisValue + "\"";
              // String hashIndex = "{\"source\":\"" + lastInterval + ","+lastTld+"," + lastValue + "\",\"target\":\"" + thisInterval + "," + thisTld + "," + thisValue + "\"";

            try {
              int typeValCounter = linkIntervalCount.get(hashIndex);
              typeValCounter++;
              linkIntervalCount.put(hashIndex, typeValCounter);
              // System.out.println(typeValCounter + "},");
            } catch (Exception e) {
              linkIntervalCount.put(hashIndex, 1);
              // System.out.println(1 + "},");
            }
            }
            lastValue = thisValue;
            lastInterval = thisInterval;
            lastTld = thisTld;
          }
          rs2.close();
        }
        rs.close();


         if (outCode.equals("1")) {

           for (String key: linkIntervalCount.keySet()) {
             if (linkIntervalCount.get(key)>10) {
               // {"source":"3,gov,9xx","target":"3,gov,9xx""},
               String edgeVals = key.replace("\"source\":", "");
               edgeVals = edgeVals.replace("\"target\":", "");
               String[] edges = edgeVals.split("\",\"");
               String sourceLink = edges[0].replace("{\"", "");
               String targetLink = edges[1].replace("\"", "");
               if (!(Vertices.contains(sourceLink))) {
                 Vertices.add(sourceLink);
               }
               if (!(Vertices.contains(targetLink))) {
                 Vertices.add(targetLink);
               }
             }
           }

           System.out.println("contents of csv");
           System.out.println("name,color");
           for (String item: Vertices) {
             System.out.println(item + ",#" + randomHex());
           }

           System.out.println("contents of json");
           System.out.print("[");
           for (String row: Vertices) {
             System.out.print("[");
             for (String col: Vertices) {
               String thisIndex = "{\"source\":\"" + row + "\",\"target\":\"" + col + "\"";
               try {
                 int thisIndexVal = linkIntervalCount.get(thisIndex);
                 System.out.print(thisIndexVal +",");
               } catch(Exception e) { 
                 String reverseIndex = "{\"source\":\"" + col + "\",\"target\":\"" + row + "\"";
                 try {
                   int reverseIndexVal = linkIntervalCount.get(reverseIndex);
                   System.out.print(reverseIndexVal +",");
                 } catch(Exception ef) {
                   System.out.print("0,");
                 }
               }
             }
             System.out.print("], ");
           }
      }
      System.out.println("]");

      // writer.close();

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
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

  public static String randomHex() {
     Random randomService = new Random();
     int RANDOM_HEX_LENGTH = 6;
     StringBuilder sb = new StringBuilder();
     while (sb.length() < RANDOM_HEX_LENGTH) {
       sb.append(Integer.toHexString(randomService.nextInt()));
     }
     sb.setLength(RANDOM_HEX_LENGTH);
     return(sb.toString());
  }

}
