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

public class fwdSimsSearch extends HttpServlet {
    // form input fields include:
    // solr query string
    // int specifying the number of records to be retrieved for this collection
    //
    //
    //
  String queryString = "";
  String tableName = "";
  String citedBy = "";

  private static String serverAddr="";

  private static final String CONFIG_FILENAME = "metrics_server.properties";
  private static Properties config = new Properties();
  static {
        try {
            InputStream in = fwdSimsSearch.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME);
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
      citedBy = request.getParameter("citedby");
      doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
      System.out.println("doing get");
      queryString = request.getParameter("uri");
      tableName = request.getParameter("table");
      citedBy = request.getParameter("citedby");

      try { 

         Class.forName(config.getProperty("db_driver"));
         String url = config.getProperty("db_url");
         Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));

         PrintWriter out = response.getWriter();

         response.setContentType("text/html");
         resolveIdentifier(con, out, queryString, tableName, citedBy);

         out.close();

     } catch (Exception e) { System.out.println("Error "+e); }


  }

  public static void resolveIdentifier (Connection con, PrintWriter outPut, String query, String tableName, String citedBy) {
    try {
      Statement stmt = con.createStatement();
      ResultSet rs=null;
      query = query.replace(" ", "+");
      String queryString = "select * from " + tableName + " where url=\'" +query+"\' and citedby=\'" + citedBy + "\' order by retrieve_interval";
      System.out.println(queryString);
      rs = stmt.executeQuery(queryString);

      StringBuffer vsimBuffer = new StringBuffer("<tr><th>VSIM</th>");
      StringBuffer vsimSpark = new StringBuffer("");
      StringBuffer simBuffer = new StringBuffer("<tr><th>Similarity Hash</th>");
      StringBuffer simSpark = new StringBuffer("");
      StringBuffer dateRetrieval = new StringBuffer("<tr><th>Date</th>");
      StringBuffer intervals = new StringBuffer("<tr><th>Interval</th>");

      outPut.println("<html><head>");
      outPut.println("    <script src=\"static/scripts/js/jquery/jquery-2.1.1.min.js\"></script>");
      outPut.println("    <script src=\"static/scripts/js/jquery/jquery.sparkline.min.js\"></script>");
      outPut.println("<style>");
      outPut.println("td, th {");
      outPut.println("    border: 1px solid black;");
      outPut.println("    font-family: Arial; font-size: 8pt;");
      outPut.println("}");
      outPut.println("td.spark, th.spark {");
      outPut.println("    font-family: Arial; font-size: 16pt;");
      outPut.println("}");
      outPut.println("table {");
      outPut.println("    border-collapse: collapse;");
      outPut.println("}");

      outPut.println("</style>");

      outPut.println("</head>");
      outPut.println("<body>");
      
      outPut.println("<h2>URI " + query + "</h2>");
      outPut.println("<table border=\"1\">");
      while (rs.next()) {
          String thisVsim = rs.getString("vsim");
          String thisSim = rs.getString("sim");

          String url = rs.getString("url");
          String intervalDate = rs.getString("citedate");
          String intervalCount = rs.getString("retrieve_interval");


          dateRetrieval.append("<th>" + intervalDate + "</th>");
          intervals.append("<th>" + intervalCount + "</th>");

          // String timetravel = "http://timetravel.mementoweb.org/list/20141117194343/http://alps.comp-phys.org";
          String timetravel = "http://timetravel.mementoweb.org/list/" + intervalDate + "194343/" + url;

          vsimBuffer.append("<td><a href=\"" + timetravel + "\">" + thisVsim + "</a></td>");
          if ((thisVsim.equals("incomparable")) || (thisVsim.equals("nan")) || (thisVsim.equals("failed"))) {
            vsimSpark.append("0,");
          } else {
            vsimSpark.append(thisVsim + ",");
          }
          simBuffer.append("<td>" + thisSim + "</td>");
          if ((thisSim.equals("incomparable")) || (thisSim.equals("nan")) || (thisSim.equals("failed"))) {
            simSpark.append("0,");
          } else {
            simSpark.append(thisSim + ",");
          }
      }
      String vsimSparkData = vsimSpark.toString();
      String vsimSparkDataSet = vsimSparkData.substring(1, vsimSparkData.length()-1);
      String simSparkData = simSpark.toString();
      String simSparkDataSet = simSparkData.substring(1, simSparkData.length()-1);
      // outPut.println(dateRetrieval.toString() + "</tr>"); 
      outPut.println(intervals.toString() + "</tr>");
      outPut.println(vsimBuffer.toString() + "</tr>");
      outPut.println(simBuffer.toString() + "</tr>");
      
      outPut.println("</table>");

      outPut.println("<p><h2>Visualizations of...</h2>");
      outPut.println("<table>");
      outPut.println("<tr><th class=\"spark\">VSIM</th></tr>");
      outPut.println("<tr><td class=\"spark\"><div id=\"sparkline1\"></div></td></tr>");
      outPut.println("<tr><th class=\"spark\">Similiarity Hash</th></tr>");
      outPut.println("<tr><td class=\"spark\"><div id=\"sparkline2\"></div></td></tr>");
      outPut.println("</table>");
      outPut.println(" <script type=\"text/javascript\">");
      outPut.println("[\"VSIM\", \"Similarity Hash\"]");

      outPut.println("$(\"#sparkline1\").sparkline(");
      outPut.println("[" + vsimSparkDataSet + "] , { type: \'bar\', chartRangeMin: \'0\', chartRangeMax: \'100\'});");
//      outPut.println("[0.47058823529411764, 0.17647058823529413, 0.4117647058823529, 0.058823529411764705, 0.058823529411764705, 0.23529411764705882, 0.23529411764705882, 0.29411764705882354, 0.4117647058823529, 1.0, 0.6470588235294118, 0.11764705882352941, 0.7647058823529411, 0.5294117647058824, 0.7647058823529411, 0.8823529411764706, 0.23529411764705882, 0.058823529411764705, 0.8823529411764706, 0.058823529411764705, 0.29411764705882354, 0.5294117647058824, 0.35294117647058826, 0.4117647058823529, 0.11764705882352941, 0.47058823529411764, 0.47058823529411764, 0.058823529411764705, 0.29411764705882354, 0.29411764705882354, 0.7647058823529411] , { type: \'bar\', lineColor: \'green\', fillColor: \'false\'});");

      outPut.println("$(\"#sparkline2\").sparkline(");
      outPut.println("[" + simSparkDataSet + "] , { type: \'bar\', chartRangeMin: \'0\', chartRangeMax: \'100\'});");
//      outPut.println("[0.4117647058823529, 0.29411764705882354, 0.17647058823529413, 0.11764705882352941, 0.29411764705882354, 0.17647058823529413, 0.058823529411764705, 0.17647058823529413, 0.058823529411764705, 0.058823529411764705, 0.29411764705882354, 0.23529411764705882, 0.4117647058823529, 0.23529411764705882, 0.47058823529411764, 0.47058823529411764, 0.35294117647058826, 0.47058823529411764, 0.5294117647058824, 0.6470588235294118, 0.7058823529411765, 0.7058823529411765, 0.5882352941176471, 0.35294117647058826] , { type: \'bar\', lineColor: \'red\', fillColor: \'false\'});");

      outPut.println("</script>");
      outPut.println("</body></html>");
      rs.close();

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

