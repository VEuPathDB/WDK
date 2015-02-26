/**
 * Factory classes that interface with the WDK Model and build
 * result objects to be formatted and passed to the client.  Since
 * the API of the WDK Model predates the development of the WDK
 * service, it does not always accept the same representations
 * we're defining for the service.  Thus, the classes in this
 * package act as translators between the data formats in the
 * service API and the needs of the WDK model.  Over time, the WDK
 * Model API will evolve to more closely match the data
 * representations created in the service.
 */
package org.gusdb.wdk.service.factory;
