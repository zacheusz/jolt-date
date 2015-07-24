package eu.zacheusz.jolt.date;

import eu.zacheusz.jolt.date.conversion.DateConverter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;


public class ArrayKey extends Key {

    private Collection<Integer> keyInts;
    private int keyInt = -1;

    public ArrayKey( String jsonKey, Object spec, DateConverter inputDateConverter ) {
        super( jsonKey, spec, inputDateConverter );

        // Handle ArrayKey specific stuff
        switch( getOp() ){
            case OR :
                keyInts = new ArrayList<Integer>();
                for( String orLiteral : keyStrings ) {
                    int orInt = Integer.parseInt( orLiteral );
                    keyInts.add( orInt );
                }
                break;
            case LITERAL:
                keyInt = Integer.parseInt( rawKey );
                keyInts = Arrays.asList( keyInt );
                break;
            case STAR:
                keyInts = Collections.emptyList();
                break;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }

    @Override
    protected int getLiteralIntKey() {
        return keyInt;
    }

    @Override
    protected void applyChild( Object container ) {

        if ( container instanceof List ) {
            @SuppressWarnings( "unchecked" )
            List<Object> defaultList = (List<Object>) container;

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( Integer literalKey : determineMatchingContainerKeys( defaultList ) ) {
                applyLiteralKeyToContainer( literalKey, defaultList );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the Container vs the Defaultr Spec type for this key.  Container wins, so do nothing.
    }

    private void applyLiteralKeyToContainer( Integer literalIndex, List<Object> container ) {

        Object currentValue = container.get( literalIndex );
//        System.out.println("Array defaulteeValue: " + currentValue);
        if ( children == null ) {
            if ( currentValue != null) {
                logger.log(Level.FINER, "value with no childrens for key: " + literalIndex);
              //  container.set( literalIndex, DeepCopy.simpleDeepCopy( literalValue ) );  // apply a copy of the default value into a List, assumes the list as already been expanded if needed.
                 if (literalValue instanceof String) {
                 	Object newValue = reformatDate((String)literalValue, currentValue);
                 	container.set( literalIndex, newValue );  
                 } else {
                     logger.log(Level.WARNING, "spec value is not an instance of String: " + literalValue);
                 }
            } else {
                logger.log(Level.WARNING, "value for key " + literalIndex + " is null");
            }
        }
        else {
            if ( currentValue == null ) {
                currentValue = createOutputContainerObject();
                container.set( literalIndex, currentValue ); // push a new sub-container into this list
            }

            // recurse by applying my children to this known valid container
            applyChildren( currentValue );
        }
    }

    private Collection<Integer> determineMatchingContainerKeys( List<Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // Container it should get these literal values added to it
                return keyInts;
            case STAR:
                // Identify all its keys
                // this assumes the container list has already been expanded to the right size
                List defaultList = (List) container;
                List<Integer> allIndexes = new ArrayList<Integer>( defaultList.size() );
                for ( int index = 0; index < defaultList.size(); index++ ) {
                    allIndexes.add( index );
                }

                return allIndexes;
            case OR:
                // Identify the intersection between the container "keys" and the OR values
                List<Integer> indexesInRange = new ArrayList<Integer>();

                for ( Integer orValue : keyInts ) {
                    if ( orValue < ((List) container ).size() ) {
                        indexesInRange.add( orValue );
                    }
                }
                return indexesInRange;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }
}