/* Copyright 2010 Speech and Language Technologies Lab, The Ohio State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.Vector;

/**
 * Common class for calculating the Pearson Correlation Coefficient
 *
 * More information can be found <a href="http://en.wikipedia.org/wiki/Pearson_product-moment_correlation_coefficient">here</a>.
 *
 * @author weale
 * @version 1.0
 * 
 */
public class Pearson {

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
        meanX += xVect.elementAt(i);
        meanY += yVect.elementAt(i);
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
