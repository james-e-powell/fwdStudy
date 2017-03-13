import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalSankeyLog {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  // private static String db_password = "remember"; 
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar intervalSankeyLog fwd_arxiv2 1 laststatus

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
        writer = new BufferedWriter(new FileWriter(outFile));

        // output data column headers
        writer.write("Interval, 2xx, 3xx 4xx, 5xx, none\n");

        Map<String,Integer> linkIntervalCount = new HashMap<String,Integer>();
        ArrayList<String> Vertices = new ArrayList<String>();

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        // rs = stmt.executeQuery("select distinct('" + fieldOfInterest + "') from " + database );
        System.out.println("select distinct('" + fieldOfInterest + "') from " + database);
        while (rs.next()) {
          String interval = rs.getString("retrieve_interval");
          // String fieldVal = rs.getString(fieldOfInterest);
          String nestedQuery = "select distinct(" + fieldOfInterest + ") from " + database + " where retrieve_interval = '" + interval + "' order by " + fieldOfInterest;
          // String nestedQuery = "select *  from " + database + " where  " + fieldOfInterest + "='" + fieldVal + "' order by " + fieldOfInterest;
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            if (thisValue.equals("") || thisValue.equals(null)) {
              thisValue = "none";
            } else {
              thisValue = thisValue.substring(0,1) + "xx";
            }
            Vertices.add(interval + "," + thisValue);
            // Vertices.add(thisValue);
            System.out.println("{\"name\":\"" + interval + "," + thisValue + "\"},");
            writer.write("{\"name\":\"" + interval + "," + thisValue + "\"},\n");
            // System.out.println("{\"name\":\"" + thisValue + "\"},");
          }
          rs2.close();
        }
        rs.close();

        Statement stmt3 = con.createStatement();
        String sqlQuery = "select * from " + database + " order by " + fieldOfInterest + ",url,retrieve_interval";
        ResultSet rs3 = null;
        rs3 = stmt3.executeQuery(sqlQuery);
        while (rs3.next()) {
          String lastUrl = "";
            count++;
            String refUrl = rs3.getString("url");
            String aValue = rs3.getString(fieldOfInterest);
            String interval = rs3.getString("retrieve_interval");
            // String hashIndex = "A" + id;

            if (fieldOfInterest.equals("laststatus")) {
              String statusVal = "";
              if ((aValue.equals("")) || (aValue.equals(null))) {
                statusVal = "none";
              } else {
                statusVal = aValue.substring(0,1) + "xx";
              }
              // if (!(colPrefix.equals("2"))) {
                try {
                  int typeValCounter = linkIntervalCount.get(interval + "," + statusVal);
                  typeValCounter++;
                  linkIntervalCount.put(interval + "," + statusVal, typeValCounter);
                } catch (Exception e) { 
                  linkIntervalCount.put(interval + "," + statusVal, 1);
                }
              // }
            }

            if (fieldOfInterest.equals("tld")) {
                try {
                  int typeValCounter = linkIntervalCount.get(interval + "," + aValue);
                  typeValCounter++;
                  linkIntervalCount.put(interval + "," + aValue, typeValCounter);
                } catch (Exception e) {
                  linkIntervalCount.put(interval + "," + aValue, 1);
                }
           }
        }
        rs3.close();

           String lastKey = "";
           for (String key: linkIntervalCount.keySet()) {
           // for (String key: Vertices) {
             try {
                writer.write(key + " " + linkIntervalCount.get(key) + "\n");
                int minusOne = linkIntervalCount.get(key)-1;
                // System.out.println(key + " " + linkIntervalCount.get(key) + "\n");
             
                // System.out.println("{\"source\":\"" + lastKey + "\",\"target\":\"" + key + "\",\"value\":" + linkIntervalCount.get(key) + "},");
                System.out.println("{\"source\":\"" + lastKey + "\",\"target\":\"" + key + "\",\"value\":" + Math.log(linkIntervalCount.get(key)) + "},");
                writer.write("{\"source\":\"" + lastKey + "\",\"target\":\"" + key + "\", \"value\":" + Math.log(linkIntervalCount.get(key)) + "},\n");

                lastKey = key;
             } catch(Exception e) { System.out.println("exception " + e); }
           }


      writer.close();

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
    System.out.println("Number of rows " + count);
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
