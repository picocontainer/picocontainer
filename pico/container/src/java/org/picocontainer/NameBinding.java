/*****************************************************************************
 * Copyright (C) 2003-2011 PicoContainer Committers. All rights reserved.    *
 * ------------------------------------------------------------------------- *
 * The software in this package is published under the terms of the BSD      *
 * style license a copy of which has been included with this distribution in *
 * the LICENSE.txt file.                                                     *
 *                                                                           *
 * Original code by                                                          *
 *****************************************************************************/
package org.picocontainer;

public interface NameBinding {
    String getName();

    /**
     * <em>Special Case</em> of name binding that represents a null NameBinding argument.
     */
    NameBinding NULL = new NameBinding() {

 		public String getName() {
			return null;
		}

		@Override
		public int hashCode() {
			return 42;
		}

		@Override
		public boolean equals(final Object obj) {
			return this == obj;
		}

		@Override
		public String toString() {
			return "Null Name Binding";
		}


    };

}
