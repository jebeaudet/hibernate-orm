/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

import org.hibernate.engine.jdbc.LobCreator;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.type.descriptor.jdbc.JdbcTypeDescriptor;

/**
 * Gives binding (nullSafeSet) and extracting (nullSafeGet) code access to options.
 *
 * @author Steve Ebersole
 */
public interface WrapperOptions {

	/**
	 * Access to the current Session
	 */
	SharedSessionContractImplementor getSession();

	/**
	 * Should streams be used for binding LOB values.
	 *
	 * @return {@code true}/{@code false}
	 */
	boolean useStreamForLobBinding();

	/**
	 * Obtain access to the {@link LobCreator}
	 *
	 * @return The LOB creator
	 */
	LobCreator getLobCreator();

	/**
	 * Allow remapping of descriptors for dealing with sql type.
	 *
	 * @param jdbcTypeDescriptor The known descriptor
	 *
	 * @return The remapped descriptor.  May be the same as the known descriptor indicating no remapping.
	 */
	JdbcTypeDescriptor remapSqlTypeDescriptor(JdbcTypeDescriptor jdbcTypeDescriptor);

	/**
	 * The JDBC {@link TimeZone} used when persisting Timestamp and DateTime properties into the database.
	 * This setting is used when storing timestamps using the {@link java.sql.PreparedStatement#setTimestamp(int, Timestamp, Calendar)} method.
	 *
	 * This way, the storage {@link TimeZone} can differ from the default JVM TimeZone given by {@link TimeZone#getDefault()}.
	 *
	 * @return JDBC {@link TimeZone}
	 */
	TimeZone getJdbcTimeZone();
}
