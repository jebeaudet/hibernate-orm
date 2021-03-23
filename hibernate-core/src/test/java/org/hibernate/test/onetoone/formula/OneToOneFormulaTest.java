/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.test.onetoone.formula;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Root;

import org.hibernate.Hibernate;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.TextType;
import org.hibernate.type.descriptor.jdbc.ClobTypeDescriptor;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.junit4.BaseCoreFunctionalTestCase;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsNull.notNullValue;
import static org.hibernate.testing.transaction.TransactionUtil.doInHibernate;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Gavin King
 */
public class OneToOneFormulaTest extends BaseCoreFunctionalTestCase {
	private static class TextAsMaterializedClobType extends AbstractSingleColumnStandardBasicType<String> {
		public final static TextAsMaterializedClobType INSTANCE = new TextAsMaterializedClobType();
		public TextAsMaterializedClobType() {
			super(  ClobTypeDescriptor.DEFAULT, TextType.INSTANCE.getJavaTypeDescriptor() );
		}
		public String getName() {
			return TextType.INSTANCE.getName();
		}
	}

	public String[] getMappings() {
		return new String[] { "onetoone/formula/Person.hbm.xml" };
	}

	public void configure(Configuration cfg) {
		if ( Oracle8iDialect.class.isInstance( getDialect() ) ) {
			cfg.registerTypeOverride( TextAsMaterializedClobType.INSTANCE );
		}
		cfg.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
		cfg.setProperty(Environment.GENERATE_STATISTICS, "true");
		cfg.setProperty(Environment.DEFAULT_BATCH_FETCH_SIZE, "2");
	}

	private Person person;
	private Address address;

	@Before
	public void setUp(){
		person = new Person();
		person.setName( "Gavin King" );
		address = new Address();
		address.setPerson( person );
		address.setType( "HOME" );
		address.setZip( "3181" );
		address.setState( "VIC" );
		address.setStreet( "Karbarook Ave" );
		person.setAddress( address );

		doInHibernate( this::sessionFactory, session -> {
			session.persist( person );
		} );
	}

	@Override
	protected void cleanupTest() {
		doInHibernate( this::sessionFactory, session -> {
			session.delete( person );
		} );
	}

	@Test
	public void testOneToOneFormula() {

		doInHibernate( this::sessionFactory, s -> {
			Person p = (Person) s.createQuery( "from Person" ).uniqueResult();

			assertNotNull( p.getAddress() );
			assertTrue( Hibernate.isInitialized( p.getAddress() ) );
			assertNull( p.getMailingAddress() );
		} );

		doInHibernate( this::sessionFactory, s -> {
			Person p = (Person) s.createQuery(
					"from Person p left join fetch p.mailingAddress left join fetch p.address" ).uniqueResult();

			assertNotNull( p.getAddress() );
			assertTrue( Hibernate.isInitialized( p.getAddress() ) );
			assertNull( p.getMailingAddress() );
		} );

		doInHibernate( this::sessionFactory, s -> {
			Person p = (Person) s.createQuery( "from Person p left join fetch p.address" ).uniqueResult();

			assertNotNull( p.getAddress() );
			assertTrue( Hibernate.isInitialized( p.getAddress() ) );
			assertNull( p.getMailingAddress() );
		} );

		doInHibernate( this::sessionFactory, s -> {
			CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
			CriteriaQuery<Person> criteria = criteriaBuilder.createQuery( Person.class );
			Root<Person> root = criteria.from( Person.class );
			Join<Object, Object> address = root.join( "address", JoinType.INNER );
			criteria.where( criteriaBuilder.equal( address.get( "zip" ), "3181" ) );
			Person p = s.createQuery( criteria ).uniqueResult();

//			Person p = (Person) s.createCriteria( Person.class )
//					.createCriteria( "address" )
//					.add( Property.forName( "zip" ).eq( "3181" ) )
//					.uniqueResult();
			assertNotNull( p );
		} );

		doInHibernate( this::sessionFactory, s -> {
			CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
			CriteriaQuery<Person> criteria = criteriaBuilder.createQuery( Person.class );
			Root<Person> root = criteria.from( Person.class );
			root.fetch( "address", JoinType.LEFT );
			Person p = s.createQuery( criteria ).uniqueResult();
//			Person p = (Person) s.createCriteria( Person.class )
//					.setFetchMode( "address", FetchMode.JOIN )
//					.uniqueResult();

			assertNotNull( p.getAddress() );
			assertTrue( Hibernate.isInitialized( p.getAddress() ) );
			assertNull( p.getMailingAddress() );
		} );

		doInHibernate( this::sessionFactory, s -> {
			CriteriaBuilder criteriaBuilder = s.getCriteriaBuilder();
			CriteriaQuery<Person> criteria = criteriaBuilder.createQuery( Person.class );
			Root<Person> root = criteria.from( Person.class );
			root.fetch( "address", JoinType.LEFT );
			Person p = s.createQuery( criteria ).uniqueResult();
//			Person p = (Person) s.createCriteria( Person.class )
//					.setFetchMode( "mailingAddress", FetchMode.JOIN )
//					.uniqueResult();

			assertNotNull( p.getAddress() );
			assertTrue( Hibernate.isInitialized( p.getAddress() ) );
			assertNull( p.getMailingAddress() );

		} );
	}


	@Test
	@TestForIssue(jiraKey = "HHH-5757")
	public void testQuery() {
		doInHibernate( this::sessionFactory, session -> {
			Person p = (Person) session.createQuery( "from Person p where p.address = :address" ).setParameter(
					"address",
					address ).uniqueResult();
			assertThat( p, notNullValue() );
		} );

		doInHibernate( this::sessionFactory, session -> {
			Address a = (Address) session.createQuery( "from Address a where a.person = :person" ).setParameter(
					"person",
					person ).uniqueResult();
			assertThat( a, notNullValue() );

		} );
	}

	@Test
	public void testOneToOneEmbeddedCompositeKey() {
		doInHibernate( this::sessionFactory, session -> {
			Address a = new Address();
			a.setType("HOME");
			a.setPerson(person);
			a = session.load(Address.class, a);
			assertFalse( Hibernate.isInitialized(a) );
			a.getPerson();
			a.getType();
			assertFalse( Hibernate.isInitialized(a) );
			assertEquals(a.getZip(), "3181");
		} );

		doInHibernate( this::sessionFactory, session -> {
			Address a = new Address();
			a.setType("HOME");
			a.setPerson(person);
			Address a2 = session.get(Address.class, a);
			assertTrue( Hibernate.isInitialized(a) );
			assertSame(a2, a);
			assertSame(a2.getPerson(), person); //this is a little bit desirable
			assertEquals(a.getZip(), "3181");
		} );
		


		
//		s.delete(a2);
//		s.delete( s.get( Person.class, p.getName() ) ); //this is certainly undesirable! oh well...
//
//		t.commit();
//		s.close();

	}

}
