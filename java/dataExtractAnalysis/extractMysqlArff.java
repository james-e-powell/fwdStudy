import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class extractMysqlArff {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {

    System.out.println("@relation fwdstudy_arxiv");
    System.out.println();
    int count = 0;

    try { 
        System.out.println("@attribute url string");
        System.out.println("@attribute interval numeric");
        System.out.println("@attribute htype string");
        System.out.println("@attribute laststatus numeric");
        System.out.println("@attribute vsim numeric");
        System.out.println("@attribute sim numeric");
        System.out.println("@data");
        System.out.println();

        Class.forName(db_driver);
        String url = db_url;
        // Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(url) from fwd_arxiv2 ");
        while (rs.next()) {
          ResultSet rs2 = null;
          String refUrl = rs.getString("url");
          Statement stmt2 = con.createStatement();
          rs2 = stmt2.executeQuery("select retrieve_interval, vsim, sim, htype, laststatus, size from fwd_plos2 where url = '" + refUrl + "'");
          while (rs2.next()) {
            count++;
            String vsim = rs2.getString("vsim");
            if (vsim.equals("nan") || vsim.equals("failed") || vsim.equals("incomparable")) { vsim = "?"; }
            String interval = rs2.getString("retrieve_interval");
            String sim = rs2.getString("sim");
            if (sim.equals("nan") || sim.equals("failed") || sim.equals("incomparable")) { sim = "?"; }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype = "?"; }
            String laststatus = rs2.getString("laststatus");
            if (laststatus.equals("")) { laststatus = "?"; }
            String size = rs2.getString(“size”);
            if (size(“”)) { size = "?"; }
            // if (!(rs2.wasNull())) { 
            // if ((vsim != "") && (vsim != null) && (vsim != " ")) {
            if (!(vsim.length()==0)) {
              System.out.println(refUrl + ", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +”, “ + size);
            }
          }
          rs2.close();
        }
        rs.close();

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
    System.out.println(“Number of rows “ + count);
I
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

