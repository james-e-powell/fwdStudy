import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.Vector;

public class correlationInterval {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 

  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar correlationTest fwd_plos2

    String database = args[0];
    String selectedInterval = args[1];
    int count = 0;
    double corrSum = 0;

    Vector<Double> rVals = new Vector<>();

    try {
        Class.forName(db_driver);
        String url = db_url;
        Connection con = DriverManager.getConnection(url,db_user,db_password);

        Statement stmt = con.createStatement();
        ResultSet rs=null;

        rs = stmt.executeQuery("select distinct(url) from " + database );
        while (rs.next()) {
          ResultSet rs2 = null;
          String refUrl = rs.getString("url");
          Statement stmt2 = con.createStatement();
          rs2 = stmt2.executeQuery("select * from " + database + " where url = '" + refUrl + "'");

          Vector<Double> vsims = new Vector<>();
          Vector<Double> sims = new Vector<>();
          Vector<Double> sizechanges = new Vector<>();

          while (rs2.next()) {
            String interval = rs2.getString("retrieve_interval");
            String vsim = rs2.getString("vsim");
            if ((vsim.equals("nan")) || (vsim.equals(""))) { vsim = "0"; }
            String sim = rs2.getString("sim");
            if ((sim.equals("nan")) || (sim.equals("")) || (sim.equals("failed")) || (sim.equals("incomparable"))) { sim = "0"; }
            String sizechange = rs2.getString("size_change");
            if (sizechange.equals("")) { sizechange = "0"; }
            Random rand = new Random();
            if (interval.equals(selectedInterval))  {
              double vsimVal = Double.valueOf(vsim);
              double simVal = Double.valueOf(sim);
              double sizechangeVal = Double.valueOf(sizechange);
              vsims.add((Double) vsimVal);
              sizechanges.add((Double) sizechangeVal);
              sims.add((Double) simVal);
            }
          } 
          rs2.close();
          double corrVal = GetCorrelation(vsims, sizechanges);
          // double corrVal = GetCorrelation(vsims, sims);
          if (!(Double.isNaN(corrVal))) {
            System.out.println("correlation value " + corrVal);
            rVals.add(corrVal);
            count++;
            corrSum += corrVal;
          }
        }
        rs.close();

        double standardErr = 1 / (Math.sqrt(count) - 3);
        System.out.println("population correlation coefficient " + populationCorrelation(rVals, count));
        System.out.println("standard error " + standardErr);

    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
    System.out.println("Mean correlation coefficient " + corrSum / count);
    System.out.println("Count " + count);

  }

  public static double populationCorrelation(Vector<Double> rVals, int count) {
      double zValSum = 0;
      for(int i = 0; i < rVals.size(); i++)
      {  
        // zValSum += Math.atanh(rVals.elementAt(i)); 
        double thisZVal = ((1/2) * (Math.log((1+rVals.elementAt(i))/ (1-rVals.elementAt(i)))));
        zValSum += thisZVal;
        System.out.println(rVals.elementAt(i) + "," + thisZVal);
        
      }
      double zValMean = zValSum / count;
      System.out.println("z value mean " + zValMean);
      return Math.tanh(zValMean / Math.sqrt(count - 3));
  }

/**
  * Calculate the Pearson correlation coefficient of two lists, X and Y.
  *
  * @param X original human relatedness values
  * @param Y metric relatedness values
  * 
  * @return measure of correlation between the two lists
  */
  public static double GetCorrelation(Vector<Double> xVect, Vector<Double> yVect) {
    double meanX = 0.0, meanY = 0.0;
    for(int i = 0; i < xVect.size(); i++)
    {
        meanX += Math.abs(xVect.elementAt(i));
        meanY += Math.abs(yVect.elementAt(i));
    }

    meanX /= xVect.size();
    meanY /= yVect.size();

    double sumXY = 0.0, sumX2 = 0.0, sumY2 = 0.0;
    for(int i = 0; i < xVect.size(); i++)
    {
      sumXY += ((xVect.elementAt(i) - meanX) * (yVect.elementAt(i) - meanY));
      sumX2 += Math.pow(xVect.elementAt(i) - meanX, 2.0);
      sumY2 += Math.pow(yVect.elementAt(i) - meanY, 2.0);
    }

    return (sumXY / (Math.sqrt(sumX2) * Math.sqrt(sumY2)));
  }//end: GetCorrelation(X,Y)
}
