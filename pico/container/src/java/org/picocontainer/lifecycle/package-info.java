/**
 * Alternative implementations of lifecycle strategy for use with a container.
 *  Currently supported options are:
 *  <ul>
 *    <li>Implement <code>Startable</code> and/or <code>Disposable</code> (or other strong interface)</li>
 *    <li>Use configuration to wire methods that are start/stop/dispose equivalent.</li>
 *    <li>J2EE 5.0 Annotation-based lifecycles.</li>
 *    <li>Combinations thereof (composite)</li>
 *  </ul>
 */
package org.picocontainer.lifecycle;

