import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalExtractSum {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String outCode = args[1];

    try { 
        Class.forName(db_driver);
        String url = db_url;
        // Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        while (rs.next()) {
          ResultSet rs2 = null;
          String interval = rs.getString("retrieve_interval");
          Statement stmt2 = con.createStatement();
          rs2 = stmt2.executeQuery("select * from " + database + " where retrieve_interval = '" + interval + "'");

          BufferedWriter writer = null;
          String outFilename = "discrete_summaries/circos_" + database + "_code_" + outCode + "_interval_" + interval + ".csv";
          File outFile = new File(outFilename);
          writer = new BufferedWriter(new FileWriter(outFile));

              if (outCode.equals("1")) {
                writer.write("labels htype laststatus vsim sim size tld\n");
              }
              if (outCode.equals("2")) {
                // show url, interval, vsim and size
                writer.write("labels vsim size\n");
              }
              if (outCode.equals("3")) {
                // show url, interval, laststatus and size
                writer.write("labels laststatus size\n");
              }
              if (outCode.equals("4")) {
                // show url, interval, laststatus and vsim
                writer.write("labels laststatus vsim\n");
              }
              if (outCode.equals("5")) {
                // show url, interval, laststatus and tld
                writer.write("labels laststatus tld\n");
              }
              if (outCode.equals("6")) {
                // show url, interval, size and tld
                writer.write("labels size tld\n");
              }
              if (outCode.equals("7")) {
                // show url, interval, vsim and tld
                writer.write("labels vsim tld\n");
              }


          Map<String,String> cols = new HashMap<String,String>();
          Map<String,Integer> typeCount = new HashMap<String,Integer>();

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

            if (outCode.equals("2")) {
                   // show id, vsim and size
                   Integer vsimVal = (int) Math.round(Double.valueOf(vsim));
                   cols.put(hashIndex, Integer.toString(vsimVal));
                   Integer sizeVal = (int) Math.round(Double.valueOf(size));
                   typeCount.put(hashIndex, sizeVal);
              }
              if (outCode.equals("3")) {
                   Integer lcount = (int) Math.round(Double.valueOf(cols.get(size)));
                   cols.put(hashIndex, laststatus);
                   typeCount.put(hashIndex, lcount);
                // show url, interval, laststatus and size
              }
              if (outCode.equals("4")) {
                // show id, laststatus, vsim
                   Integer lcount = (int) Math.round(Double.valueOf(cols.get(laststatus)));
                   cols.put(hashIndex, laststatus);
                   typeCount.put(hashIndex, (int) Math.round(Double.valueOf(vsim)));
              }
              if (outCode.equals("5")) {
                try {
                   Integer lcount = (int) Math.round(Double.valueOf(cols.get(vsim)));
                   lcount += (int) Math.round(Double.valueOf(size));
                   cols.put(vsim, Integer.toString(lcount));
                   int tcount = typeCount.get(vsim);
                   tcount += 1;
                   typeCount.put(vsim, tcount);
                } catch (Exception e) {
                  cols.put(vsim, Integer.toString((int) Math.round(Double.valueOf(size))));
                  typeCount.put(vsim, 1);
                  // show url, interval, vsim and size
                }

                // show url, interval, laststatus and tld
              }
              if (outCode.equals("6")) {
                // show url, interval, size and tld
                   cols.put(hashIndex, tld);
                   typeCount.put(hashIndex, (int) Math.round(Double.valueOf(size)));
              }
              if (outCode.equals("7")) {
                // show url, interval, vsim and tld
                   cols.put(hashIndex, tld);
                   typeCount.put(hashIndex, (int) Math.round(Double.valueOf(vsim)));
              }


         }

         for (String key: cols.keySet()) {
                // writer.write(key + " " + cols.get(key) + "\n");
                writer.write(key + " " + cols.get(key) + " " + typeCount.get(key) + "\n");
          }
          writer.close();
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
