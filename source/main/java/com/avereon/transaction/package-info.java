/**
 * Basic, multi-purpose, thread scoped transaction management. Originally
 * developed as part of the {@link com.avereon.data} package, the logic was
 * eventually separated for other uses. The general intent is that things that
 * should happen together, happen together when the transaction is committed.
 */
package com.avereon.transaction;