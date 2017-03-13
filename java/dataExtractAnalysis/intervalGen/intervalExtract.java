import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalExtract {

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
          String outFilename = "discrete_summaries/" + database + "_code_" + outCode + "_interval_" + interval + ".csv";
          File outFile = new File(outFilename);
          writer = new BufferedWriter(new FileWriter(outFile));

          while (rs2.next()) {
            count++;
            String refUrl = rs2.getString("url");
            String vsim = rs2.getString("vsim");
            if (vsim.equals("nan") || vsim.equals("failed") || vsim.equals("incomparable")) { vsim = "?"; }
            String sim = rs2.getString("sim");
            if (sim.equals("nan") || sim.equals("failed") || sim.equals("incomparable")) { sim = "?"; }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype = "?"; }
            String laststatus = rs2.getString("laststatus");
            if (laststatus.equals("")) { laststatus = "?"; }
            String size = rs2.getString("size");
            if (size.equals("")) { size = "?"; }
            String tld = rs2.getString("tld");
            // if (!(rs2.wasNull())) { 
            // if ((vsim != "") && (vsim != null) && (vsim != " ")) {
            if (!(vsim.length()==0)) {
              if (outCode.equals("1")) {
                System.out.println(refUrl + ", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + tld);
                writer.write(refUrl + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + tld + "\n");
              }
              if (outCode.equals("2")) {
                // show url, interval, vsim and size
                System.out.println(refUrl + ", " + interval + ", " + vsim + ", " + size);
                writer.write(refUrl + ", " + vsim + ", " + size + "\n");
              }
              if (outCode.equals("3")) {
                // show url, interval, laststatus and size
                System.out.println(refUrl + ", " + interval + ", " + laststatus + ", " + size);
                writer.write(refUrl + ", " + laststatus + ", " + size + "\n");
              }
              if (outCode.equals("4")) {
                // show url, interval, laststatus and vsim
                System.out.println(refUrl + ", " + interval + ", " + laststatus + ", " + vsim );
                writer.write(refUrl + ", " + laststatus + ", " + vsim + "\n");
              }
              if (outCode.equals("5")) {
                // show url, interval, laststatus and tld
                System.out.println(refUrl + ", " + interval + ", " + laststatus + ", " + tld);
                writer.write(refUrl + ", " + laststatus + ", " + tld + "\n");
              }
              if (outCode.equals("6")) {
                // show url, interval, size and tld
                System.out.println(refUrl + ", " + interval + ", " + size + ", " + tld);
                writer.write(refUrl + ", " + size + ", " + tld + "\n");
              }
              if (outCode.equals("7")) {
                // show url, interval, vsim and tld
                writer.write(refUrl + ", " + vsim + ", " + tld + "\n");
                System.out.println(refUrl + ", " + interval + ", " + vsim + ", " + tld);
              }

            }
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
