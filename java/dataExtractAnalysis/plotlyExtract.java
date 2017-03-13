import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class plotlyExtract {
// java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar plotlyExtract fwd_plos2 vsim > plos_vsim_status_sankey.json

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String fieldOfInterest = args[1];
    String intervalOfInterest = args[2];
    // 1 is counts, 2 is percent matrix, 3 is a column name
    // Interval, 2xx, 3xx 4xx, 5xx, none
    // 0,0.882279,0.015394,0.0971719,0.0020897,0.01650878
    System.out.println("var data = [");
    System.out.println("  {");

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

        System.out.println("  x:");
        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        
        while (rs.next()) {
          String thisInterval = rs.getString("retrieve_interval");
          if (thisInterval.equals(intervalOfInterest)) {
          String nestedQuery = "select * from " + database + " where retrieve_interval = '" + thisInterval + "' order by url";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            if (thisValue.equals("") || thisValue.equals(null) || (thisValue.equals("nan") || (thisValue.equals("failed") || (thisValue.equals("incomparable"))))) {
              thisValue = "0";
            }
            int thisValueNumber = Math.round(Float.valueOf(thisValue));
            System.out.print(thisValueNumber + ",");
           
            if ((thisValueNumber>90) && (thisValueNumber<=100)) {
              thisValue = "91-100";
            }
            if ((thisValueNumber>80) && (thisValueNumber<=90)) {
              thisValue = "81-90";
            }
            if ((thisValueNumber>70) && (thisValueNumber<=80)) {
              thisValue = "71-80";
            }
            if ((thisValueNumber>60) && (thisValueNumber<=70)) {
              thisValue = "61-70";
            }
            if ((thisValueNumber>50) && (thisValueNumber<=60)) {
              thisValue = "51-60";
            }
            if ((thisValueNumber>40) && (thisValueNumber<=50)) {
              thisValue = "41-50";
            }
            if ((thisValueNumber>30) && (thisValueNumber<=40)) {
              thisValue = "31-40";
            }
            if ((thisValueNumber>20) && (thisValueNumber<=30)) {
              thisValue = "21-30";
            }
            if ((thisValueNumber>10) && (thisValueNumber<=20)) {
              thisValue = "11-20";
            }
            if ((thisValueNumber>=1) && (thisValueNumber<=10)) {
              thisValue = "1-10";
            }
            if (thisValueNumber==0) {
              thisValue = "0";
            }

            String hashIndex = thisInterval + "," + thisValue;
            try {
                int typeValCounter = linkIntervalCount.get(hashIndex);
                typeValCounter++;
                linkIntervalCount.put(hashIndex, typeValCounter);
            } catch (Exception e) {
                linkIntervalCount.put(hashIndex, 1);
            }
          }
          rs2.close();
        }
        }
        rs.close();

        for (String key: linkIntervalCount.keySet()) {
          try {
            // System.out.println(key + "," + linkIntervalCount.get(key));
          } catch(Exception e) { System.out.println("exception " + e); }
        }

      System.out.println();
      System.out.println("  type: 'histogram'");

      System.out.println("  }");
      System.out.println("];");

      // writer.close();

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
