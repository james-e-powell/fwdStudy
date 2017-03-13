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

public class fwdCiteSearch extends HttpServlet {
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
            InputStream in = fwdCiteSearch.class.getClassLoader().getResourceAsStream(CONFIG_FILENAME);
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
      queryString = request.getParameter("query");
      tableName = request.getParameter("table");
      doGet(request, response);
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException {
      System.out.println("doing get");
      queryString = request.getParameter("query");
      tableName = request.getParameter("table");

      try { 

         Class.forName(config.getProperty("db_driver"));
         String url = config.getProperty("db_url");
         Connection con = DriverManager.getConnection(url,config.getProperty("db_user"),config.getProperty("db_password"));

         PrintWriter out = response.getWriter();

         response.setContentType("text/html");
         resolveIdentifier(con, out, queryString, tableName);
         con.close();
         out.close();

     } catch (Exception e) { System.out.println("Error "+e); }


  }

  public static void resolveIdentifier (Connection con, PrintWriter outPut, String query, String tableName) {
    try {
      Statement stmt = con.createStatement();
      ResultSet rs=null;
      rs = stmt.executeQuery("select distinct(citedby) from " + tableName + " where citedby like \'%"+query+"%\' order by citedby");
      outPut.println("<h2>FwdStudy Similarity Browser</h2>");
      outPut.println("For data set " + tableName );
      
      outPut.println("<table><tr><th>URIs</th></tr>");
      while (rs.next()) {
          String thisUrl = rs.getString("citedby");
          outPut.println("<tr><td><a href=\"fwdRefSearch?uri=" + thisUrl + "&table=" + tableName + "\">" + thisUrl + "</a></td></tr>");
      }
      
      outPut.println("</table>");
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

