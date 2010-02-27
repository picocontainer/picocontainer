package org.picocontainer.converters;

import org.picocontainer.converters.Converter;


/**
 * Converts string to single-character.  It does so by only grabbing
 * the first character in the string. 
 * 
 * @author Paul Hammant
 * @author Michael Rimov
 */
class CharacterConverter implements Converter<Character> {

    public Character convert(String paramValue) {
        return paramValue.charAt(0);
    }
}
