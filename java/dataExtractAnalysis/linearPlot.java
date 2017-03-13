import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class linearPlot {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "";
  private static String db_password = "";
  // private static String db_password = ""; 

  int[] vsimRanges = new int[] {0,10,20,30,40,50,60,70,90,100};

  public static void main(String args[]) {

    int rowcount = 0;
    String database = args[0];
    String outCode = args[1];
    String fieldOfInterest = args[2];
    // 1 is counts, 2 is percent matrix, 3 is a column name
    // Interval, 2xx, 3xx 4xx, 5xx, none
    // 0,0.882279,0.015394,0.0971719,0.0020897,0.01650878

    try { 
        Class.forName(db_driver);
        String url = db_url;
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Map<String,Integer> linkIntervalCount = new HashMap<String,Integer>();
        ArrayList<String> Vertices = new ArrayList<String>();

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        int intervalCount = 0;
        rs = stmt.executeQuery("select distinct(retrieve_interval) from " + database);
        while (rs.next()) {
          intervalCount++;
        }
        rs.close();

        int [] dataLine0 = new int[intervalCount]; //  0
        int [] dataLine1 = new int[intervalCount]; // 1-9
        int [] dataLine2 = new int[intervalCount]; // 10-19
        int [] dataLine3 = new int[intervalCount]; // 20-29
        int [] dataLine4 = new int[intervalCount]; // 30-39
        int [] dataLine5 = new int[intervalCount]; // 40-49
        int [] dataLine6 = new int[intervalCount]; // 50-59
        int [] dataLine7 = new int[intervalCount]; // 60-69
        int [] dataLine8 = new int[intervalCount]; // 70-79
        int [] dataLine9 = new int[intervalCount]; // 80-89
        int [] dataLine10 = new int[intervalCount]; // 90-99
        int [] dataLine11 = new int[intervalCount]; // 100
        int [] dataLine12 = new int[intervalCount]; // 100
        int [] dataLine13 = new int[intervalCount]; // 100
        int [] dataLine14 = new int[intervalCount]; // 100
        int [] dataLine15 = new int[intervalCount]; // 100
        int [] dataLine16 = new int[intervalCount]; // 100
        int [] dataLine17 = new int[intervalCount]; // 100
        for (int count=0; count<intervalCount; count++) {
          dataLine0[count]=0;
          dataLine1[count]=0;
          dataLine2[count]=0;
          dataLine3[count]=0;
          dataLine4[count]=0;
          dataLine5[count]=0;
          dataLine6[count]=0;
          dataLine7[count]=0;
          dataLine8[count]=0;
          dataLine9[count]=0;
          dataLine10[count]=0;
          dataLine11[count]=0;
          dataLine12[count]=0;
          dataLine13[count]=0;
          dataLine14[count]=0;
          dataLine15[count]=0;
          dataLine16[count]=0;
          dataLine17[count]=0;
        }
        System.out.println(intervalCount);

        // String nestedQuery = "select * from " + database + " order by retrieve_interval";
        String nestedQuery = "select * from " + database + " order by url";
        Statement stmt2 = con.createStatement();
        ResultSet rs2 = null;
        rs2 = stmt2.executeQuery(nestedQuery);
        String lastValue = "";
        String lastInterval = "";
        float startSize = 0;
        int thisValueNumber = 0;
        while (rs2.next()) {
            String thisValue = rs2.getString(fieldOfInterest);
            String thisInterval = rs2.getString("retrieve_interval");
            String thisUrl = rs2.getString("url");
            int intervalIndex = Integer.valueOf(thisInterval);
            if (intervalIndex==0) {
              startSize = Float.valueOf(thisValue);
            } 

            if ((intervalIndex>0) && (startSize>0)) {
              thisValueNumber = Math.round(Float.valueOf(thisValue)/startSize * 100);
            }

            if (thisValue.equals("") || thisValue.equals(null) || (thisValue.equals("nan") || (thisValue.equals("failed") || (thisValue.equals("incomparable"))))) {
              thisValue = "0";
              dataLine0[intervalIndex]++;
            }
           
            if (thisValueNumber==100) {
              thisValue = "100";
              dataLine11[intervalIndex]++;
            }
            if (thisValueNumber >100 && thisValueNumber <=150) {
              dataLine12[intervalIndex]++;
            }
            if (thisValueNumber >150 && thisValueNumber <=200) {
              dataLine13[intervalIndex]++;
            }
            if (thisValueNumber >200 && thisValueNumber <=300) {
              dataLine14[intervalIndex]++;
            }
            if (thisValueNumber >300 && thisValueNumber <=400) {
              dataLine15[intervalIndex]++;
            }
            if (thisValueNumber >400 && thisValueNumber <=500) {
              dataLine16[intervalIndex]++;
            }
            if (thisValueNumber >500) {
              dataLine17[intervalIndex]++;
            }
            
            if ((thisValueNumber>=90) && (thisValueNumber<100)) {
              thisValue = "90-99";
              dataLine10[intervalIndex]++;
            }
            if ((thisValueNumber>=80) && (thisValueNumber<90)) {
              thisValue = "80-89";
              dataLine9[intervalIndex]++;
            }
            if ((thisValueNumber>=70) && (thisValueNumber<80)) {
              thisValue = "70-79";
              dataLine8[intervalIndex]++;
            }
            if ((thisValueNumber>=60) && (thisValueNumber<70)) {
              thisValue = "60-69";
              dataLine7[intervalIndex]++;
            }
            if ((thisValueNumber>=50) && (thisValueNumber<60)) {
              thisValue = "50-59";
              dataLine6[intervalIndex]++;
            }
            if ((thisValueNumber>=40) && (thisValueNumber<50)) {
              thisValue = "40-49";
              dataLine5[intervalIndex]++;
            }
            if ((thisValueNumber>=30) && (thisValueNumber<40)) {
              thisValue = "30-39";
              dataLine4[intervalIndex]++;
            }
            if ((thisValueNumber>=20) && (thisValueNumber<30)) {
              thisValue = "20-29";
              dataLine3[intervalIndex]++;
            }
            if ((thisValueNumber>=10) && (thisValueNumber<20)) {
              thisValue = "10-19";
              dataLine2[intervalIndex]++;
            }
            if ((thisValueNumber>=1) && (thisValueNumber<10)) {
              thisValue = "1-9";
              dataLine1[intervalIndex]++;
            }
            if (thisValueNumber==0) {
              thisValue = "0";
              dataLine0[intervalIndex]++;
            }
            System.out.println(intervalIndex + " " + thisUrl + " " + thisValue + " " + thisValueNumber);

            rowcount++;
        }
        rs2.close();
        System.out.print(fieldOfInterest + "_value0,");
        for (int entry : dataLine0) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value1,");
        for (int entry : dataLine1) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value2,");
        for (int entry : dataLine2) {
          System.out.print(entry+",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value3,");
        for (int entry : dataLine3) {
          System.out.print(entry+",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value4,");
        for (int entry : dataLine4) {
          System.out.print(entry+",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value5,");
        for (int entry : dataLine5) {
          System.out.print(entry+",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value6,");
        for (int entry : dataLine6) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value7,");
        for (int entry : dataLine7) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value8,");
        for (int entry : dataLine8) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value9,");
        for (int entry : dataLine9) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value10,");
        for (int entry : dataLine10) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value11,");
        for (int entry : dataLine11) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value12,");
        for (int entry : dataLine12) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value13,");
        for (int entry : dataLine13) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value14,");
        for (int entry : dataLine14) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value15,");
        for (int entry : dataLine15) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value16,");
        for (int entry : dataLine16) {
          System.out.print(entry +",");
        }
        System.out.println();
        System.out.print(fieldOfInterest + "_value17,");
        for (int entry : dataLine17) {
          System.out.print(entry +",");
        }
        System.out.println();



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
