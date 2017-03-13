import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalExtractCircos {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar intervalExtractCircos fwd_plos2 8

    int count = 0;
    String database = args[0];
    String outCode = args[1];

    try { 
        Class.forName(db_driver);
        String url = db_url;
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
              if (outCode.equals("8")) {
                // show url, interval, vsim and tld
                writer.write("labels vsim sizechange\n");
              }


          String lastUrl = "";
          while (rs2.next()) {
            count++;
            String refUrl = rs2.getString("url");
            String id = rs2.getString("id");

            String vsim = rs2.getString("vsim");
            if (vsim.equals("nan") || vsim.equals("failed") || vsim.equals("incomparable")) { vsim = "0"; }
            String sim = rs2.getString("sim");
            if (sim.equals("nan") || sim.equals("failed") || sim.equals("incomparable")) { sim = "0"; }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype = "0"; }
            String laststatus = rs2.getString("laststatus");
            if (laststatus.equals("")) { laststatus = "0"; }
            String size = rs2.getString("size");
            if (size.equals("")) { size = "0"; }
            String sizechange = rs2.getString("size_change");
            if (sizechange.equals("")) { sizechange = "0"; }
            String tld = rs2.getString("tld");
            if (!(refUrl.equals(lastUrl))) {
            if (!(vsim.length()==0)) {
              if (outCode.equals("1")) {
                System.out.println(refUrl + " " + interval + " " + htype + " " + laststatus + " " + vsim + " " + sim +" " + size + " " + tld);
                writer.write(refUrl + " " + htype + " " + laststatus + " " + vsim + " " + sim +" " + size + " " + tld + "\n");
              }
              if (outCode.equals("2")) {
                // show url, interval, vsim and size
                System.out.println(refUrl + " " + interval + " " + vsim + " " + size);
                // writer.write(refUrl + " " + vsim + " " + size + "\n");
                writer.write("A" + id  + " " + vsim + " " + size + "\n");
              }
              if (outCode.equals("3")) {
                // show url, interval, laststatus and size
                System.out.println(refUrl + " " + interval + " " + laststatus + " " + size);
                // writer.write(refUrl + " " + laststatus + " " + size + "\n");
                writer.write("B" + id + " " + laststatus + " " + size + "\n");
              }
              if (outCode.equals("4")) {
                // show url, interval, laststatus and vsim
                System.out.println(refUrl + " " + interval + " " + laststatus + " " + vsim );
                // writer.write(refUrl + " " + laststatus + " " + vsim + "\n");
                writer.write("C" + id  + " " + laststatus + " " + vsim + "\n");
              }
              if (outCode.equals("5")) {
                // show url, interval, laststatus and tld
                System.out.println(refUrl + " " + interval + " " + laststatus + " " + tld);
                // writer.write(refUrl + " " + laststatus + " " + tld + "\n");
                writer.write("D" + id + " " + laststatus + " " + tld + "\n");
              }
              if (outCode.equals("6")) {
                // show url, interval, size and tld
                System.out.println(refUrl + " " + interval + " " + size + " " + tld);
                // writer.write(refUrl + " " + size + " " + tld + "\n");
                writer.write("E" + id + " " + size + " " + tld + "\n");
              }
              if (outCode.equals("7")) {
                // show url, interval, vsim and tld
                // writer.write(refUrl + " " + vsim + " " + tld + "\n");
                writer.write("F" + id + " " + vsim + " " + tld + "\n");
                System.out.println(refUrl + " " + interval + " " + vsim + " " + tld);
              }
              if (outCode.equals("8")) {
                // show url, interval, vsim and sizechange 
                // writer.write(refUrl + " " + vsim + " " + tld + "\n");
                int sizeChangeVal = Math.abs(Integer.valueOf(sizechange));
                writer.write("F" + id + " " + vsim + " " + sizeChangeVal + "\n");
                System.out.println(refUrl + " " + interval + " " + vsim + " " + sizeChangeVal);
              }
           }
           lastUrl = refUrl;
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
