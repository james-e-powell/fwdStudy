import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class pairwisePlot {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 


  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar pairwisePlot fwd_plos2 vsim size_change

    int rowcount = 0;
    String database = args[0];
    String field1 = args[1];
    String field2 = args[2];

    try { 
        Class.forName(db_driver);
        String url = db_url;
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        int intervalCount = 0;
        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database);
        while (rs.next()) {
          intervalCount++;
        }
        rs.close();
        System.out.println(intervalCount);

        String nestedQuery = "select * from " + database + " order by retrieve_interval";
        Statement stmt2 = con.createStatement();
        ResultSet rs2 = null;
        rs2 = stmt2.executeQuery(nestedQuery);
        while (rs2.next()) {
            String field1Val = rs2.getString(field1);
            if (field1Val.equals("") || field1Val.equals("nan")) { field1Val = "0"; }
            String field2Val = rs2.getString(field2);
            String thisInterval = rs2.getString("retrieve_interval");
            String thisId = rs2.getString("id");
            System.out.println(thisInterval + "," + thisId + ","  + (Math.abs(Double.valueOf(field1Val))) + "," + (Math.abs(Double.valueOf(field2Val))));
            rowcount++;
        }
        rs2.close();

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
    System.out.println("Number of rows " + rowcount);
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
