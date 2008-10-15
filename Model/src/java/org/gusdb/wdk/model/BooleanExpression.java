/**
 * 
 */
package org.gusdb.wdk.model;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * @author Jerric
 * @created Sep 14, 2005
 */
public class BooleanExpression {
    
    private static final String STUB_PREFIX = "__STUB__";
    
    private User user;
    
    private String orgExp;
    
    /**
     * 
     */
    public BooleanExpression( User user ) {
        this.user = user;
    }
    
    /**
     * this function accept a boolean expression that defines the combination of
     * cached answers in current user's step. In the expression answers can
     * be identified by their IDs or names. For example,
     * <code>#1 UNION "ans_gene"</code>, where the answer ID starts with #,
     * and answer name can be quoted by double quote (the double quote is
     * required if there's space in answer name).
     * 
     * User can use parenthese in the boolean expression to change the order of
     * its execution. For example,
     * <code>#1 MINUS (ans_mRNA UNION "ans tRNA")</code>
     * 
     * @param expression
     * @param operandMap
     * @return
     * @throws WdkUserException
     * @throws WdkModelException
     */
    public BooleanQuestionNode parseExpression( String expression,
            Map< String, String > operatorMap ) throws WdkUserException,
            WdkModelException {
        this.orgExp = expression;
        // TEST
        // System.out.println("Expression: " + expression);
        
        // replace the literals in the expression
        Map< String, String > replace = new LinkedHashMap< String, String >();
        expression = replaceLiterals( expression, replace ).trim();
        
        // validate the expression by number of parentheses
        int count = 0;
        for ( int i = 0; i < expression.length(); i++ ) {
            if ( expression.charAt( i ) == '(' ) count++;
            else if ( expression.charAt( i ) == ')' ) count--;
            if ( count < 0 )
                throw new WdkUserException( "Bad parentheses: " + orgExp );
        }
        if ( count != 0 )
            throw new WdkUserException( "Bad parentheses: " + orgExp );
        
        // insert a space before open parenthes; it's used when getting operator
        expression = expression.replaceAll( "\\(", Matcher.quoteReplacement(" (") );
        expression = expression.replaceAll( "\\+", " + " );
        expression = expression.replaceAll( "\\-", " - " );
        // delete extra white spaces
        expression = expression.replaceAll( "\\s", " " ).trim();
        
        // build the BooleanQuestionNode tree, use it to create Steps at internal nodes
        return parseBlock( expression, replace, operatorMap );
    }
    
    public Step parseExpressionStep( String expression,
            Map< String, String > operatorMap ) throws WdkUserException,
            WdkModelException {
        this.orgExp = expression;
        // TEST
        // System.out.println("Expression: " + expression);
        
        // replace the literals in the expression
        Map< String, String > replace = new LinkedHashMap< String, String >();
        expression = replaceLiterals( expression, replace ).trim();
        
        // validate the expression by number of parentheses
        int count = 0;
        for ( int i = 0; i < expression.length(); i++ ) {
            if ( expression.charAt( i ) == '(' ) count++;
            else if ( expression.charAt( i ) == ')' ) count--;
            if ( count < 0 )
                throw new WdkUserException( "Bad parentheses: " + orgExp );
        }
        if ( count != 0 )
            throw new WdkUserException( "Bad parentheses: " + orgExp );
        
        // insert a space before open parenthes; it's used when getting operator
        expression = expression.replaceAll( "\\(", Matcher.quoteReplacement(" (") );
        expression = expression.replaceAll( "\\+", " + " );
        expression = expression.replaceAll( "\\-", " - " );
        // delete extra white spaces
        expression = expression.replaceAll( "\\s", " " ).trim();
        
        // build the BooleanQuestionNode tree, use it to create Steps at internal nodes
	Map<BooleanQuestionNode, Step> stepsMap = new LinkedHashMap<BooleanQuestionNode, Step>();
	BooleanQuestionNode root = parseBlockStep( expression, stepsMap, replace, operatorMap );
	
        return stepsMap.get(root);
    }

    private String replaceLiterals( String expression,
            Map< String, String > replace ) throws WdkUserException {
        // split the expression by quotes, and literals are the even items. If
        // the expression ends with a quote, the String.split() won't be able to
        // separate an extra part beyond the last quote. I have to append a
        // space to get an odd number of parts. It's weird, but works.
        String[ ] parts = ( expression + " " ).split( "\"" );
        
        // no quote involved, no need for replacement
        if ( parts.length == 1 ) return expression;
        
        // validate the expression by number of quotes; there are odd parts
        if ( parts.length % 2 == 0 )
            throw new WdkUserException( "Odd number of quotes in: " + orgExp );
        
        // replace literals with stub
        StringBuffer sb = new StringBuffer( parts[ 0 ] );
        int stubIndex = 0;
        for ( int i = 1; i < parts.length; i += 2 ) {
            String stub = STUB_PREFIX + ( stubIndex++ );
            replace.put( stub.intern(), parts[ i ].intern() );
            sb.append( stub );
            sb.append( parts[ i + 1 ] );
        }
        return sb.toString();
    }

    private BooleanQuestionNode parseBlock( String block,
            Map< String, String > replace, Map< String, String > operatorMap )
            throws WdkUserException, WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces
        int spaces = block.indexOf( " " );
        int parenthese = block.indexOf( "(" );
        // it's a leaf node
        if ( spaces < 0 && parenthese < 0 ) {
	    return buildLeaf( block, replace );
        }

        // otherwise, need to divide further
        String[ ] triplet = getTriplet( block );
        
        if ( triplet.length == 1 ) { // only remove one pair of parentheses
            return parseBlock( triplet[ 0 ], replace, operatorMap );
        } else { // a triplet
            // create BooleanQuestionNode for each piece
            BooleanQuestionNode left = parseBlock( triplet[ 0 ], replace,
                    operatorMap );
            BooleanQuestionNode right = parseBlock( triplet[ 2 ], replace,
                    operatorMap );
            
            // combine left & right sub-tree to form a new tree
            return BooleanQuestionNode.combine( left, right, triplet[ 1 ],
                    user.getWdkModel(), operatorMap );
        }
    }
    
    private BooleanQuestionNode parseBlockStep( String block, Map<BooleanQuestionNode, Step> stepsMap,
            Map< String, String > replace, Map< String, String > operatorMap )
            throws WdkUserException, WdkModelException {
        // check if the expression can be divided further
        // to do so, just need to check if there're spaces
        int spaces = block.indexOf( " " );
        int parenthese = block.indexOf( "(" );
	String newBlock = block;
        // it's a leaf node
        if ( spaces < 0 && parenthese < 0 ) {
	    return buildLeaf( block, replace );
        }

        // otherwise, need to divide further
        String[ ] triplet = getTriplet( block );
        
        if ( triplet.length == 1 ) { // only remove one pair of parentheses
            return parseBlockStep( triplet[ 0 ], stepsMap, replace, operatorMap );
	} else { // a triplet
            // create BooleanQuestionNode for each piece
            BooleanQuestionNode left = parseBlockStep( triplet[ 0 ], stepsMap, replace,
                    operatorMap );
	    if (stepsMap.containsKey(left)) {
		newBlock = newBlock.replaceAll("\\(" + triplet[0] + "\\)", Integer.toString(stepsMap.get(left).getStepId()));
		newBlock = newBlock.replaceAll(triplet[0], Integer.toString(stepsMap.get(left).getStepId()));
	    }
            BooleanQuestionNode right = parseBlockStep( triplet[ 2 ], stepsMap, replace,
                    operatorMap );
	    if (stepsMap.containsKey(right)) {
		newBlock = newBlock.replaceAll("\\(" + triplet[2] + "\\)", Integer.toString(stepsMap.get(right).getStepId()));
		newBlock = newBlock.replaceAll(triplet[2], Integer.toString(stepsMap.get(right).getStepId()));
	    }

            // combine left & right sub-tree to form a new tree
            BooleanQuestionNode node = BooleanQuestionNode.combine( left, right, triplet[ 1 ],
                    user.getWdkModel(), operatorMap );
	    AnswerValue answer = node.makeAnswerValue(1, user.getItemsPerPage());
	    
	    stepsMap.put(node, user.createStep(answer, newBlock, false));

	    return node;
        }
    }
    
    private BooleanQuestionNode buildLeaf( String block,
            Map< String, String > replace ) throws WdkUserException,
            WdkModelException {
        // the block must be a step id or an id starting with '#'
        String strId = ( block.charAt( 0 ) == '#' ) ? block.substring( 1 )
                : block;
        int stepId;
        try {
            stepId = Integer.parseInt( strId );
        } catch ( NumberFormatException ex ) {
            throw new WdkUserException( "Invalid step Id: " + orgExp );
        }
        
        // get step
        Step step = user.getStep( stepId );
        if ( !step.isValid() )
            throw new WdkUserException( "The step #" + stepId
                    + " is invalid." );
        AnswerValue answer = step.getAnswerValue();
        
        // create a leaf BooleanQuestionNode from the answer
        BooleanQuestionNode leaf = new BooleanQuestionNode(
                answer.getQuestion(), null );
        leaf.setValues( new LinkedHashMap< String, Object >( answer.getParams() ) );
        return leaf;
    }
    
    /**
     * Split the block into left operand, operator, right operand. If it cannot
     * be divided into three part, there should be only one part, put in [0].
     * 
     * @param block
     * @return
     * @throws WdkUserException
     */
    private String[ ] getTriplet( String block ) throws WdkUserException {
        int pos;
        
	// get the right part
	if (block.charAt(block.length() - 1) == ')') {
            int parenthese = 1;
	    pos = block.length() - 1;
            // find the paired closing parenthese
	    while ( pos >= 0 && parenthese > 0 ) {
		if (block.charAt( pos ) == '(' ) parenthese--;
		if (block.charAt( pos ) == ')' ) parenthese++;
                if ( parenthese < 0 )
                    throw new WdkUserException( "Bad parentheses: " + orgExp );
		pos--;
            }
            if ( parenthese != 0 )
                throw new WdkUserException( "Bad parentheses: " + orgExp );
        } else { // no parenthese, then must be separated with space
	    pos = block.lastIndexOf( " " );
        }
	String right = block.substring( pos ).trim();
        // remove parenthese if necessary
        int bound = right.length() - 1;
        if ( right.charAt( 0 ) == '(' && right.charAt( bound ) == ')' )
            right = right.substring( 1, bound ).trim();
        
        // there's only one part
        if ( pos == 0 ) return new String[ ] { right };
        
        // grab operator
        String remain = block.substring( 0, pos ).trim();
        int start = remain.lastIndexOf( " " );
        if ( start < 0 )
            throw new WdkUserException( "Incomplete expression: " + orgExp );
        String operator = remain.substring( start ).trim();
        
        // grab left piece
        String left = remain.substring( 0, start ).trim();
        // remove parenthese if necessary
        bound = left.length() - 1;
        if ( left.charAt( 0 ) == '(' && left.charAt( bound ) == ')' )
            left = left.substring( 1, bound ).trim();
        return new String[ ] { left, operator, right };
    }

    /* This is the original getTriplet, which parses left-to-right instead of right-to-left
    private String[ ] getTriplet( String block ) throws WdkUserException {
        int pos;
        
        // get the left part
        if ( block.charAt( 0 ) == '(' ) {
            int parenthese = 1;
            pos = 1;
            // find the paired closing parenthese
            while ( pos < block.length() && parenthese > 0 ) {
                if ( block.charAt( pos ) == '(' ) parenthese++;
                else if ( block.charAt( pos ) == ')' ) parenthese--;
                if ( parenthese < 0 )
                    throw new WdkUserException( "Bad parentheses: " + orgExp );
                pos++;
            }
            if ( parenthese != 0 )
                throw new WdkUserException( "Bad parentheses: " + orgExp );
        } else { // no parenthese, then must be separated with space
            pos = block.indexOf( " " );
        }
        String left = block.substring( 0, pos ).trim();
        
        // remove parenthese if necessary
        int bound = left.length() - 1;
        if ( left.charAt( 0 ) == '(' && left.charAt( bound ) == ')' )
            left = left.substring( 1, bound ).trim();
        
        // there's only one part
        if ( pos == block.length() ) return new String[ ] { left };
        
        // grab operator
        String remain = block.substring( pos ).trim();
        int end = remain.indexOf( " " );
        if ( end < 0 )
            throw new WdkUserException( "Incomplete expression: " + orgExp );
        String operator = remain.substring( 0, end ).trim();
        
        // grab right piece
        String right = remain.substring( end + 1 ).trim();
        // remove parenthese if necessary
        bound = right.length() - 1;
        if ( right.charAt( 0 ) == '(' && right.charAt( bound ) == ')' )
            right = right.substring( 1, bound ).trim();
        return new String[ ] { left, operator, right };
    }
    */

    public Set< Integer > getOperands( String expression ) {
        Set< Integer > operands = new LinkedHashSet< Integer >();
        // assuming operands are all digits, or digits heading with #
        Pattern pattern = Pattern.compile( "\\b#?(\\d+?)\\b" );
        Matcher matcher = pattern.matcher( expression );
        while ( matcher.find() ) {
            String strId = matcher.group( 1 );
            operands.add( Integer.parseInt( strId ) );
        }
        return operands;
    }
}
