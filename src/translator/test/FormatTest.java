package translator.test;

import java.text.NumberFormat;

import svgrenderer.shapes.converters.SvgFormatting;

public class FormatTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String result;
        NumberFormat svgDecimalFormat = NumberFormat.getInstance();
        //DecimalFormat svgDecimalFormat = (DecimalFormat) DecimalFormat.getInstance();
        svgDecimalFormat.setMaximumFractionDigits(2);
        //svgDecimalFormat.setDecimalSeparatorAlwaysShown(false);
        //System.out.println(svgDecimalFormat.isDecimalSeparatorAlwaysShown());
        svgDecimalFormat.setGroupingUsed(false);
        result = svgDecimalFormat.format(1009.71);
        /*if(result.indexOf(",") > 0){
        	String[] str = result.split(",");
        	result = str[0] + str[1];
        }*/
        //result = Double.toString(coordinate);
        System.out.println(result);

	}

}
