import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class extractMysql {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {
    try { 
        System.out.println("URL, Interval, VSIM");
        Class.forName(db_driver);
        String url = db_url;
        // Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(url) from fwd_plos2 ");
        while (rs.next()) {
          ResultSet rs2 = null;
          String refUrl = rs.getString("url");
          Statement stmt2 = con.createStatement();
          rs2 = stmt2.executeQuery("select retrieve_interval, vsim from fwd_plos2 where url = '" + refUrl + "'");
          while (rs2.next()) {
            String vsim = rs2.getString("vsim");
            String interval = rs2.getString("retrieve_interval");
            // if (!(rs2.wasNull())) { 
            // if ((vsim != "") && (vsim != null) && (vsim != " ")) {
            if (!(vsim.length()==0)) {
              System.out.println("\"" + refUrl + "\"," + interval + "," + vsim );
            }
          }
          rs2.close();
        }
        rs.close();

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

}

