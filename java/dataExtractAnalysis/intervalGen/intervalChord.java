import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalVsimTld {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = "remember"; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String outCode = args[1];
    String threshold = args[2];
    // 1 is counts, 2 is percent matrix
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

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        int rsCount = 0;
        while (rs.next()) {
          Map<String,Integer> typeCount = new HashMap<String,Integer>();
          ResultSet rs2 = null;
          String interval = rs.getString("retrieve_interval");
          Statement stmt2 = con.createStatement();

          String sqlQuery = "select * from " + database + " where retrieve_interval = '" + interval + "' order by laststatus";

          rs2 = stmt2.executeQuery(sqlQuery);

          int rs2Count = 0;
          String lastUrl = "";
          while (rs2.next()) {
            count++;
            String refUrl = rs2.getString("url");
            String id = rs2.getString("id");
            String hashIndex = "A" + id;

            String vsim = rs2.getString("vsim");
            if (vsim.equals("nan") || vsim.equals("failed") || vsim.equals("incomparable") || vsim.equals("")) { vsim = "0"; }
            String sim = rs2.getString("sim");
            if (sim.equals("nan") || sim.equals("failed") || sim.equals("incomparable") || sim.equals("")) { sim = "0"; }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype = "0"; }
            String laststatus = rs2.getString("laststatus");
            if (laststatus.equals("")) { laststatus = "0"; }
            String size = rs2.getString("size");
            if (size.equals("")) { size = "0"; }
            String tld = rs2.getString("tld").toLowerCase();
            if (tld.equals("")) { tld = "null"; }

            String colVal = laststatus;

            // show id, vsim and size
            Integer vsimVal = (int) Math.round(Double.valueOf(vsim));
            Integer sizeVal = (int) Math.round(Double.valueOf(size));
            String colPrefix = colVal.substring(0,1);
            if (!(colPrefix.equals("2"))) {
              try {
                int typeValCounter = typeCount.get(colPrefix + "xx,"+tld);
                typeValCounter++;
                typeCount.put(colPrefix + "xx,"+tld, typeValCounter);
              } catch (Exception e) { 
                typeCount.put(colPrefix + "xx,"+tld, 1);
              }
            }
            rs2Count++;

         }
         rs2.close();

         if (outCode.equals("1")) {
           for (String key: typeCount.keySet()) {
              if (Integer.valueOf(typeCount.get(key))>=Integer.valueOf(threshold)) {
                writer.write(rsCount + " " + key + " " + typeCount.get(key) + "\n");
              }
           }
           writer.write("total " + rs2Count + "\n\n");
         }

         if (outCode.equals("2")) {
           writer.write(rsCount + " ");
           for (String key: typeCount.keySet()) {
             double sizeVal = Double.valueOf(typeCount.get(key)) / rs2Count;
             writer.write("," + sizeVal);
           }
           writer.write("\n");
         }
         rsCount++;

      }
      rs.close();
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
