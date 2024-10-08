/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.engine.spi;

import org.hibernate.type.Type;
import org.hibernate.type.MappingContext;

/**
 * Declares operations used by implementors of {@link Type} that are common to
 * "compiled" mappings held at runtime by a {@link org.hibernate.SessionFactory}
 * and "uncompiled" mappings held by a {@link org.hibernate.cfg.Configuration}.
 *
 * @see Type
 * @see org.hibernate.internal.SessionFactoryImpl
 * @see org.hibernate.cfg.Configuration
 *
 * @author Gavin King
 *
 * @deprecated Use {@link org.hibernate.type.spi.TypeConfiguration},
 * {@link org.hibernate.boot.Metadata}, or
 * {@link org.hibernate.metamodel.RuntimeMetamodels}
 * or {@link  MappingContext}
 * to access such information
 */
@Deprecated(since = "6.0")
public interface Mapping extends MappingContext {
}
