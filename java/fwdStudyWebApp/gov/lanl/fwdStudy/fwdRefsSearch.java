package gov.lanl.fwdStudy;

import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

import javax.servlet.*;
import javax.servlet.http.*;

public class fwdRefsSearch extends HttpServlet {
    // form input fields include:
    // solr query string
    // int specifying the number of records to be retrieved for this collection
    //
    //
    //
  String queryString = "";
  String tableName = "";


  private static String serverAddr="";

  private static final String CONFIG_FILENAME = "metrics_server.properties";
  private static Properties config = new Properties();
  static {
        try {
            InputStream in = fwdRefsSearch.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME);
            config.load(in);
            System.out.println("DB URL: " + config.getProperty("db_url"));
            System.out.println("DB Driver: " + config.getProperty("db_driver"));
            System.out.println("DB User: " + config.getProperty("db_user"));
            System.out.println("DB Password: " + config.getProperty("db_password"));
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        } // try

  }

  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
      queryString = request.getParameter("uri");
      tableName = request.getParameter("table");
      doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
      System.out.println("doing get");
      queryString = request.getParameter("uri");
      tableName = request.getParameter("table");

      try { 

         Class.forName(config.getProperty("db_driver"));
         String url = config.getProperty("db_url");
         Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));

         PrintWriter out = response.getWriter();

         response.setContentType("text/html");
         resolveIdentifier(con, out, queryString, tableName);

         out.close();

     } catch (Exception e) { System.out.println("Error "+e); }


  }

  public static void listIdentifiers (Connection con, PrintWriter outPut, String query, String tableName) {

  }

  public static void resolveIdentifier (Connection con, PrintWriter outPut, String query, String tableName) {
    try {

      outPut.println("<html><head>");
      outPut.println("    <script src=\"static/scripts/js/jquery/jquery-2.1.1.min.js\"></script>");
      outPut.println("    <script src=\"static/scripts/js/jquery/jquery.sparkline.min.js\"></script>");
      outPut.println("<style>");
      outPut.println("td, th {");
      // outPut.println("    border: 1px solid black;");
      outPut.println("    font-family: Arial; font-size: 8pt;");
      outPut.println("}");
      outPut.println("td.spark, th.spark {");
      outPut.println("    font-family: Arial; font-size: 10pt;");
      outPut.println("}");
      outPut.println("td.sparkbar, th.sparkbar {");
      outPut.println("    font-family: Arial; font-size: 20pt;");
      outPut.println("}");
      outPut.println("table {");
      outPut.println("    border-collapse: collapse;");
      outPut.println("}");

      outPut.println("</style>");

      outPut.println("</head>");
      outPut.println("<body>");

      ArrayList<String> driftList = new ArrayList<String>();

      Statement stmt = con.createStatement();
      ResultSet rs=null;
      query = query.replace(" ", "+");
      String queryString = "select distinct(url) from " + tableName + " where citedby=\'" + query + "\' and htype=\'text/html\'";

      System.out.println(queryString);
      rs = stmt.executeQuery(queryString);
      int count = 0;
      int intervalCount = 0;
      outPut.println("<h2>Paper URI: " + query + "</h2>");
      outPut.println("<h3>Drift Graph: <div id=\"driftspark\"></div></h3>");
      while (rs.next()) {
        Statement stmt2 = con.createStatement();
        String refUrl = rs.getString("url");
        String refQueryString = "select * from " + tableName + " where url=\'" + refUrl + "\' and citedby=\'" + query + "\'";
        System.out.println(refQueryString);
        ResultSet rs2=null;
        rs2 = stmt2.executeQuery(refQueryString);
        StringBuffer vsimSpark = new StringBuffer("");
        StringBuffer simSpark = new StringBuffer("");

        outPut.println("<h3>Cited Reference: <a href=\"/fwdStudy/fwdSimSearch?uri=" + refUrl + "&citedby=" + query + "&table=" + tableName + "\">" + refUrl + "</a></h3>");

        while (rs2.next()) {
          if (count==0) {
            intervalCount++;
          }
          // if rs2.getString("htype").equals("text/html")
          String thisVsim = rs2.getString("vsim");
          System.out.println("this VSIM " + thisVsim);

          String thisSim = rs2.getString("sim");
          System.out.println("this SIM " + thisSim);

          String intervalDate = rs2.getString("citedate");
          System.out.println("cite date " + intervalDate);

          // if ((thisVsim.equals("incomparable")) || (thisVsim.equals("nan")) || (thisVsim.equals("failed") || (thisVsim.equals("")))) {
          if ((thisVsim.equals("incomparable")) || (thisVsim.equals("nan")) || (thisVsim.equals("failed"))) {
              vsimSpark.append("0,");
            } else {
              vsimSpark.append(thisVsim + ",");
          }
          // if ((thisSim.equals("incomparable")) || (thisSim.equals("nan")) || (thisSim.equals("failed")) || (thisSim.equals(""))) {
          if ((thisSim.equals("incomparable")) || (thisSim.equals("nan")) || (thisSim.equals("failed"))) {
              simSpark.append("0,");
          } else {
              simSpark.append(thisSim + ",");
          }
        }
        rs2.close();
        String vsimSparkData = vsimSpark.toString();
        String vsimSparkDataSet = vsimSparkData.substring(1, vsimSparkData.length()-1);
        driftList.add(vsimSparkDataSet);
        String simSparkData = simSpark.toString();
        String simSparkDataSet = simSparkData.substring(1, simSparkData.length()-1);
        outPut.println("<table>");
        outPut.println("<tr><th class=\"spark\">VSIM</th></tr>");
        outPut.println("<tr><td class=\"sparkbar\"><div id=\"vsimspark"+Integer.toString(count) + "\"></div></td></tr>");
        outPut.println("<tr><th class=\"spark\">Similiarity Hash</th></tr>");
        outPut.println("<tr><td class=\"sparkbar\"><div id=\"hashspark" + Integer.toString(count) + "\"></div></td></tr>");
        outPut.println("</table>");
        outPut.println(" <script type=\"text/javascript\">");
        outPut.println("[\"VSIM\", \"Similarity Hash\"]");

        outPut.println("$(\"#vsimspark" + Integer.toString(count) + "\").sparkline(");
        outPut.println("[" + vsimSparkDataSet + "] , { type: \'bar\', chartRangeMin: \'0\', chartRangeMax: \'100\'});");

        outPut.println("$(\"#hashspark" + Integer.toString(count) + "\").sparkline(");
        outPut.println("[" + simSparkDataSet + "] , { type: \'bar\', chartRangeMin: \'0\', chartRangeMax: \'100\'});");

        outPut.println("</script>");
        count++;
    }

    String[] driftVals = new String[intervalCount];

    for (int i=0; i<intervalCount-1; i++) {
      System.out.println("index is " + i);
      float sum = 0;
      for (String entry : driftList) {
        String [] entries = entry.split(",");
        float intervalVal = 0;
        try { 
          intervalVal = Float.parseFloat(entries[i]); 
        } catch (Exception e) { 
          intervalVal = 0;
        }
        sum += intervalVal;
        System.out.println(entries[i]);
      }
      System.out.println("sum is " + sum);
      System.out.println("drift val is " + sum / count);
      driftVals[i] = Float.toString(sum / count);
    }

    StringBuffer driftSpark = new StringBuffer("");

    for (int i=0; i<intervalCount-1; i++) {
        driftSpark.append(driftVals[i] + ",");
        System.out.println(driftVals[i]);
    }
    String driftSparkData = driftSpark.toString();
    System.out.println(driftSparkData);
    String driftSparkDataSet = driftSparkData.substring(0, driftSparkData.length()-1);
    System.out.println(driftSparkDataSet);
    outPut.println(" <script type=\"text/javascript\">");
    outPut.println("[\"Drift\"]");

    outPut.println("$(\"#driftspark\").sparkline(");
    outPut.println("[" + driftSparkDataSet + "] , { type: \'line\', chartRangeMin: \'0\', chartRangeMax: \'100\'});");
    outPut.println(" </script>");
   
    rs.close();
    outPut.println("</body></html>");
  } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
  } 
} // getNodes method

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

