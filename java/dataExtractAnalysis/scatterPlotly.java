import java.io.*;
import java.net.*;
import java.util.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException; 
import java.sql.ResultSet;
import java.sql.Statement;

public class scatterPlotly {

  private static String db_url ="jdbc:mysql://localhost:3306/fwdstudy?autoReconnect=true";
  private static String db_driver = "org.gjt.mm.mysql.Driver";
  private static String db_user = "root";
  private static String db_password = ""; 


  public static void main(String args[]) {
  // java -classpath $CLASSPATH:mysql-connector-java-3.1.12-bin.jar scatterPlotly fwd_plos2 vsim size_change 12 n 200 Plos\ vsim\ vs\ size\ interval\ 12

    int rowcount = 0;
    String database = args[0];
    String field1 = args[1];
    String field2 = args[2];
    String interval = args[3];
    String logScale = args[4]; // y or n
    int threshold = Integer.valueOf(args[5]);
    String label = args[6];

    ArrayList x = new ArrayList();
    ArrayList y = new ArrayList();

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

        System.out.println("<html>");
        System.out.println("  <head>");
        System.out.println("    <!-- Plotly.js -->");
        System.out.println("     <script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>");
        System.out.println("  </head>");
        System.out.println("  <body>");
        System.out.println("    <div id=\"myDiv\" style=\"width: 480px; height: 400px;\"><!-- Plotly chart will be drawn inside this DIV --></div>");
        System.out.println("    <script>");
        System.out.println("      var data = [{");

        String nestedQuery = "select * from " + database + " order by retrieve_interval";
        Statement stmt2 = con.createStatement();
        ResultSet rs2 = null;
        rs2 = stmt2.executeQuery(nestedQuery);
        while (rs2.next()) {
            String thisInterval = rs2.getString("retrieve_interval");
            // if (!(thisInterval.equals("0"))) {
            if (thisInterval.equals(interval)) {
            String field1Val = rs2.getString(field1);
            if (field1Val.equals("") || field1Val.equals("nan")) { field1Val = "0"; }
            String field2Val = rs2.getString(field2);
            if (field2Val.equals("") || field2Val.equals("nan") || field2Val.equals("failed") || field2Val.equals("incomparable")) { field2Val = "0"; }
            String thisId = rs2.getString("id");
            // System.out.println(thisInterval + "," + thisId + ","  + (Math.abs(Double.valueOf(field1Val))) + "," + (Math.abs(Double.valueOf(field2Val))));
            // System.out.println((Math.abs(Double.valueOf(field1Val))) + "," + (Math.abs(Double.valueOf(field2Val))));
            x.add(Math.abs(Double.valueOf(field1Val)));
            // y.add(Math.log10(Math.abs(Double.valueOf(field2Val))));
            // y.add(Math.log(Math.abs(Double.valueOf(field2Val))) / Math.log(100));
            if (logScale.equals("y")) {
              y.add(Math.log1p(Math.abs(Double.valueOf(field2Val))));
            } else 
              if (Math.abs(Double.valueOf(field2Val))>threshold) {
                 y.add(threshold);
             }  
             else {
                 y.add(Math.abs(Double.valueOf(field2Val)));
            }
            rowcount++;
        }
        }
        rs2.close();

        System.out.println("x: " + x);
        System.out.println(",");
        System.out.println("y: " + y);
        System.out.println(",");
  
        System.out.println("    mode: 'markers',");
        System.out.println("    type: 'scatter'");
        System.out.println("    }];");

        System.out.println("      var layout = {");
        System.out.println("      annotations: [");
        System.out.println("        {");
        System.out.println("          font: {size: 4},");
        System.out.println("          showarrow: false,");
        System.out.println("          text: \"PLOS vsim vs sim\",");
        System.out.println("          xanchor: \"left\",");
        System.out.println("          xref: \"vsim\",");
        System.out.println("          yanchor: \"bottom]\",");
        System.out.println("          yref: \"sim\"");
        System.out.println("        }");
        System.out.println("      ],");
        System.out.println("      height: 525,");
        System.out.println("      hovermode: \"closest\",");
        System.out.println("      margin: {");
        System.out.println("        r: 200,");
        System.out.println("        t: 100,");
        System.out.println("        b: 80,");
        System.out.println("        l: 80");
        System.out.println("      },");
        System.out.println("      plot_bgcolor: \"rgb(255,255,255)\",");
        System.out.println("      title: \"" + label + "\",");
        System.out.println("      width: 650,");
        System.out.println("      xaxis: {");
        System.out.println("        title: '" + field1 + "',");
        System.out.println("        mirror: true,");
        System.out.println("        showgrid: true,");
        System.out.println("        showline: true,");
        System.out.println("        showticklabels: true,");
        System.out.println("        ticks: \"\",");
        System.out.println("        zeroline: false");
        System.out.println("      },");
        System.out.println("      yaxis: {");
        System.out.println("        title: '" + field2 + "',");
        System.out.println("        mirror: true,");
        System.out.println("        showgrid: true,");
        System.out.println("        showline: true,");
        System.out.println("        showticklabels: true,");
        System.out.println("        ticks: \"\",");
        System.out.println("        zeroline: false");
        System.out.println("      }");
        System.out.println("    };");


        System.out.println("    Plotly.newPlot('myDiv', data, layout);");
        System.out.println("    </script>");
        System.out.println("  </body>");


    } catch (Exception e) {
      System.out.println("Something went wrong: " + e);
    } 
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
