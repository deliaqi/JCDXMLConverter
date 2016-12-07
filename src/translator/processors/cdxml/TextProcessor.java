package translator.processors.cdxml;

import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.image.BufferedImage;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import translator.ParseElementDefinition;
import translator.ParsedElement;
import translator.cddom.properties.BoundingBox;
import translator.cdxml.CDXMLParser;
import translator.chemistry.PeriodicTable;
import translator.graphics.Color;
import translator.graphics.Font;
import translator.graphics.StyleElement;
import translator.graphics.shapes.builders.configurations.CompositeTextConfiguration;
import translator.graphics.shapes.builders.configurations.TextConfiguration;
import translator.utils.Point;

public class TextProcessor extends CDXMLProcessor {
    
    private static String RIGHT = "Right";
    private static String LEFT = "Left";
    private static String CENTER = "Center";
    private static String FULL = "Full";
    
    private static final int RENDER_CONTEXT_WIDTH = 100;
    private static final int RENDER_CONTEXT_HIGHT = 100;
    
    private static final char DECIMAL_SEPARATOR = new DecimalFormatSymbols().getDecimalSeparator();
    private static final char[] DASHED_CHARS = new char[]{'-'};
    
    private CompositeTextConfiguration compositeResultingConfiguration = new CompositeTextConfiguration();
    protected ParsedElement text = null;
    protected translator.utils.Point textPosition;
    private double lineHeight = 0;
    
    private FontRenderContext renderContext  =  null;
      
    public TextProcessor() {
        renderContext = getRenderContext();
    }
    
    protected void process() {
        text = getElement();
        try {
            
            textPosition = getPosition();
            
            double x = textPosition.getX();
            double y = textPosition.getY();
            String stringValue = "";
            int[] styles = new int[0];
            
            List<ParsedElement> subElements = text.getElements(ParseElementDefinition.STRING);
                              
            if(boundingBox != null){
                
                String[] boundingBoxArray = boundingBox.trim().split(" ");
                
                //This is for fix files created with old ChemDraw C++ versions.
                if(Double.parseDouble(boundingBoxArray[0]) == x && Double.parseDouble(boundingBoxArray[1]) == y){
                    
                    float fontSize = Float.parseFloat(subElements.get(0).getAttribute(ParseElementDefinition.STRING_SIZE));
                
                    TextLayout charLayout = environment.createTextLayout("O", x, y, subElements.get(0).getAttribute(ParseElementDefinition.STRING_FONT), 0, fontSize, 0);
                    
                    double xOffset = charLayout.getAdvance()/2;
                    double yOffset = charLayout.getAscent()/2;
                    
                    x -= xOffset;
                    y += yOffset;
                    
                }
            }
            
            compositeResultingConfiguration = new CompositeTextConfiguration(text.getId());
            
            TextConfiguration currentLine = null;
            boolean usePreviousLine = false;
            int numberOfLines = 0;
            
            //if the text element is part of an ObjectTag we must obtain the zOrder in this point
            //because the configure method don't do that
            setZOrder(text.getZOrder());
            
            if (text.hasAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE)) {
                double rotationAngle = environment.createAngle(text.getAttribute(ParseElementDefinition.TEXT_ROTATION_ANGLE));
                
                compositeResultingConfiguration.setRotationAngle((int)rotationAngle);
            }
            
            if (text.hasAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT)) {
                String labelAlignment = text.getAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT);
                
                if(labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_ABOVE)){
                    compositeResultingConfiguration.setAlignment(TextConfiguration.ABOVE_ALIGNMENT);
                }
            }
            
            String justification = text.getAttribute(ParseElementDefinition.TEXT_JUSTIFICATION);
            if(justification.equals(RIGHT)){
                compositeResultingConfiguration.setJustification(TextConfiguration.RIGHT_JUSTIFICATION);
            } else if(justification.equals(LEFT)){
                compositeResultingConfiguration.setJustification(TextConfiguration.LEFT_JUSTIFICATION);
            } else if(justification.equals(CENTER)){
                compositeResultingConfiguration.setJustification(TextConfiguration.CENTER_JUSTIFICATION);
            }else if (justification.equals(FULL)){
                compositeResultingConfiguration.setJustification(TextConfiguration.FULL_JUSTIFICATION);
            }
            
            if (text.hasAttribute(ParseElementDefinition.TEXT_VISIBLE)) {
                if (text.getAttribute(ParseElementDefinition.TEXT_VISIBLE).equalsIgnoreCase(ParseElementDefinition.TEXT_VISIBLE_NO)) {
                    compositeResultingConfiguration.setDisplay(false);
                }
            }
            
            lineHeight = environment.getLineHeight(text);
            compositeResultingConfiguration.setLineHeight(lineHeight);
            boolean first = true;
            
            // loop the text runs
            for(ParsedElement textRun : subElements){
                
                stringValue = textRun.getValue();
                
                // If textRun is the first subElement, has above alignment and starts with an "\n"
                // char the "\n" char is omitted.
                if(first && stringValue.startsWith("\n")){
                    if (text.hasAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT)) {
                        String labelAlignment = text.getAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT);
                        if(labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_ABOVE)){
                            stringValue = stringValue.substring(1);
                        }
                    }
                }
                
                first = false;
                
                Font stringFont = Font.createFromElement(textRun, renderContext);
                
                ParsedElement colorElement = null;
                if(textRun.getElements(ParseElementDefinition.TEXT_COLOR).size() > 0){
                    colorElement = textRun.getElements(ParseElementDefinition.TEXT_COLOR).get(0);
                }else{
                    colorElement = text.getElements(ParseElementDefinition.TEXT_COLOR).get(0);
                }
                
                Color stringColor = convertColor(colorElement);
                
                // build lines to contain text runs with different formats
                // and store them in the CompositeText
                
                // keep track of new lines positions
                int lastNewLineIndex = -1;
                int newLineIndex = stringValue.indexOf("\n", lastNewLineIndex);
                
                // there are no line breaks in this text run
                if (newLineIndex == -1 && stringValue.length()>0) {
                    // if this is the first text run, there will be no lines created
                    if (currentLine == null || !usePreviousLine) {
                        currentLine = new TextConfiguration(x, numberOfLines++ * lineHeight + y, 0);
                        compositeResultingConfiguration.addLine(currentLine);
                        usePreviousLine = true;
                    }
                    String substring = stringValue.substring(lastNewLineIndex + 1);
                    currentLine.addPart(substring, stringFont, stringColor);
                } else {
                    // loop while there are more new lines
                    while (newLineIndex != -1 && lastNewLineIndex < stringValue.length() - 1) {
                        String substring = stringValue.substring(lastNewLineIndex + 1, newLineIndex);
                        
                        if (!usePreviousLine) {
                            // line was not created in the previous text run
                            // create it here
                            currentLine = new TextConfiguration(x, numberOfLines++ * lineHeight + y, 0);
                            compositeResultingConfiguration.addLine(currentLine);
                        } else {
                            // make the next line be created here
                            usePreviousLine = false;
                        }
                        
                        // add a new substring with font and color of the text run
                        currentLine.addPart(substring, stringFont, stringColor);
                        
                        // update new line indexes
                        lastNewLineIndex = newLineIndex;
                        newLineIndex = stringValue.indexOf("\n", lastNewLineIndex + 1); // add 1 to lastIndex because it is now pointing to a \n
                    }
                    
                    // add the last piece when the text run does not end with a \n
                    if (lastNewLineIndex < stringValue.length() - 1) {
                        currentLine = new TextConfiguration(x, numberOfLines++ * lineHeight + y, 0);
                        compositeResultingConfiguration.addLine(currentLine);
                        
                        // make the next text run use this line
                        usePreviousLine = true;
                        
                        String substring = stringValue.substring(lastNewLineIndex + 1);
                        currentLine.addPart(substring, stringFont, stringColor);
                    }
                }
            }
            
            //Get the biggest line length of the composite.
            ((CompositeTextConfiguration)compositeResultingConfiguration).setBiggestWholeLineLenght(calculateBiggestLineLenght(justification));
            
            //If the text has Alignment equals to Full, then, justify it.
            if (justification.equals(FULL)) {
                calculateWordSpacing();
            }
            
            getEnvironment().getCompositeTextConfigurations().put(getElement().getId(), compositeResultingConfiguration);
            compositeResultingConfiguration.setZOrder(zOrder);
            
            getEnvironment().getCompositeTextConfigurations().put(getElement().getId(), compositeResultingConfiguration);
            
            Area textArea = getEnvironment().createAreaFromCompositeText(compositeResultingConfiguration);
            
            Rectangle2D textBounds = textArea.getBounds2D();            
            
            compositeResultingConfiguration.setBoundingBox(new BoundingBox(new Point(textBounds.getX(), textBounds.getY()), textBounds.getWidth(), textBounds.getHeight()));
            
            setResultingConfiguration(compositeResultingConfiguration);
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        }
    }
    
    protected void cleanup() {
        super.cleanup();
    }
    
    protected FontRenderContext getRenderContext(){
        Graphics2D graphics = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB));
        // The following rendering hint takes into account sub-pixel accuracy.
        // When this hint is set on, getAdvance() method returns size of glyph in real number, not real Integer only
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        FontRenderContext renderContext = graphics.getFontRenderContext(); 
        return renderContext;
    }
    
    /*
     * This method calculates the biggest line lenght in the whole composite.
     * This also set the lenght and the amount of white spaces of each line.
     */
    private double calculateBiggestLineLenght(String alignment) {
        
        int linesCount = ((CompositeTextConfiguration)compositeResultingConfiguration).getLines().size();
        double nonWhiteSpaceLenght = 0;
        double whiteSpaceLenght = 0;
        int whiteSpacesCount = 0;
        double biggestLineLenght = ((CompositeTextConfiguration)compositeResultingConfiguration).getBiggestWholeLineLenght();
        
       //Lines
        for (int lineIndex = 0; lineIndex <= linesCount - 1; lineIndex++) {
            
            TextConfiguration currentLine = ((CompositeTextConfiguration)compositeResultingConfiguration).getLines().get(lineIndex);
            double x = currentLine.getX();
            double y = currentLine.getY();
            translator.graphics.Font font = null;
            //Parts
            for (int partsIndex = 0; partsIndex <= currentLine.getParts().size() - 1; partsIndex++) {
                
                //Word-spacing for SVG format, does not get the right spacing for the case where there is a white space in between strings,
                //and that white space is concatenated to the beginning of the second string.
                //For instance: "This" and " is a sample". They have to seems like: "This " and "is a sample".
                if (alignment.equals(FULL) && partsIndex < currentLine.getParts().size() - 1 && currentLine.getParts().size() >= 2) {
                    String currentPart = currentLine.getParts().get(partsIndex);
                    String nextPart = currentLine.getParts().get(partsIndex + 1);
                    if (!currentLine.getParts().get(partsIndex).endsWith(" ") && currentLine.getParts().get(partsIndex + 1).startsWith(" ")
                    && nextPart.length() >= 2) {
                        currentLine.getParts().set(partsIndex, currentPart + " ");
                        currentLine.getParts().set(partsIndex + 1,  nextPart.substring(1, nextPart.length()));
                    }
                }
                
                String part = currentLine.getParts().get(partsIndex);
                font = currentLine.getFont(String.valueOf(partsIndex));
                
                for (int charsIndex = 0; charsIndex <= part.length() - 1; charsIndex++) {
                    String subpart = part.substring(charsIndex, charsIndex + 1);
                    
                    TextLayout charLayout = new TextLayout(subpart, environment.createPlatformFontFromFont(font, lineHeight), renderContext);
                    
                    //Respect the original blank quantity. There could be more than one blank in between strings.
                    if (subpart.equals(" ")) {
                        if (charsIndex == (part.length() - 1) && subpart.equals(" ") && partsIndex == currentLine.getParts().size() - 1) {
                            //We delete the remaining blank space at the end of the line.
                            part = part.substring(0, part.length() - 1);
                        } else {
                            whiteSpacesCount++;
                            whiteSpaceLenght += charLayout.getAdvance();
                        }
                    } else {
                        nonWhiteSpaceLenght += charLayout.getAdvance();
                    }
                }
            }
            currentLine.setWholeLineLenght(nonWhiteSpaceLenght + whiteSpaceLenght);
            currentLine.setWhiteSpacesCount(whiteSpacesCount);
            nonWhiteSpaceLenght = 0;
            whiteSpaceLenght = 0;
            whiteSpacesCount = 0;
        }
        //Get the bigger width from all lines.
        for (int lineIndex = 0; lineIndex <= linesCount - 1; lineIndex++) {
            //Get the biggest line lenght of the whole paragraph. Do not take the bounding box as reference
            //because it does not fit the real text length, it is a little bit bigger.
            TextConfiguration currentLine = ((CompositeTextConfiguration)compositeResultingConfiguration).getLines().get(lineIndex);
            if (currentLine.getWholeLineLenght() > biggestLineLenght) {
                biggestLineLenght = currentLine.getWholeLineLenght();
            }
        }
        return biggestLineLenght;
    }
    
    /*
     *This method calculates the inter-word space in addition to the default space between words.
     */
    private void calculateWordSpacing(){
       int linesCount = ((CompositeTextConfiguration)compositeResultingConfiguration).getLines().size();
       double biggestLineLenght = ((CompositeTextConfiguration)compositeResultingConfiguration).getBiggestWholeLineLenght();
        //Set the word-spacing for each line.
        //The last line will not be justified.
        for (int lineIndex = 0; lineIndex < linesCount - 1; lineIndex++) {
            TextConfiguration currentLine = ((CompositeTextConfiguration)compositeResultingConfiguration).getLines().get(lineIndex);
            //Get the word-spacing from the real occupated space divided by the white space count.
            if (currentLine.getWhiteSpacesCount() > 0 && currentLine.getWholeLineLenght() < biggestLineLenght) {
                currentLine.setWordSpacing((biggestLineLenght - currentLine.getWholeLineLenght()) / currentLine.getWhiteSpacesCount());
            }
        }
    }
    
    /**
     * Return the length of the element at the start of the specified label.
     */
    private static int parseOneElement(char[] label, StyleElement[] styles, int labelLength, boolean dontSwapHydrogens) {
        
        // Taken from C++ code
        
        /*	An atom consists of an optional prefix, an uppercase letter,
         *	zero or more lowercase letters, followed by zero or more subscripted digits.
         *
         *	A prefix consists of either a lowercase letter followed by a hyphen, minus, or dash,
         *	or any number of superscripted digits.
         *
         *	If the atom ends with a lowercase letter and the next char is a hyphen, minus, or dash,
         *	then the lowercase letter is not considered part of this atom.
         *
         *	If the first character in the atom is a hyphen, minus, dash, or plus, then the atom
         *	consists of that character and any following digits.
         *
         *	If the first character is anything else, the atom consists of just that character.
         *
         *	If the text starts with a shortcut name longer than the atom found by the above rules,
         *	then the shortcut name length is returned.
         *
         *	Any subscripted char is considered part of the atom, not just a digit.
         *	This handles labels like C<SUB>e</SUB>.
         *	If the entire remainder of the text is superscripted, then the superscript is
         *	considered part of the atom as well.  This handles labels like R<SUP>1</SUP>.
         */
        
        int result;
        
        int i; // used as index to iterate through the label's characters
        
        StringBuilder charsPart = new StringBuilder();
        
        if (labelLength >= 3 &&
                Character.isLowerCase(label[0]) &&
                isDashedChar(label[1]) &&
                Character.isUpperCase(label[2])) {
            i = 3;
        } else if (isDashedChar(label[0]) || label[0] == '+') {
            
            for (i = 1;  i < labelLength && Character.isDigit(label[i]);  i++);
            result = i;
            return result;
        } else if (Character.isDigit(label[0]) && !styles[0].isSuperscript()) {
            // a sequence of non-superscripted digits is a token
            for (i = 0; i < labelLength && Character.isDigit(label[i]) && !styles[i].isSuperscript(); ++i);
            result = i;
            return result;
        } else {
            for (i = 0; i < labelLength &&
                    Character.isDigit(label[i]) &&
                    styles[i].isSuperscript();
            ++i);
            
            if (!Character.isUpperCase(label[i])) {
                result = i + 1;
                return result;
            } else {
                charsPart.append(label[i]);
                i++;
            }
        }
        
        // Treat labels like C6H5 as a single atom
        if (label[i-1] == 'C' && i < labelLength &&
                Character.isDigit(label[i]) && styles[i].isSubscript()) {
            for (i++; i < labelLength; i++) {
                if (!Character.isDigit(label[i]) || !styles[i].isSubscript()) {
                    break;
                }
            }
            
            // Allow a superscripted number here, e.g. C62H5 to indicate perdeuterophenyl
            for (; i < labelLength; i++) {
                if (!Character.isDigit(label[i]) || !styles[i].isSuperscript()) {
                    break;
                }
            }
            
            if (i >= labelLength) {
                result = i;
                return result;
            } else {
                if (!Character.isUpperCase(label[i])) {
                    result = i + 1;
                    return result;
                } else {
                    charsPart = new StringBuilder();
                    i++;
                }
            }
        }
        
        for (; i < labelLength; i++) {
            if (styles[i].isSubscript() || styles[i].isSuperscript() || !Character.isLowerCase(label[i])) {
                break;
            } else {
                charsPart.append(label[i]);
            }
        }
        
        for (; i < labelLength; i++) {
            // Any subscripted char is part of this atom
            if (styles[i].isSubscript()) {
                continue;
            }
            
            // Digits in formula style are subscripted, and thus part of this atom.
            if (Character.isDigit(label[i]) && styles[i].isFormula()) {
                continue;
            }
            
            // Any number of primes are part of the label
            if (label[i] == '\'') {
                continue;
            }
            
            break;
        }
        
        // If all the remaining chars are superscripts, they're part of this atom, too.
        int countSupers;
        boolean supersAreDigits = true;
        for (countSupers = i; countSupers < labelLength; countSupers++) {
            // Any non-superscripted char means there's another atom
            // A superscripted charge is *not* part of the atom
            if (isDashedChar(label[countSupers]) ||
                    label[countSupers] == '+' ||
                    !styles[countSupers].isSuperscript()) {
                // Superscripted digits are not part of the atom if also followed by a superscripted charge
                if (countSupers > i && supersAreDigits) {
                    countSupers = i;
                }
                break;
            }
            if (!Character.isDigit(label[countSupers]) && label[countSupers] != '.') {
                supersAreDigits = false;
            }
        }
        
        // If there's nothing left but superscripts, they're part of this atom
        if (countSupers == labelLength) {
            i = labelLength;
        }
        // if the superscript is nondigits, or if the following thing is not an element, they're part of this atom
        else {
            // replace this decision with a lookup in periodic table
            char[] element = new char[labelLength];
            System.arraycopy(label, countSupers - 1, element, 0, labelLength - countSupers);
            boolean isChemicalElement = PeriodicTable.getTable().isElement(new String(element));
            if (countSupers > i &&
                    (!supersAreDigits || !isChemicalElement)) {
                i = countSupers;
            }
        }
        
        if (labelLength - i >= 2 &&
                Character.isLowerCase(label[i - 1]) &&
                isDashedChar(label[i]) &&
                Character.isUpperCase(label[i + 1])) {
            --i;
        }
        
        if (dontSwapHydrogens &&
                charsPart.length() > 0 &&                   // actually found some probably-elemental text
                !charsPart.toString().equals("H") &&        // didn't find a single hydrogen to start with
                i < labelLength &&                         // there are some more characters
                label[i] == 'H' &&                          // the next character is an H
                (i + 1 == labelLength || !Character.isLetter(label[i + 1])))	// the char after the H isn't itself alphabetic
        {
            // See if we can grab any hydrogens
            char[] labelSubstring = new char[labelLength - i];
            System.arraycopy(label, i, labelSubstring, 0, labelLength - i - 1);
            int hLength = parseOneElement(labelSubstring, styles, labelLength - i, dontSwapHydrogens);
            i += hLength;
        }
        
        result = i;
        return result;
    }
    
    /**
     * Exchange the first numberOfChars characters in the text with the following
     * labelLength - numberOfChars characters, and adjust the styles to match.
     */
    private static void swapStyles(char[] label, StyleElement[] styles, ParsedElement[] colors, Font[] fonts, int numberOfChars, int labelLength) {
        char[] temporaryChars = new char[numberOfChars];
        StyleElement[] temporaryStyles = new StyleElement[numberOfChars];
        ParsedElement[] temporaryColors = new ParsedElement[numberOfChars];
        Font[] temporaryFonts = new Font[numberOfChars];
        
        // copy start elements to temporary
        System.arraycopy(label, 0, temporaryChars, 0, numberOfChars);
        System.arraycopy(styles, 0, temporaryStyles, 0, numberOfChars);
        System.arraycopy(colors, 0, temporaryColors, 0, numberOfChars);
        System.arraycopy(fonts, 0, temporaryFonts, 0, numberOfChars);
        
        // move end side elements to start side
        System.arraycopy(label, numberOfChars, label, 0, labelLength - numberOfChars);
        System.arraycopy(styles, numberOfChars, styles, 0, labelLength - numberOfChars);
        System.arraycopy(colors, numberOfChars, colors, 0, labelLength - numberOfChars);
        System.arraycopy(fonts, numberOfChars, fonts, 0, labelLength - numberOfChars);
        
        // copy temporary back to the end
        System.arraycopy(temporaryChars, 0, label, labelLength - numberOfChars, numberOfChars);
        System.arraycopy(temporaryStyles, 0, styles, labelLength - numberOfChars, numberOfChars);
        System.arraycopy(temporaryColors, 0, colors, labelLength - numberOfChars, numberOfChars);
        System.arraycopy(temporaryFonts, 0, fonts, labelLength - numberOfChars, numberOfChars);
    }
    
    /**
     * Reverses the order of the elements inside a label recursively,
     * updating style information accordingly.
     */
    private static void swapLabel(char[] theLabel, StyleElement[] styles, ParsedElement[] colors, Font[] fonts, int titleLen, boolean dontSwapHydrogens) {
        // Taken from C++ code
        
        // If we're looking at a NOT-atom list, don't swap anything.
        if (!(titleLen > 4 && new String(theLabel).startsWith("NOT "))) {
            int remaining = titleLen;
            int i, j, level;
            do {
                if (isOpenParenthesis(theLabel[0])) {
                    level = 1;
                    j = 0;
                    do {
                        ++j;
                        if (isOpenParenthesis(theLabel[j])) {
                            ++level;
                        } else if (isCloseParenthesis(theLabel[j])) {
                            --level;
                        }
                    } while (j < remaining && level > 0);
                    
                    if (level != 0) {
                        i = 1;	// unbalanced open parenthesis
                    } else {
                        // Now theLabel[0] is the open paren, theLabel[j] is the close.
                        // Search for subscripted digits after the close - they stay with the close paren.
                        
                        for (i = j + 1;  i < remaining;  ++i) {
                            if (!Character.isDigit(theLabel[i]) || !styles[i].isSubscript()) {
                                break;
                            }
                        }
                        
                        // Special case not-element lists such as [-F,Cl,Br] and [NOT F,Cl,Br]
                        int prefixSize;
                        if (j > 5 && new String(theLabel).startsWith("[NOT ")) {
                            prefixSize = 5;
                        } else if (j > 2 && theLabel[0] == '[' && isDashedChar(theLabel[1])) {
                            prefixSize = 2;
                        } else {
                            prefixSize = 1;
                        }
                        
                        // Allow additional spaces after the NOT or -
                        while (prefixSize < j && theLabel[prefixSize] == ' ') {
                            ++prefixSize;
                        }
                        
                        // Now i is the total size of the parenthesized expression,
                        // including trailing digits.  j is the number of characters
                        // prior to the right paren, including the open paren and NOT (if present).
                        swapStyles(theLabel, styles, colors, fonts, prefixSize, i);                    // Put the open paren at the end
                        swapLabel(theLabel, styles, colors, fonts, j - prefixSize, dontSwapHydrogens); // swap the part inside the parens
                        swapStyles(theLabel, styles, colors, fonts, j - prefixSize, i);                // Put the part inside the parens at the end
                        swapStyles(theLabel, styles, colors, fonts, i - j, i);                         // Put the close paren at the end
                    }
                } else { // no open parenthesis
                    i = parseOneElement(theLabel, styles, remaining, dontSwapHydrogens);
                }
                
                if (i < remaining) {
                    swapStyles(theLabel, styles, colors, fonts, i, remaining);
                }
                remaining -= i;
            } while (remaining > 0);
        }
    }
    
    private static boolean isOpenParenthesis(char c) {
        return c == '(' || c == '[' || c == '{';
    }
    
    private static boolean isCloseParenthesis(char c) {
        return c == ')' || c == ']' || c == '}';
    }
    
    private static boolean isDashedChar(char c) {
        boolean isDashed = false;
        for (int i = 0; i < DASHED_CHARS.length; i++) {
            if (c == DASHED_CHARS[i]) {
                isDashed = true;
                break;
            }
        }
        return isDashed;
    }
    
    private static boolean isBulletChar(char c) {
        return c == '·';
    }
    
    /**
     * Analyzes the specified text element, breaking it down into characters,
     * and setting subscript and superscript attributes according to chemical rules.
     *
     * A text element contains one or more string elements inside.
     * Each string element has a <code>value</code> inside which is the real <code>String</code> instance.
     */
    public static void processFormula(ParsedElement text) {
        
        Graphics2D graphics = GraphicsEnvironment.getLocalGraphicsEnvironment().createGraphics(
                new BufferedImage(RENDER_CONTEXT_WIDTH, RENDER_CONTEXT_HIGHT, BufferedImage.TYPE_INT_RGB));
        // The following rendering hint takes into account sub-pixel accuracy.
        // When this hint is set on, getAdvance() method returns size of glyph in real number, not real Integer only
        graphics.setRenderingHint(java.awt.RenderingHints.KEY_FRACTIONALMETRICS, java.awt.RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        FontRenderContext renderContext = graphics.getFontRenderContext(); 
        
        List<ParsedElement> strings = text.getElements(ParseElementDefinition.STRING);
        
        // remove all current strings, they will be recreated at the end
        text.removeAllElements(strings);
        
        // formula style strings might be split into several strings
        // store the final result in resultingStrings
        List<ParsedElement> resultingStrings = new ArrayList();
        
        StringBuilder wholeTextBuilder = new StringBuilder();
        
        // stores the indexes where each substring starts
        int[] substringIndexes = new int[strings.size() + 1];
        int currentString = 0;
        
        // stores the final values after being modified
        String[] values = new String[strings.size()];
        
        String labelAlignment = null;
        if (text.hasAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT)) {
            if (text.hasAttribute(ParseElementDefinition.TEXT_LABEL_DISPLAY) &&
                    !text.getAttribute(ParseElementDefinition.TEXT_LABEL_DISPLAY).equals(ParseElementDefinition.TEXT_ALIGNMENT_AUTO)) {
                // set alignment to left so there is no re-ordering
                labelAlignment = ParseElementDefinition.TEXT_ALIGNMENT_LEFT;
                
                // set justification with the overridden value, if it is right or center
                String labelDisplay = text.getAttribute(ParseElementDefinition.TEXT_LABEL_DISPLAY);
                if (labelDisplay.equals(ParseElementDefinition.TEXT_ALIGNMENT_RIGHT) ||
                        labelDisplay.equals(ParseElementDefinition.TEXT_ALIGNMENT_CENTER)) {
                    text.addAttribute(ParseElementDefinition.TEXT_LABEL_JUSTIFICATION, text.getAttribute(ParseElementDefinition.TEXT_LABEL_DISPLAY));
                }
            } else {
                labelAlignment = text.getAttribute(ParseElementDefinition.TEXT_LABEL_ALIGNMENT);
            }
        }
        
        // concatenate all strings into one
        for (ParsedElement string : strings) {
            String value = string.getValue();
            
            // remove line breaks before processing formula, they will be
            // added properly later when processing label alignment
            if (labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_ABOVE) ||
                    labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_BELOW)) {
                value = value.replaceAll("\n", "");
            }
            
            substringIndexes[currentString + 1] = substringIndexes[currentString] + value.length();
            wholeTextBuilder.append(value);
            
            values[currentString] = value;
            
            currentString++;
        }
        
        // the whole concatenated text as a <code>String</code> instance
        String wholeTextString = wholeTextBuilder.toString();
        
        // the whole concatenated text as a <code>char</code> array
        char[] wholeText = wholeTextString.toCharArray();
        
        // styles per character
        StyleElement[] styles = new StyleElement[wholeText.length];
        // font per character
        Font[] fonts = new Font[wholeText.length];
        // color per character
        ParsedElement[] colors = new ParsedElement[wholeText.length];
        
        // initialize styles, fonts and colors per character
        currentString = 0;
        for(ParsedElement string : strings) {
            Font stringFont = Font.createFromElement(string, renderContext);
            ParsedElement stringColor = string.getElements(ParseElementDefinition.COLOR).get(0);
            for (int currentChar = substringIndexes[currentString]; currentChar < substringIndexes[currentString + 1]; currentChar++) {
                styles[currentChar] = stringFont.getStyle();
                fonts[currentChar] = stringFont;
                colors[currentChar] = stringColor;
            }
            currentString++;
        }
        
        // Taken from C++ code
        // count number of dashes
        int numberOfDashes = 0;
        for (int c = 0; c < wholeText.length; c++) {
            if (isDashedChar(wholeText[c])) {
                numberOfDashes++;
            }
        }
        
        // process all characters with formula style
        boolean firstFormulaStyle = false;
        // iterate strings inside text
        for (currentString = 0; currentString < strings.size(); currentString++) {
            ParsedElement string = strings.get(currentString);
            
            // value of the current string
            String value = values[currentString];
            
            // the value of the current string as a char array
            char[] characters = value.toCharArray();
            
            // iterate through characters inside the string
            for (int currentChar = 0; currentChar < characters.length; currentChar++) {
                // the currently analyzed character
                char thisChar = characters[currentChar];
                
                // the character's index in the whole text
                int thisIndex = substringIndexes[currentString] + currentChar;
                
                // previous character in text
                char previousChar = 0;
                if (thisIndex > 0) {
                    previousChar = wholeText[thisIndex - 1];
                }
                
                if (styles[thisIndex].isFormula()) {
                    
                    // set this flag so next formula styles know they aren't first
                    if (!firstFormulaStyle) {
                        firstFormulaStyle = true;
                    }
                    
                    // Taken from C++ code
                    
                    // first character in formula style
                    if (firstFormulaStyle && currentChar == 0) {
                        // if there are no previous strings
                        if (currentString == 0) {
                            // leave character normal
                            continue;
                        } else {
                            // previous style wasn't formula and previous character wasn't a letter or a close parenthesis
                            if (styles[thisIndex].isFormula() && !Character.isLetter(previousChar) && !isCloseParenthesis(previousChar)) {
                                // leave character normal
                                continue;
                            }
                        }
                    }
                    
                    // A digit following decimal, always has the same positioning as the previous character
                    if (Character.isDigit(thisChar) && previousChar == DECIMAL_SEPARATOR) {
                        styles[thisIndex] = styles[thisIndex - 1];
                        continue;
                    }
                    
                    // A digit, +, - or bullet character following a space (or tab, or return, or...) or period,
                    // comma, semicolon or colon, or paren, always has normal positioning
                    if ((Character.isDigit(thisChar) || thisChar == '+' ||
                            isDashedChar(thisChar) || isBulletChar(thisChar)) &&
                            (Character.isWhitespace(previousChar) || previousChar == DECIMAL_SEPARATOR ||
                            previousChar == '.' || previousChar == ',' ||
                            previousChar == ':' || previousChar == ';' ||
                            isOpenParenthesis(previousChar))) {
                        continue;
                    }
                    
                    if (thisChar == '+' || isDashedChar(thisChar) || isBulletChar(thisChar)) {
                        // A + or - character preceding a non-space non-digit non-closeparen has normal positioning unless
                        // there is another + or - character somewhere following it in the label ('t-Bu' would
                        // have a non-superscripted dash, but 'COO-Na+' would have a superscripted dash)
                        if (thisIndex + 1 < wholeText.length &&
                                Character.isLetter(wholeText[thisIndex + 1])) {
                            
                            boolean foundAnother = false;
                            
                            if (isDashedChar(thisChar)) {
                                if (wholeTextString.indexOf('+', thisIndex + 1) != -1) {
                                    foundAnother = true;
                                }
                            } else if (thisChar == '+') {
                                for (int i = 0; i < DASHED_CHARS.length; i++) {
                                    if (wholeTextString.indexOf(DASHED_CHARS[i], thisIndex + 1) != -1) {
                                        foundAnother = true;
                                        break;
                                    }
                                }
                            }
                            
                            if (!foundAnother) {
                                continue;
                            }
                        }
                        
                        // A + or - character preceding an explicitly non-formula-style digit will itself have normal positioning (glucose-1-P)
                        if (thisIndex + 1 < wholeText.length &&
                                Character.isDigit(wholeText[thisIndex + 1]) &&
                                currentString + 1 < substringIndexes.length &&
                                substringIndexes[currentString + 1] == thisIndex + 1) {
                            
                            if (!styles[thisIndex + 1].isFormula()) {
                                continue;
                            }
                        }
                        
                        // A - character between two digits, the first of which is subscripted,
                        // where the second is greater than the first
                        // might be intended as a range for a link node. Subscript it.
                        if (isDashedChar(thisChar) &&
                                thisIndex > 0 && Character.isDigit(previousChar) &&
                                styles[thisIndex - 1].isSubscript() &&   // or previous style in whole text
                                thisIndex + 1 < wholeText.length && Character.isDigit(wholeText[thisIndex + 1]) &&
                                ((thisIndex + 2 < wholeText.length && Character.isDigit(wholeText[thisIndex + 2]) ||
                                (wholeText[thisIndex - 1] < wholeText[thisIndex + 1])))) {
                            
                            styles[thisIndex] = styles[thisIndex].deriveSubscript();
                            numberOfDashes--;
                            continue;
                        }
                        
                        if (isDashedChar(thisChar) && numberOfDashes > 1) {
                            continue;
                        }
                        
                        // A + or - character in other circumstances is always superscripted
                        styles[thisIndex] = styles[thisIndex].deriveSuperscript();
                    }
                    
                    if (Character.isDigit(thisChar)) {
                        // A digit following another digit, +, or - character always has the same positioning as the previous character
                        if (Character.isDigit(previousChar) || previousChar == '+' ||
                                isDashedChar(previousChar) || isBulletChar(previousChar)) {
                            styles[thisIndex] = styles[thisIndex - 1];
                            continue;
                        }
                        
                        // A digit in other circumstances is always subscripted
                        styles[thisIndex] = styles[thisIndex].deriveSubscript();
                        continue;
                    }
                    
                    if (thisChar == DECIMAL_SEPARATOR) {
                        // A decimal following a digit has the same positioning as the previous character
                        if (Character.isDigit(previousChar)) {
                            styles[thisIndex] = styles[thisIndex - 1];
                            continue;
                        }
                    }
                } else {
                    // reset this flag to start over with a possible next formula style
                    firstFormulaStyle = false;
                }
            }
        }
        
        // process label alignment
        // this might break the text in 2 lines and/or reorder the elements inside
        if (labelAlignment != null && wholeText.length > 1) {
            // If the first character of the label is a bracket-type character, keep everything on
            // one line, regardless.
            // Taken from C++ code
            if (labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_RIGHT) ||
                    !isOpenParenthesis(wholeText[0])) {
                
                // Taken from C++ code
                if (labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_BELOW)) {
                    int firstElementLength = parseOneElement(wholeText, styles, wholeText.length, false);
                    
                    StringBuilder newText = new StringBuilder();
                    
                    // Add a Return character after the first element
                    newText.append(wholeTextString);
                    newText.insert(firstElementLength, "\n");
                    
                    wholeTextString = newText.toString();
                    wholeText = wholeTextString.toCharArray();
                    
                    // insert a new style where the \n is
                    StyleElement[] newStyles = new StyleElement[styles.length + 1];
                    System.arraycopy(styles, 0, newStyles, 0, firstElementLength);
                    // set the style for \n character the same as previous
                    newStyles[firstElementLength] = newStyles[firstElementLength - 1];
                    System.arraycopy(styles, firstElementLength, newStyles, firstElementLength + 1, styles.length - firstElementLength);
                    styles = newStyles;
                    
                    // insert a new color where the \n is
                    ParsedElement[] newColors = new ParsedElement[colors.length + 1];
                    System.arraycopy(colors, 0, newColors, 0, firstElementLength);
                    // set the color for \n character the same as previous
                    newColors[firstElementLength] = newColors[firstElementLength - 1];
                    System.arraycopy(colors, firstElementLength, newColors, firstElementLength + 1, colors.length - firstElementLength);
                    colors = newColors;
                    
                    // insert a new font where the \n is
                    Font[] newFonts = new Font[fonts.length + 1];
                    System.arraycopy(fonts, 0, newFonts, 0, firstElementLength);
                    // set the font for \n character the same as previous
                    newFonts[firstElementLength] = newFonts[firstElementLength - 1];
                    System.arraycopy(fonts, firstElementLength, newFonts, firstElementLength + 1, fonts.length - firstElementLength);
                    fonts = newFonts;
                } else if (labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_ABOVE)) {
                    // Add a Return character at the end of the title if there isn't one there already
                    // Then take the first atom and put it at the end of the title, producing two (or more) lines
                    int firstElementLength = parseOneElement(wholeText, styles, wholeText.length, false);
                    
                    wholeTextString += "\n";
                    wholeText = wholeTextString.toCharArray();
                    
                    StyleElement[] styleTemp = new StyleElement[styles.length + 1];
                    System.arraycopy(styles, 0, styleTemp, 0, styles.length);
                    styles = styleTemp;
//                    styles = Arrays.copyOf(styles, styles.length + 1);
                    // set the style for \n character the same as previous
                    styles[styles.length - 1] = styles[styles.length - 2];
                    
                    ParsedElement[] colorTemp = new ParsedElement[colors.length + 1];
                    System.arraycopy(colors, 0, colorTemp, 0, colors.length);
                    colors = colorTemp;

                    // set the color for \n character the same as previous
                    colors[colors.length - 1] = colors[colors.length - 2];
                    
                    Font[] fontTemp = new Font[fonts.length + 1];
                    System.arraycopy(fonts, 0, fontTemp, 0, fonts.length);
                    fonts = fontTemp;

                    // set the font for \n character the same as previous
                    fonts[fonts.length - 1] = fonts[fonts.length - 2];
                    
                    // move the first atom to the end of the text array
                    // and update the styles, font and color accordingly
                    swapStyles(wholeText, styles, colors, fonts, firstElementLength, wholeText.length);
                    
                    // update text with changes to the array
                    wholeTextString = new String(wholeText);
                } else if (labelAlignment.equals(ParseElementDefinition.TEXT_ALIGNMENT_RIGHT)) {
                    // reverse the order of the elements in the text array
                    // and update the styles, font and color accordingly
                    swapLabel(wholeText, styles, colors, fonts, wholeText.length, false);
                    
                    // update text with changes to the array
                    wholeTextString = new String(wholeText);
                }
            }
        }
        
        // iterate through the styles to find where they change
        List<Integer> changeStyleIndexes = new ArrayList();
        // start adding from the second style
        for (int i = 1; i < styles.length; i++) {
            
            // if there is any change in style, font or color
            if (!styles[i].equals(styles[i - 1]) ||
                    !equalColors(colors[i], colors[i - 1]) ||
                    !fonts[i].equals(fonts[i - 1])) {
                
                // add the index
                changeStyleIndexes.add(i);
            }
        }
        // add text length as the last index to be able to substring
        changeStyleIndexes.add(styles.length);
        
        // rebuild the strings based on the new styles
        int previousChangeIndex = 0;
        
        // number of digits to pad the string elements' ids
        int numberOfIdDigits = String.valueOf(changeStyleIndexes.size()).length();
        
        for (int i = 0; i < changeStyleIndexes.size(); i++) {
            int currentChangeIndex = changeStyleIndexes.get(i);
            
            StyleElement newStyle = styles[previousChangeIndex];
            
            // obtain the text run with the characters that share the same styling all through
            String substring = wholeTextString.substring(previousChangeIndex, currentChangeIndex);
            
            // create a new string element
            ParsedElement substringElement = new ParsedElement();
            
            substringElement.setId(CDXMLParser.zeroPadString(String.valueOf(i + 1), numberOfIdDigits));
            substringElement.setValue(substring);
            
            substringElement.setName(ParseElementDefinition.STRING);
            substringElement.addElement(colors[previousChangeIndex]);
            
            Font substringFont = fonts[previousChangeIndex];
            if (newStyle.isFormula()) {
                // reset formula style, make it plain (not sub or superscript)
                newStyle = newStyle.deriveFormula();
            }
            substringFont.setStyle(newStyle);
            
            substringElement.addAttribute(ParseElementDefinition.STRING_FACE, substringFont.getStyle().getFace());
            substringElement.addAttribute(ParseElementDefinition.STRING_FONT, substringFont.getName());
            substringElement.addAttribute(ParseElementDefinition.STRING_CHAR_SET, substringFont.getCharSet());
            
            double substringFontSize = Double.parseDouble(substringFont.getSize());
            
            //Validate it the font size is less than 1000.0 and bigger than zero.
            if( substringFontSize > Font.MIN_FONT_SIZE && substringFontSize < Font.MAX_FONT_SIZE ){
                substringElement.addAttribute(ParseElementDefinition.STRING_SIZE, substringFont.getSize());
            }else{
                //If the font size is not valid use the default font size.
                substringElement.addAttribute(ParseElementDefinition.STRING_SIZE, Font.DEFAULT_FONT_SIZE);
            }
            
            // add the string element to the result
            resultingStrings.add(substringElement);
            
            previousChangeIndex = currentChangeIndex;
        }
        
        // set the new string elements after formula processing
        text.addAllElements(resultingStrings);
    }
    
    /**
     * Compares two ParsedElements representing colors
     */
    private static boolean equalColors(ParsedElement colorElement1, ParsedElement colorElement2) {
        Color color1 = convertColor(colorElement1);
        Color color2 = convertColor(colorElement2);
        return color1.equals(color2);
    }
    
    /**
     *return the text position
     */
    protected translator.utils.Point getPosition() {
        translator.utils.Point result;
        ParsedElement text = getElement();
        String location = text.getAttribute(ParseElementDefinition.TEXT_POSITION);
        result = parseCoords(location, text);
        return result;
    }
}
