import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class traitCounter {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar traitCounter fwd_plos5 entropy

    int count = 0;
    String database = args[0];
    String fieldOfInterest = args[1];

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

          float firstVal = 0;
          float lastVal = 0;

          String refUrl = rs.getString("url");
          String nestedQuery = "select * from " + database + " where url = '" + refUrl + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);

          // System.out.println(refUrl);
          while (rs2.next()) {
            String interval = rs2.getString("retrieve_interval");
            if (!(interval.equals("0"))) {
              String thisValue = rs2.getString(fieldOfInterest);
              float thisNumeric = 0;
              // System.out.println(thisValue);
              if (thisValue.equals("") || thisValue.equals(null) || (thisValue.equals("NULL")) || (thisValue==null) || (thisValue.equals("failed")) || (thisValue.equals("nan")) || (thisValue.equals("incomparable"))) {
                thisNumeric = 0;
              } else {
                thisNumeric = Float.valueOf(thisValue);
              }
              if (interval.equals("1")) {
                firstVal = thisNumeric;
              }
              // if (interval.equals("14")) {
              if (interval.equals("2")) {
                lastVal = thisNumeric;
              }
            }
            // System.out.println("{\"name\":\"" + thisValue + "\"},");
          }
          if (firstVal < lastVal) {
            yesCount++;
          } 
          if (firstVal > lastVal) {
            noCount++;
          }
          if (firstVal == lastVal) {
            sameCount++;
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
