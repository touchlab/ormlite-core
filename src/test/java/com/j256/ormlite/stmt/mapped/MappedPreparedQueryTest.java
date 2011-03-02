package com.j256.ormlite.stmt.mapped;

import static org.junit.Assert.assertEquals;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.j256.ormlite.BaseCoreTest;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.FieldType;
import com.j256.ormlite.stmt.SelectArg;
import com.j256.ormlite.stmt.StatementBuilder.StatementType;
import com.j256.ormlite.support.CompiledStatement;
import com.j256.ormlite.support.DatabaseResults;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.TableInfo;

public class MappedPreparedQueryTest extends BaseCoreTest {

	private final static String TABLE_NAME = "tableName";

	@Test
	public void testMapRow() throws Exception {
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, true);
		LocalFoo foo1 = new LocalFoo();
		fooDao.create(foo1);

		TableInfo<LocalFoo> tableInfo = new TableInfo<LocalFoo>(connectionSource, LocalFoo.class);
		MappedPreparedStmt<LocalFoo, Integer> rowMapper =
				new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, null, new ArrayList<FieldType>(),
						Arrays.asList(tableInfo.getFieldTypes()), new ArrayList<SelectArg>(), null,
						StatementType.SELECT);

		CompiledStatement stmt =
				connectionSource.getReadOnlyConnection().compileStatement("select * from " + TABLE_NAME,
						StatementType.SELECT, new FieldType[0], new FieldType[0]);

		DatabaseResults results = stmt.runQuery();
		while (results.next()) {
			LocalFoo foo2 = rowMapper.mapRow(results);
			assertEquals(foo1.id, foo2.id);
		}
	}

	@Test
	public void testLimit() throws Exception {
		Dao<LocalFoo, Object> fooDao = createDao(LocalFoo.class, true);
		List<LocalFoo> foos = new ArrayList<LocalFoo>();
		LocalFoo foo = new LocalFoo();
		// create foo #1
		fooDao.create(foo);
		foos.add(foo);
		foo = new LocalFoo();
		// create foo #2
		fooDao.create(foo);
		foos.add(foo);

		TableInfo<LocalFoo> tableInfo = new TableInfo<LocalFoo>(connectionSource, LocalFoo.class);
		MappedPreparedStmt<LocalFoo, Integer> preparedQuery =
				new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, "select * from " + TABLE_NAME,
						new ArrayList<FieldType>(), Arrays.asList(tableInfo.getFieldTypes()),
						new ArrayList<SelectArg>(), 1, StatementType.SELECT);

		checkResults(foos, preparedQuery, 1);
		preparedQuery =
				new MappedPreparedStmt<LocalFoo, Integer>(tableInfo, "select * from " + TABLE_NAME,
						new ArrayList<FieldType>(), Arrays.asList(tableInfo.getFieldTypes()),
						new ArrayList<SelectArg>(), null, StatementType.SELECT);
		checkResults(foos, preparedQuery, 2);
	}

	private void checkResults(List<LocalFoo> foos, MappedPreparedStmt<LocalFoo, Integer> preparedQuery, int expectedNum)
			throws SQLException {
		CompiledStatement stmt = null;
		try {
			stmt = preparedQuery.compile(connectionSource.getReadOnlyConnection());
			DatabaseResults results = stmt.runQuery();
			int fooC = 0;
			while (results.next()) {
				LocalFoo foo2 = preparedQuery.mapRow(results);
				assertEquals(foos.get(fooC).id, foo2.id);
				fooC++;
			}
			assertEquals(expectedNum, fooC);
		} finally {
			if (stmt != null) {
				stmt.close();
			}
		}
	}

	@Test(expected = IllegalArgumentException.class)
	public void testObjectNoConstructor() throws SQLException {
		new MappedPreparedStmt<NoConstructor, Void>(
				new TableInfo<NoConstructor>(connectionSource, NoConstructor.class), null, new ArrayList<FieldType>(),
				new ArrayList<FieldType>(), new ArrayList<SelectArg>(), null, StatementType.SELECT);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDifferentArgSizes() throws SQLException {
		ArrayList<SelectArg> selectArgList = new ArrayList<SelectArg>();
		selectArgList.add(new SelectArg());
		new MappedPreparedStmt<LocalFoo, Integer>(new TableInfo<LocalFoo>(connectionSource, LocalFoo.class), null,
				new ArrayList<FieldType>(), new ArrayList<FieldType>(), selectArgList, null, StatementType.SELECT);
	}

	@DatabaseTable(tableName = TABLE_NAME)
	protected static class LocalFoo {
		@DatabaseField(generatedId = true)
		int id;
		@DatabaseField
		String stuff;
	}

	protected static class NoConstructor {
		@DatabaseField
		String id;
		NoConstructor(int someField) {
			// to stop the default no-arg constructor
		}
	}
}