import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalStep {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = "remember"; 
  // private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String outCode = args[1];
    String fieldOfInterest = args[2];
    String stepField = args[3];
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

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        // rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        rs = stmt.executeQuery("select distinct(url) from " + database);
        while (rs.next()) {
          String thisUrl = rs.getString("url");
          String nestedQuery = "select * from " + database + " where url = '" + thisUrl + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          String lastValue = "";
          String lastInterval = "";
          String lastUrl = "";
          while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            String thisInterval = rs2.getString("retrieve_interval");
            String statusChain = rs2.getString("status");
            if (thisValue.equals("") || thisValue.equals(null)) {
              thisValue = "none";
            }
            System.out.println(thisInterval + "," + thisUrl + "," + lastValue + "," + thisValue + ",\"" + statusChain + "\"");
            lastValue = thisValue;
            lastValue = rs.getString(stepField);
            lastInterval = thisInterval;
          }
          rs2.close();
        }
        rs.close();


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
