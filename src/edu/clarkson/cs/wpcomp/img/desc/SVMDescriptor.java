package edu.clarkson.cs.wpcomp.img.desc;

import edu.clarkson.cs.wpcomp.img.accessor.ColorAccessor;

/**
 * SVMDescriptor generate a vector that can be used as input of SVMs.
 * 
 * @author harper
 * @since webpage-comparison 1.0
 * @version 1.0
 * 
 * 
 */
public interface SVMDescriptor {

	public Feature describe(ColorAccessor input);
}