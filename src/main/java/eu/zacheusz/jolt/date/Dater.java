package eu.zacheusz.jolt.date;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.log4j.Logger;

import com.bazaarvoice.jolt.SpecDriven;
import com.bazaarvoice.jolt.Transform;
import com.bazaarvoice.jolt.exception.SpecException;
import com.bazaarvoice.jolt.exception.TransformException;
import com.thoughtworks.xstream.converters.basic.DateConverter;

public class Dater implements SpecDriven, Transform {

	private final Logger logger = Logger.getLogger(getClass());
	
    public interface WildCards {
        String STAR = "*";
        String OR = "|";
        String ARRAY = "[]";
    }

    private final Key mapRoot;
    private final Key arrayRoot;

    /**
     * Configure an instance of Dater with a spec.
     *
     * @throws SpecException for a malformed spec or if there are issues
     */
    @Inject
    public Dater( Object spec ) {

    	Map<String, Object> specMap = (Map<String,Object>)spec;
    	
    	Map<String,String> input = (Map<String,String>)specMap.get("input");
    	final Object defaultFormat = input.get("defaultInputFormat");
    	if (!(defaultFormat instanceof String)) {
    		throw new SpecException("defaultInputFormat context parameter is not instanceof String");
    	}
    	logger.info("defaultInputFormat: " + defaultFormat);
    	final Object acceptableFormats = input.get("acceptableInputFormats");
    	if (!(acceptableFormats instanceof List)) {
    		throw new SpecException("acceptableInputFormats context parameter is not instanceof String[]");
    	}
    	List<String> acceptableFormatsList = (List<String>)acceptableFormats;
    	
    	logger.info("acceptableInputFormats: " + acceptableFormatsList);
    	final String[] af = acceptableFormatsList.toArray(new String[acceptableFormatsList.size()]);
        final DateConverter dateConverter = new DateConverter((String)defaultFormat, af);
        
    	
        String rootKey = "root";

        // Due to defaultr's array syntax, we can't actually express that we expect the top level of the defaultee to be an array, until we see the input.
        //  Thus, in order to have parsed the spec so that we can perform many transforms, we create to specs, one where the root of the input
        //   is a map, and the other where the root of the input is an array.
        // TODO : Handle arrays better, maybe by having a parent reference in the keys, or ditch the feature of having input that is at top level an array

        {
            Map<String, Object> rootSpec = new LinkedHashMap<String, Object>();
            rootSpec.put( rootKey, specMap.get("output") );
            mapRoot = Key.parseSpec( rootSpec, dateConverter ).iterator().next();
        }

        //  Thus we check the top level type of the input.
        {
            Map<String, Object> rootSpec = new LinkedHashMap<String, Object>();
            rootSpec.put( rootKey + WildCards.ARRAY,  specMap.get("output") );
            Key tempKey = null;
            try {
                tempKey = Key.parseSpec( rootSpec, dateConverter ).iterator().next();
            }
            catch ( NumberFormatException nfe ) {
                // this is fine, it means the top level spec has non numeric keys
                //  if someone passes a top level array as input later we will error then
            }
            arrayRoot = tempKey;
        }
        
    }

    /**
     * Top level standalone Defaultr method.
     *
     * @param input JSON object to have defaults applied to. This will be modified.
     * @return the modified input
     */
    @Override
    public Object transform(Object input) {

    	
        if ( input == null ) {
            // if null, assume HashMap
            input = new HashMap();
        }

        // TODO : Make copy of the defaultee or like shiftr create a new output object
        if ( input instanceof List ) {
            if  ( arrayRoot == null ) {
                throw new TransformException( "The Spec provided can not handle input that is a top level Json Array." );
            }
            arrayRoot.applyChildren( input );
        }
        else {
            mapRoot.applyChildren( input );
        }

        return input;
    }
}