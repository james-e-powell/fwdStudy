import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalStepArff {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  // private static String db_password = "remember"; 
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar intervalStepArff fwd_plos2 1 vsim laststatus

    int count = 0;
    String database = args[0];
    String outCode = args[1];
    String fieldOfInterest = args[2];
    String stepField = args[3];
    // 1 is counts, 2 is percent matrix, 3 is a column name
    // Interval, 2xx, 3xx 4xx, 5xx, none
    // 0,0.882279,0.015394,0.0971719,0.0020897,0.01650878

    System.out.println("@relation " + database );
    System.out.println();

    System.out.println("@attribute interval {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16}");
    System.out.println("@attribute url string");
    System.out.println("@attribute citedby string");
    System.out.println("@attribute last_val_" + stepField + " {200, 203, 301, 302, 400, 401, 403, 404, 405, 406, 408, 409, 410, 416, 429, 490, 500, 501, 502, 503, 504}");
    System.out.println("@attribute current_val_" + stepField + " {200, 203, 301, 302, 400, 401, 403, 404, 405, 406, 408, 409, 410, 416, 429, 490, 500, 501, 502, 503, 504}");
    System.out.println("@attribute status_chain string");
    System.out.println("@attribute vsim {0,1-9,10-19,20-29,30-39,40-49,50-59,60-69,70-79,80-89,90-99,100}");
    System.out.println("@attribute sim {0,1-9,10-19,20-29,30-39,40-49,50-59,60-69,70-79,80-89,90-99,100}");
    System.out.println("@attribute htype { *, application/json, application/msword, application/octet-stream, application/pdf, application/postscript, application/rar, application/rdf+xml, application/rss+xml, application/unknown, application/vnd.google-earth.kmz, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/x-dvi, application/x-gzip, application/x-rar, application/x-rar-compressed, application/x-tar, application/x-troff-man, application/xhtml+xml, application/xml, application/zip, Content-Type:text/html, image/jpeg, image/png, image/svg+xml, text/csv, text/html, text/plain, text/turtle, text/vnd.wap.wml, text/x-csrc, text/x-server-parsed-html, text/xml, video/mp4, video/quicktime, video/x-msvideo }");
    // System.out.println("@attribute laststatus {200, 203, 301, 302, 400, 401, 403, 404, 405, 406, 408, 409, 410, 416, 429, 490, 500, 501, 502, 503, 504}");
    System.out.println("@attribute size numeric");
    System.out.println("@attribute size_change numeric");
    System.out.println("@attribute tld string");
    System.out.println("@attribute drift {yes, no}");
    System.out.println("@data");
    System.out.println();

    // System.out.println("interval,url,last val " + fieldOfInterest + ", this val " + fieldOfInterest + ",statuschain,vsim,sim,htype,size,tld");

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
        // rs = stmt.executeQuery("select distinct(url) from " + database);
        rs = stmt.executeQuery("select distinct(url), citedby from " + database );
        while (rs.next()) {
          String thisUrl = rs.getString("url");
          String citedby = rs.getString("citedby");
          // String nestedQuery = "select * from " + database + " where url = '" + thisUrl + "' order by retrieve_interval";
          String nestedQuery = "select * from " + database + " where url = '" + thisUrl + "' and citedby = '" + citedby + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          String lastValue = "";
          String lastInterval = "";
          String lastUrl = "";
          while (rs2.next()) {
            // String thisValue = rs2.getString(fieldOfInterest);
            String thisValue = rs2.getString(stepField);
            if (lastValue.equals("")) { lastValue="?"; }
            String sim = rs2.getString("sim");
            if (sim.equals("") || sim.equals("failed") || sim.equals("incomparable") || sim.equals("nan")) { sim="?"; } else { sim=makeNominal(sim); }
            String vsim = rs2.getString("vsim");
            if (vsim.equals("") || vsim.equals("nan")) { vsim="?"; } else { vsim=makeNominal(vsim); }
            String htype = rs2.getString("htype");
            if (htype.equals("")) { htype="?"; }
            String objSize = rs2.getString("size");
            if (objSize.equals("")) { objSize="?"; }
            String tld = rs2.getString("tld");
            if (tld.equals("")) { tld="?"; } else {tld = tld.toLowerCase(); }
            String sizeChange = rs2.getString("size_change");
            if (sizeChange.equals("")) { sizeChange = "?"; }
            String thisInterval = rs2.getString("retrieve_interval");
            String statusChain = rs2.getString("status");
            if (statusChain.equals("")) { statusChain="?"; } else { statusChain = "\"" + statusChain + "\""; }
            if (thisValue.equals("")) { thisValue="?"; }
            if (thisValue.equals("") || thisValue.equals(null)) {
              thisValue = "none";
            }
            String drift = "";
            if (thisInterval.equals("0") || vsim.equals("80-90") || vsim.equals("90-99") || vsim.equals("100")) {
              drift = "no";
            } else 
              if (!vsim.equals("80-90") && !vsim.equals("90-99") && !vsim.equals("100")) {
                drift = "yes";
              } else { 
                drift = "no";
            }
            System.out.println(thisInterval + ",\"" + thisUrl + "\",\"" + citedby + "\"," + lastValue + "," + thisValue + "," + statusChain + ","+ vsim + "," + sim + "," + htype + "," + objSize + "," + sizeChange + "," + tld + "," + drift);
            lastValue = thisValue;
            lastValue = rs2.getString(stepField);
            lastInterval = thisInterval;
          }
          rs2.close();
        }
        rs.close();

        String tldQuery = "select distinct(tld) from " + database;
        Statement stmt3 = con.createStatement();
        ResultSet rs3 = null;
        rs3 = stmt3.executeQuery(tldQuery);
        while (rs3.next()) {
          System.out.print(rs3.getString("tld")+ ",");
        }
        rs3.close();


    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
  } 

  public static String makeNominal(String value) {
    float numValue = Float.valueOf(value);
    String retNom = "";
    if (numValue==0) { retNom = "0"; } 
    else if (numValue>0 && numValue<10) { retNom = "1-9"; }
    else if (numValue>9 && numValue<20) { retNom = "10-19"; }
    else if (numValue>19 && numValue<30) { retNom = "20-29"; }
    else if (numValue>29 && numValue<40) { retNom = "30-39"; }
    else if (numValue>39 && numValue<50) { retNom = "40-49"; }
    else if (numValue>49 && numValue<60) { retNom = "50-59"; }
    else if (numValue>59 && numValue<70) { retNom = "60-69"; }
    else if (numValue>69 && numValue<80) { retNom = "70-79"; }
    else if (numValue>79 && numValue<90) { retNom = "80-89"; }
    else if (numValue>89 && numValue<100) { retNom = "90-99"; }
    else if (numValue>=100) { retNom = "100"; }
    return retNom;
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
