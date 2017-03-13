import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class extractMysqlArffTrain {
// java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar extractMysqlArffTrain fwd_arxiv2

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {

    String database = args[0];
    System.out.println("@relation " + database);
    System.out.println();
    int count = 0;

    try { 
        System.out.println("@attribute url string");
        System.out.println("@attribute interval numeric");
        System.out.println("@attribute htype { *, application/json, application/msword, application/octet-stream, application/pdf, application/postscript, application/rar, application/rdf+xml, application/rss+xml, application/unknown, application/vnd.google-earth.kmz, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-dvi, application/x-gzip, application/x-rar, application/x-rar-compressed, application/x-tar, application/x-troff-man, application/xhtml+xml, application/xml, application/zip, Content-Type:text/html, image/jpeg, image/png, image/svg+xml, text/csv, text/html, text/plain, text/turtle, text/vnd.wap.wml, text/x-csrc, text/x-server-parsed-html, text/xml, video/mp4, video/quicktime, video/x-msvideo }");
        System.out.println("@attribute laststatus {200, 203, 301, 302, 400, 401, 403, 404, 405, 406, 408, 409, 410, 416, 429, 490, 500, 501, 502, 503, 504}");
        System.out.println("@attribute vsim numeric");
        System.out.println("@attribute sim numeric");
        System.out.println("@attribute size numeric");
        System.out.println("@attribute sizechange numeric");
        System.out.println("@attribute drifted {yes, no}");
        System.out.println("@data");
        System.out.println();

        Class.forName(db_driver);
        String url = db_url;
        // Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(url) from " + database );
        while (rs.next()) {
          ResultSet rs2 = null;
          String refUrl = rs.getString("url");
          Statement stmt2 = con.createStatement();
          rs2 = stmt2.executeQuery("select retrieve_interval, vsim, sim, htype, laststatus, size, size_change from " + database + " where url = '" + refUrl + "'");
          while (rs2.next()) {
            count++;
            String vsim = rs2.getString("vsim");
            if (vsim.equals("nan")) { vsim = "0"; }
            if (vsim.equals("")) { vsim = "?"; }
            String interval = rs2.getString("retrieve_interval");
            String sim = rs2.getString("sim");
            if (sim.equals("nan") || sim.equals("failed") || sim.equals("incomparable") || (sim.equals(""))) { sim = "?"; }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype = "?"; }
            String laststatus = rs2.getString("laststatus");
            if (laststatus.equals("")) { laststatus = "?"; }
            String size = rs2.getString("size");
            if (size.equals("")) { size = "?"; }
            String sizechange = rs2.getString("size_change");
            Random rand = new Random();
            int  n = rand.nextInt(5) + 1;
            // if (n==5) {
              if (interval.equals("0") || (Float.valueOf(vsim)>=80)) {
              System.out.println("\"" + refUrl + "\", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + sizechange + ", no");
              } else 
              if (Float.valueOf(vsim)<80) {
                System.out.println("\"" + refUrl + "\", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + sizechange + ", yes");
              } else 
              if ((Float.valueOf(sizechange)>10) || (Float.valueOf(sizechange)<-10)) {
                System.out.println("\"" + refUrl + "\", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + sizechange +  ", yes");
              } else 
                System.out.println("\"" + refUrl + "\", " + interval + ", " + htype + ", " + laststatus + ", " + vsim + ", " + sim +", " + size + ", " + sizechange +  ", no");
            // }
          } 
          rs2.close();
        }
        rs.close();

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
    System.out.println("% Number of rows converted to instances " + count);
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

