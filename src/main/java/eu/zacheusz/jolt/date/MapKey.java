package eu.zacheusz.jolt.date;

import com.bazaarvoice.jolt.common.DeepCopy;
import eu.zacheusz.jolt.date.conversion.DateConverter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

public class MapKey extends Key {

    public MapKey( String jsonKey, Object spec, DateConverter inputDateConverter ) {
        super( jsonKey, spec, inputDateConverter );
    }

    @Override
    protected int getLiteralIntKey() {
        throw new UnsupportedOperationException( "Shouldn't be be asking a MapKey for int getLiteralIntKey()."  );
    }

    @Override
    protected void applyChild( Object container ) {

        if ( container instanceof Map ) {
            Map<String, Object> defaulteeMap = (Map<String, Object>) container;

            // Find all defaultee keys that match the childKey spec.  Simple for Literal keys, more work for * and |.
            for ( String literalKey : determineMatchingContainerKeys( defaulteeMap ) ) {
                applyLiteralKeyToContainer( literalKey, defaulteeMap );
            }
        }
        // Else there is disagreement (with respect to Array vs Map) between the data in
        //  the Container vs the Defaultr Spec type for this key.  Container wins, so do nothing.
    }

    private void applyLiteralKeyToContainer( String literalKey, Map<String, Object> container ) {

        Object currentValue = container.get( literalKey );
        logger.log(Level.FINER, "for key: " + literalKey + " value: " + currentValue);
//        System.out.println("map defaulteeValue: " + currentValue);
        if ( children == null ) {
            if ( currentValue != null ) {
                logger.log(Level.FINER, "value with no childrens for key: " + literalKey);
               // container.put( literalKey, DeepCopy.simpleDeepCopy( literalValue ) );  // apply a copy of the default value into a map

                if (literalValue instanceof String) {
                	Object newValue = reformatDate((String)literalValue, currentValue);
                	container.put( literalKey, newValue );  
                } else {
                    logger.log(Level.WARNING, "spec value is not an instance of String: " + literalValue);
                }
            } else {
                logger.log(Level.WARNING, "value for key " + literalKey + " is null");
            }
        }
        else {
            if ( currentValue == null ) {
                currentValue = createOutputContainerObject();
                container.put( literalKey, currentValue );  // push a new sub-container into this map
            }

            // recurse by applying my children to this known valid container
            applyChildren( currentValue );
        }
    }

    private Collection<String> determineMatchingContainerKeys( Map<String, Object> container ) {

        switch ( getOp() ) {
            case LITERAL:
                // the container should get these literal values added to it
                return keyStrings;
            case STAR:
                // Identify all its keys
                return container.keySet();
            case OR:
                // Identify the intersection between its keys and the OR values
                Set<String> intersection = new HashSet<String>( container.keySet() );
                intersection.retainAll( keyStrings );
                return intersection;
            default :
                throw new IllegalStateException( "Someone has added an op type without changing this method." );
        }
    }
}