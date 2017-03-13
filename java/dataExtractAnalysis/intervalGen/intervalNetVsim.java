import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class intervalNetVsim {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  // private static String db_password = "remember"; 
  private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int count = 0;
    String database = args[0];
    String outCode = args[1];
    String fieldOfInterest = args[2];
    // 1 is counts, 2 is percent matrix, 3 is a column name
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
        // writer = new BufferedWriter(new FileWriter(outFile));

        // output data column headers
        // writer.write("Interval, 2xx, 3xx 4xx, 5xx, none\n");

        Map<String,Integer> linkIntervalCount = new HashMap<String,Integer>();
        ArrayList<String> Vertices = new ArrayList<String>();

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        // rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database );
        rs = stmt.executeQuery("select distinct(tld) from " + database);
        while (rs.next()) {
          String thisTld = rs.getString("tld");
          String nestedQuery = "select * from " + database + " where tld = '" + thisTld + "' order by retrieve_interval";
          Statement stmt2 = con.createStatement();
          ResultSet rs2 = null;
          rs2 = stmt2.executeQuery(nestedQuery);
          String lastValue = "";
          String lastInterval = "";
          String lastTld = "";
          while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            String thisInterval = rs2.getString("retrieve_interval");
            if (thisValue.equals("") || thisValue.equals(null) || (thisValue.equals("nan") || (thisValue.equals("failed") || (thisValue.equals("incomparable"))))) {
              thisValue = "0";
            }
            int thisValueNumber = Math.round(Float.valueOf(thisValue));
           
            if (thisValueNumber==100) {
              thisValue = "100";
            }
            if ((thisValueNumber>=90) && (thisValueNumber<100)) {
              thisValue = "90-99";
            }
            if ((thisValueNumber>=80) && (thisValueNumber<90)) {
              thisValue = "80-89";
            }
            if ((thisValueNumber>=70) && (thisValueNumber<80)) {
              thisValue = "70-79";
            }
            if ((thisValueNumber>=60) && (thisValueNumber<70)) {
              thisValue = "60-69";
            }
            if ((thisValueNumber>=50) && (thisValueNumber<60)) {
              thisValue = "50-59";
            }
            if ((thisValueNumber>=40) && (thisValueNumber<50)) {
              thisValue = "40-49";
            }
            if ((thisValueNumber>=30) && (thisValueNumber<40)) {
              thisValue = "30-39";
            }
            if ((thisValueNumber>=20) && (thisValueNumber<30)) {
              thisValue = "20-29";
            }
            if ((thisValueNumber>=10) && (thisValueNumber<20)) {
              thisValue = "10-19";
            }
            if ((thisValueNumber>=1) && (thisValueNumber<10)) {
              thisValue = "1-9";
            }
            if (thisValueNumber==0) {
              thisValue = "0";
            }

            if (!(lastValue.equals("")) && (!(lastInterval.equals("")))) {
              String hashIndex = "{\"source\":\"" + lastInterval + ","+lastTld+"," + lastValue + "\",\"target\":\"" + thisInterval + "," + thisTld + "," + thisValue + "\"";
              if (!(Vertices.contains(thisInterval+","+thisTld + "," + thisValue))) {
                Vertices.add(thisInterval+","+thisTld+","+thisValue);
                System.out.println(thisInterval+","+thisTld+","+thisValue);
              }

            try {
              int typeValCounter = linkIntervalCount.get(hashIndex);
              typeValCounter++;
              linkIntervalCount.put(hashIndex, typeValCounter);
              // System.out.println(typeValCounter + "},");
            } catch (Exception e) {
              linkIntervalCount.put(hashIndex, 1);
              // System.out.println(1 + "},");
            }
            }
            lastValue = thisValue;
            lastInterval = thisInterval;
            lastTld = thisTld;
          }
          rs2.close();
        }
        rs.close();


         if (outCode.equals("1")) {

           System.out.println("{\"nodes\":[");
           for (String key: Vertices) {
             System.out.println("{\"name\":\"" + key + "\"},");
           }
           System.out.println("],");

           System.out.println("\"links\":[");
           for (String key: linkIntervalCount.keySet()) {
           // for (String key: Vertices) {
             try {
                // writer.write(key + " " + linkIntervalCount.get(key) + "\n");
                System.out.println(key + ",\"value\":" + linkIntervalCount.get(key) + "},");
             } catch(Exception e) { System.out.println("exception " + e); }
           }
           System.out.println("]}");
         }


      // writer.close();

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
