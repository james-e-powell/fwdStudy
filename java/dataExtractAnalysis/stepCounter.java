import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class stepCounter {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar stepCounter fwd_arxiv2 status vsim

    int count = 0;
    String database = args[0];
    String fieldOfInterest1 = args[1]; // e.g. status
    String fieldOfInterest2 = args[2]; // numeric value, e.g. vsim

    try { 
        Class.forName(db_driver);
        String url = db_url;
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        BufferedWriter writer = null;
        String outFilename = "summarized/" + database + ".csv";
        File outFile = new File(outFilename);
        writer = new BufferedWriter(new FileWriter(outFile));
        int yesCount = 0;
        int noCount = 0;
        int sameCount = 0;

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(url) from " + database );
        while (rs.next()) {

          float thisVal = 0;
          float lastVal = 0;
          String lastString = "";

          String refUrl = rs.getString("url");
          String nestedQuery = "select * from " + database + " where url = '" + refUrl + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);

          // System.out.println(refUrl);
          Boolean condition = false;
          while (rs2.next()) {
            String interval = rs2.getString("retrieve_interval");
            if (!(interval.equals("15"))) {
              
              String thisString = rs2.getString(fieldOfInterest1);
              String thisValue = rs2.getString(fieldOfInterest2);
              float thisNumeric = 0;
              // System.out.println(thisString);
              // System.out.println(thisValue);
              // if (thisValue==null) {
              if ((thisValue==null) || (thisValue.equals("")) || (thisValue.equals(null)) || (thisValue.equals("NULL")) || (thisValue==null) || (thisValue.equals("failed")) || (thisValue.equals("nan")) || (thisValue.equals("incomparable"))) {
              // if ((thisValue.equals("")) || (thisValue.equals(null)) || (thisValue.equals("NULL")) || (thisValue==null) || (thisValue.equals("failed")) || (thisValue.equals("nan")) || (thisValue.equals("incomparable"))) {
                thisNumeric = 0;
              } else {
                thisNumeric = Float.valueOf(thisValue);
              }
              thisVal = thisNumeric;
              if (condition) {
                 System.out.println("\"" + refUrl + "\",\"" + interval + "\",\"" + thisString + "\",\"" + thisVal + "\",\"" + lastString + "\",\"" + lastVal + "\"");
                  if (thisVal > lastVal) {
                    yesCount++;
                  } 
                  if (thisVal < lastVal) {
                    noCount++;
                  }
                  if (thisVal == lastVal) {
                    sameCount++;
                  }
                 condition = false;
              }

              if (thisString.length()>=5) {
                condition = true;
              } else {
                condition = false;
              }
    
              lastString = thisString; 
              lastVal = thisVal;
            }
            // System.out.println("{\"name\":\"" + thisValue + "\"},");
          }
          rs2.close();
        }
      rs.close();
      writer.close();
      System.out.println("End value higher than start " + yesCount);
      System.out.println("End value lower than start " + noCount);
      System.out.println("End value same as start " + sameCount);

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
